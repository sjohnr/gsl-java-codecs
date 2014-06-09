/* ============================================================================
 * GridMessage.java
 * 
 * Generated codec class for GridMessage
 * ----------------------------------------------------------------------------
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by   
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.                                      
 *                                                                      
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of           
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU     
 * Lesser General Public License for more details.                      
 *                                                                      
 * You should have received a copy of the GNU Lesser General Public     
 * License along with this program. If not, see                         
 * http://www.gnu.org/licenses.                                         
 * ============================================================================
 */
package grid.zmq;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.nio.ByteBuffer;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * GridMessage codec.
 * 
 * The specification for this class is as follows:
 * <pre class="text">
 *  CONNECT - Send connection information to establish a connection with a new peer.
 *	  sequence		number 2
 *	  ip		string
 *	  port		number 2
 *	  clusters		strings
 *	  status		number 1
 *	  headers		dictionary
 *  WHISPER - Send a message to a peer.
 *	  sequence		number 2
 *	  content		frame
 *  BROADCAST - Send out a state change for followers.
 *	  sequence		number 2
 *	  cluster		string
 *	  content		frame
 *  JOIN - Request membership to a cluster.
 *	  sequence		number 2
 *	  cluster		string
 *	  status		number 1
 *  EXIT - Relinquish membership from a cluster.
 *	  sequence		number 2
 *	  cluster		string
 *	  status		number 1
 *  PING - Ping a peer that has gone silent.
 *	  sequence		number 2
 *  ECHO - Reply to a peer's ping.
 *	  sequence		number 2
 * </pre>
 * 
 * @author sriesenberg
 */
public class GridMessage implements Cloneable {
	public static final int GRID_MESSAGE_VERSION	= 1;
	
	public static final int CONNECT			= 1;
	public static final int WHISPER			= 2;
	public static final int BROADCAST			= 3;
	public static final int JOIN			= 4;
	public static final int EXIT			= 5;
	public static final int PING			= 6;
	public static final int ECHO			= 7;
	
	//  Structure of our class
	private ZFrame address;		//  Address of peer if any
	private int id;				//  GridMessage message ID
	private ByteBuffer needle;	//  Read/write pointer for serialization
	private int sequence;
	private String ip;
	private int port;
	private List<String> clusters;
	private int status;
	private Map<String, String> headers;
	private int headersBytes;
	private ZFrame content;
	private String cluster;
	
	/**
	 * Create a new GridMessage.
	 * 
	 * @param id The Message ID
	 */
	public GridMessage(int id) {
		this.id = id;
	}
	
	/**
	 * Destroy the GridMessage.
	 */
	public void destroy() {
		//  Free class properties
		if (address != null)
			address.destroy();
		address = null;

        //  Destroy frame fields
		if (content != null)
			content.destroy();
		content = null;
	}
	
	//  --------------------------------------------------------------------------
	//  Network data encoding macros

	//  Put a 1-byte number to the frame
	protected final void putNumber1(int value) {
		needle.put((byte) value);
	}

	//  Get a 1-byte number to the frame
	//  then make it unsigned
	protected final int getNumber1() { 
		int value = needle.get(); 
		if (value < 0)
			value =(0xff) & value;
		return value;
	}

	//  Put a 2-byte number to the frame
	protected final void putNumber2(int value) {
		needle.putShort((short) value);
	}

	//  Get a 2-byte number to the frame
	protected final int getNumber2() { 
		int value = needle.getShort(); 
		if (value < 0)
			value =(0xffff) & value;
		return value;
	}

	//  Put a 4-byte number to the frame
	protected final void putNumber4(long value)  {
		needle.putInt((int) value);
	}

	//  Get a 4-byte number to the frame
	//  then make it unsigned
	protected final long getNumber4()  { 
		long value = needle.getInt(); 
		if (value < 0)
			value =(0xffffffff) & value;
		return value;
	}

	//  Put a 8-byte number to the frame
	protected final void putNumber8(long value)  {
		needle.putLong(value);
	}

	//  Get a 8-byte number to the frame
	protected final long getNumber8()  {
		return needle.getLong();
	}

	//  Put a block to the frame
	protected final void putBlock(byte[] value, int size)  {
		needle.put(value, 0, size);
	}

	protected final byte[] getBlock(int size)  {
		byte[] value = new byte[size]; 
		needle.get(value);

		return value;
	}

	//  Put a string to the frame
	protected final void putString(String value)  {
		needle.put((byte) value.length());
		needle.put(value.getBytes());
	}

	//  Get a string from the frame
	protected final String getString()  {
		int size = getNumber1();
		byte[] value = new byte[size];
		needle.get(value);

		return new String(value);
	}

	/**
	 * Receive and parse a GridMessage from the socket. Returns new object or
	 * null if error. Will block if there's no message waiting.
	 * 
	 * @param input The socket used to receive this GridMessage
	 */
	public static GridMessage receive(Socket input) {
		assert (input != null);
		GridMessage self = new GridMessage(0);
		ZFrame frame = null;

		try {
			//  Read valid message frame from socket; we loop over any
			//  garbage data we might receive from badly-connected peers
			while (true) {
				//  If we're reading from a ROUTER socket, get address
				if (input.getType() == ZMQ.ROUTER) {
					self.address = ZFrame.recvFrame(input);
					if (self.address == null)
						return null;		 //  Interrupted
					if (!input.hasReceiveMore())
						throw new IllegalArgumentException();
				}
				//  Read and parse command in frame
				frame = ZFrame.recvFrame(input);
				if (frame == null)
					return null;			 //  Interrupted

				//  Get and check protocol signature
				self.needle = ByteBuffer.wrap(frame.getData()); 
				int signature = self.getNumber2();
				if (signature ==(0xAAA0 | 1))
					break;				  //  Valid signature

				//  Protocol assertion, drop message
				while (input.hasReceiveMore()) {
					frame.destroy();
					frame = ZFrame.recvFrame(input);
				}
				frame.destroy();
			}

			//  Get message id, which is first byte in frame
			self.id = self.getNumber1();

			switch (self.id) {
				case CONNECT:
					self.sequence = self.getNumber2();
					self.ip = self.getString();
					self.port = self.getNumber2();
					int clustersListSize = self.getNumber1();
					self.clusters = new ArrayList<String>();
					while (clustersListSize-- > 0) {
						String string = self.getString();
						self.clusters.add(string);
					}
					self.status = self.getNumber1();
					int headersHashSize = self.getNumber1();
					self.headers = new HashMap<String, String>();
					while (headersHashSize-- > 0) {
						String string = self.getString();
						String[] kv = string.split("=");
						self.headers.put(kv[0], kv[1]);
					}

					break;

				case WHISPER:
					self.sequence = self.getNumber2();
					//  Get next frame, leave current untouched
					if (!input.hasReceiveMore())
						throw new IllegalArgumentException();
					self.content = ZFrame.recvFrame(input);
					break;

				case BROADCAST:
					self.sequence = self.getNumber2();
					self.cluster = self.getString();
					//  Get next frame, leave current untouched
					if (!input.hasReceiveMore())
						throw new IllegalArgumentException();
					self.content = ZFrame.recvFrame(input);
					break;

				case JOIN:
					self.sequence = self.getNumber2();
					self.cluster = self.getString();
					self.status = self.getNumber1();
					break;

				case EXIT:
					self.sequence = self.getNumber2();
					self.cluster = self.getString();
					self.status = self.getNumber1();
					break;

				case PING:
					self.sequence = self.getNumber2();
					break;

				case ECHO:
					self.sequence = self.getNumber2();
					break;

				default:
					throw new IllegalArgumentException();
			}

			return self;
		} catch (Exception e) {
			//  Error returns
			System.out.printf("E: malformed message '%d'\n", Integer.valueOf(self.id));
			self.destroy();
			return null;
		} finally {
			if (frame != null)
				frame.destroy();
		}
	}

	//  Count size of key=value pair
	private static void headersCount(final Map.Entry<String, String> entry, GridMessage self) {
		self.headersBytes += entry.getKey().length() + 1 + entry.getValue().length() + 1;
	}

	//  Serialize headers key=value pair
	private static void headersWrite(final Map.Entry<String, String> entry, GridMessage self) {
		String string = entry.getKey() + "=" + entry.getValue();
		self.putString(string);
	}

	/**
	 * Send the GridMessage to the socket, and destroy it.
	 * 
	 * @param socket The socket used to send this GridMessage
	 */
	public boolean send(Socket socket) {
		assert (socket != null);

		//  Calculate size of serialized data
		int frameSize = 2 + 1;		  //  Signature and message ID
		switch (id) {
			case CONNECT:
				//  sequence is a 2-byte integer
				frameSize += 2;
				//  ip is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (ip != null)
					frameSize += ip.length();
				//  port is a 2-byte integer
				frameSize += 2;
				//  clusters is an array of strings
				frameSize++;		//  Size is one octet
				if (clusters != null) {
					for (String value : clusters) 
						frameSize += 1 + value.length();
				}
				//  status is a 1-byte integer
				frameSize += 1;
				//  headers is an array of key=value strings
				frameSize++;		//  Size is one octet
				if (headers != null) {
					headersBytes = 0;
					for (Map.Entry<String, String> entry: headers.entrySet()) {
						headersCount(entry, this);
					}
					frameSize += headersBytes;
				}
				break;
				
			case WHISPER:
				//  sequence is a 2-byte integer
				frameSize += 2;
				break;
				
			case BROADCAST:
				//  sequence is a 2-byte integer
				frameSize += 2;
				//  cluster is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (cluster != null)
					frameSize += cluster.length();
				break;
				
			case JOIN:
				//  sequence is a 2-byte integer
				frameSize += 2;
				//  cluster is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (cluster != null)
					frameSize += cluster.length();
				//  status is a 1-byte integer
				frameSize += 1;
				break;
				
			case EXIT:
				//  sequence is a 2-byte integer
				frameSize += 2;
				//  cluster is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (cluster != null)
					frameSize += cluster.length();
				//  status is a 1-byte integer
				frameSize += 1;
				break;
				
			case PING:
				//  sequence is a 2-byte integer
				frameSize += 2;
				break;
				
			case ECHO:
				//  sequence is a 2-byte integer
				frameSize += 2;
				break;
				
			default:
				System.out.printf("E: bad message type '%d', not sent\n", Integer.valueOf(id));
				assert (false);
		}

		//  Now serialize message into the frame
		ZFrame frame = new ZFrame(new byte[frameSize]);
		needle = ByteBuffer.wrap(frame.getData()); 
		int frameFlags = 0;
		putNumber2(0xAAA0 | 1);
		putNumber1((byte) id);

		switch (id) {
			case CONNECT:
				putNumber2(sequence);
				if (ip != null)
					putString(ip);
				else
					putNumber1((byte) 0);	  //  Empty string
				putNumber2(port);
				if (clusters != null) {
					putNumber1((byte) clusters.size());
					for (String value : clusters) {
						putString(value);
					}
				}
				else
					putNumber1((byte) 0);	  //  Empty string array
				putNumber1(status);
				if (headers != null) {
					putNumber1((byte) headers.size());
					for (Map.Entry<String, String> entry: headers.entrySet()) {
						headersWrite(entry, this);
					}
				}
				else
					putNumber1((byte) 0);	  //  Empty dictionary
				break;
			
			case WHISPER:
				putNumber2(sequence);
				frameFlags = ZMQ.SNDMORE;
				break;
			
			case BROADCAST:
				putNumber2(sequence);
				if (cluster != null)
					putString(cluster);
				else
					putNumber1((byte) 0);	  //  Empty string
				frameFlags = ZMQ.SNDMORE;
				break;
			
			case JOIN:
				putNumber2(sequence);
				if (cluster != null)
					putString(cluster);
				else
					putNumber1((byte) 0);	  //  Empty string
				putNumber1(status);
				break;
			
			case EXIT:
				putNumber2(sequence);
				if (cluster != null)
					putString(cluster);
				else
					putNumber1((byte) 0);	  //  Empty string
				putNumber1(status);
				break;
			
			case PING:
				putNumber2(sequence);
				break;
			
			case ECHO:
				putNumber2(sequence);
				break;
			
		}

		//  If we're sending to a ROUTER, we send the address first
		if (socket.getType() == ZMQ.ROUTER) {
			assert (address != null);
			if (!address.send(socket, ZMQ.SNDMORE)) {
				destroy();
				return false;
			}
		}

		//  Now send the data frame
		if (!frame.sendAndDestroy(socket, frameFlags)) {
			frame.destroy();
			destroy();
			return false;
		}

		//  Now send any frame fields, in order
		switch (id) {
			case WHISPER:
				//  If content isn't set, send an empty frame
				if (content == null)
					content = new ZFrame("".getBytes());
				if (!content.send(socket, 0)) {
					frame.destroy();
					destroy();
					return false;
				}
				break;
			case BROADCAST:
				//  If content isn't set, send an empty frame
				if (content == null)
					content = new ZFrame("".getBytes());
				if (!content.send(socket, 0)) {
					frame.destroy();
					destroy();
					return false;
				}
				break;
		}

		//  Destroy GridMessage object
		destroy();
		return true;
	}

	/**
	 * Send the CONNECT to the socket in one step.
	 */
	public static void sendConnect(Socket output,
			int sequence,
			String ip,
			int port,
			Collection<String> clusters,
			int status,
			Map<String, String> headers) {
		GridMessage self = new GridMessage(GridMessage.CONNECT);
		self.setSequence(sequence);
		self.setIp(ip);
		self.setPort(port);
		self.setClusters(new ArrayList<String>(clusters));
		self.setStatus(status);
		self.setHeaders(new HashMap<String, String>(headers));
		self.send(output); 
	}

	/**
	 * Send the WHISPER to the socket in one step.
	 */
	public static void sendWhisper(Socket output,
			int sequence,
			ZFrame content) {
		GridMessage self = new GridMessage(GridMessage.WHISPER);
		self.setSequence(sequence);
		self.setContent(content.duplicate());
		self.send(output); 
	}

	/**
	 * Send the BROADCAST to the socket in one step.
	 */
	public static void sendBroadcast(Socket output,
			int sequence,
			String cluster,
			ZFrame content) {
		GridMessage self = new GridMessage(GridMessage.BROADCAST);
		self.setSequence(sequence);
		self.setCluster(cluster);
		self.setContent(content.duplicate());
		self.send(output); 
	}

	/**
	 * Send the JOIN to the socket in one step.
	 */
	public static void sendJoin(Socket output,
			int sequence,
			String cluster,
			int status) {
		GridMessage self = new GridMessage(GridMessage.JOIN);
		self.setSequence(sequence);
		self.setCluster(cluster);
		self.setStatus(status);
		self.send(output); 
	}

	/**
	 * Send the EXIT to the socket in one step.
	 */
	public static void sendExit(Socket output,
			int sequence,
			String cluster,
			int status) {
		GridMessage self = new GridMessage(GridMessage.EXIT);
		self.setSequence(sequence);
		self.setCluster(cluster);
		self.setStatus(status);
		self.send(output); 
	}

	/**
	 * Send the PING to the socket in one step.
	 */
	public static void sendPing(Socket output,
			int sequence) {
		GridMessage self = new GridMessage(GridMessage.PING);
		self.setSequence(sequence);
		self.send(output); 
	}

	/**
	 * Send the ECHO to the socket in one step.
	 */
	public static void sendEcho(Socket output,
			int sequence) {
		GridMessage self = new GridMessage(GridMessage.ECHO);
		self.setSequence(sequence);
		self.send(output); 
	}

	/**
	 * Duplicate the GridMessage message.
	 * 
	 * @param self The instance of GridMessage to duplicate
	 */
	@Override
	public GridMessage clone() {
		GridMessage self = this;
		GridMessage copy = new GridMessage(self.id);
		if (self.address != null)
			copy.address = self.address.duplicate();
		switch (self.id) {
			case CONNECT:
				copy.sequence = self.sequence;
				copy.ip = self.ip;
				copy.port = self.port;
				copy.clusters = new ArrayList<String>(self.clusters);
				copy.status = self.status;
				copy.headers = new HashMap<String, String>(self.headers);
				break;
			case WHISPER:
				copy.sequence = self.sequence;
				copy.content = self.content.duplicate();
				break;
			case BROADCAST:
				copy.sequence = self.sequence;
				copy.cluster = self.cluster;
				copy.content = self.content.duplicate();
				break;
			case JOIN:
				copy.sequence = self.sequence;
				copy.cluster = self.cluster;
				copy.status = self.status;
				break;
			case EXIT:
				copy.sequence = self.sequence;
				copy.cluster = self.cluster;
				copy.status = self.status;
				break;
			case PING:
				copy.sequence = self.sequence;
				break;
			case ECHO:
				copy.sequence = self.sequence;
				break;
		}
		return copy;
	}

	/**
	 * Dump headers key=value pair to stdout.
	 * 
	 * @param entry The entry to dump
	 * @param self The GridMessage instance
	 */
	public static void headersDump(Map.Entry<String, String> entry, GridMessage self) {
		System.out.printf("		%s=%s\n", entry.getKey(), entry.getValue());
	}

	/**
	 * Print contents of message to stdout.
	 */
	@SuppressWarnings("boxing")
	public void dump() {
		switch (id) {
			case CONNECT:
				System.out.println("CONNECT:");
				System.out.printf("	sequence=%d\n", sequence);
				if (ip != null)
					System.out.printf("	ip='%s'\n", ip);
				else
					System.out.printf("	ip=\n");
				System.out.printf("	port=%d\n", port);
				System.out.printf("	clusters={");
				if (clusters != null) {
					for (String value : clusters) {
						System.out.printf(" '%s'", value);
					}
				}
				System.out.printf(" }\n");
				System.out.printf("	status=%d\n", status);
				System.out.printf("	headers={\n");
				if (headers != null) {
					for (Map.Entry<String, String> entry : headers.entrySet())
						headersDump(entry, this);
				}
				System.out.printf("	}\n");
				break;
			
			case WHISPER:
				System.out.println("WHISPER:");
				System.out.printf("	sequence=%d\n", sequence);
				System.out.printf("	content={\n");
				if (content != null) {
					int size = content.size();
					byte[] data = content.getData();
					System.out.printf("		size=%d\n", content.size());
					if (size > 32)
						size = 32;
					int contentIndex;
					for (contentIndex = 0; contentIndex < size; contentIndex++) {
						if (contentIndex != 0 &&(contentIndex % 4 == 0))
							System.out.printf("-");
						System.out.printf("%02X", data[contentIndex]);
					}
				}
				System.out.printf("	}\n");
				break;
			
			case BROADCAST:
				System.out.println("BROADCAST:");
				System.out.printf("	sequence=%d\n", sequence);
				if (cluster != null)
					System.out.printf("	cluster='%s'\n", cluster);
				else
					System.out.printf("	cluster=\n");
				System.out.printf("	content={\n");
				if (content != null) {
					int size = content.size();
					byte[] data = content.getData();
					System.out.printf("		size=%d\n", content.size());
					if (size > 32)
						size = 32;
					int contentIndex;
					for (contentIndex = 0; contentIndex < size; contentIndex++) {
						if (contentIndex != 0 &&(contentIndex % 4 == 0))
							System.out.printf("-");
						System.out.printf("%02X", data[contentIndex]);
					}
				}
				System.out.printf("	}\n");
				break;
			
			case JOIN:
				System.out.println("JOIN:");
				System.out.printf("	sequence=%d\n", sequence);
				if (cluster != null)
					System.out.printf("	cluster='%s'\n", cluster);
				else
					System.out.printf("	cluster=\n");
				System.out.printf("	status=%d\n", status);
				break;
			
			case EXIT:
				System.out.println("EXIT:");
				System.out.printf("	sequence=%d\n", sequence);
				if (cluster != null)
					System.out.printf("	cluster='%s'\n", cluster);
				else
					System.out.printf("	cluster=\n");
				System.out.printf("	status=%d\n", status);
				break;
			
			case PING:
				System.out.println("PING:");
				System.out.printf("	sequence=%d\n", sequence);
				break;
			
			case ECHO:
				System.out.println("ECHO:");
				System.out.printf("	sequence=%d\n", sequence);
				break;
			
		}
	}

	/**
	 * Get the message address.
	 * 
	 * @return The message address frame
	 */
	public ZFrame getAddress() {
		return address;
	}

	/**
	 * Set the message address.
	 * 
	 * @param address The new message address
	 */
	public void setAddress(ZFrame address) {
		if (this.address != null)
			this.address.destroy();
		this.address = address.duplicate();
	}

	/**
	 * Get the GridMessage id.
	 * 
	 * @return The GridMessage id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the GridMessage id.
	 * 
	 * @param id The new message id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the sequence field.
	 * 
	 * @return The sequence field
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * Set the sequence field.
	 * 
	 * @param sequence The sequence field
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * Get the ip field.
	 * 
	 * @return The ip field
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Set the ip field.
	 * 
	 * @param ip The ip field
	 */
	public void setIp(String format, Object... args) {
		//  Format into newly allocated string
		ip = String.format(format, args);
	}

	/**
	 * Get the port field.
	 * 
	 * @return The port field
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the port field.
	 * 
	 * @param port The port field
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get the list of clusters strings.
	 * 
	 * @return The clusters strings
	 */
	public List<String> getClusters() {
		return clusters;
	}

	/**
	 * Iterate through the clusters field, and append a clusters value.
	 * 
	 * @param format The string format
	 * @param args The arguments used to build the string
	 */
	public void addCluster(String format, Object... args) {
		//  Format into newly allocated string
		String string = String.format(format, args);

		//  Attach string to list
		if (clusters == null)
			clusters = new ArrayList<String>();
		clusters.add(string);
	}

	/**
	 * Set the list of clusters strings.
	 * 
	 * @param value The collection of strings
	 */
	public void setClusters(Collection<String> value) {
		clusters = new ArrayList<String>(value);
	}

	/**
	 * Get the status field.
	 * 
	 * @return The status field
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the status field.
	 * 
	 * @param status The status field
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Get the the headers dictionary.
	 * 
	 * @return The headers dictionary
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Get a value in the headers dictionary as a string.
	 * 
	 * @param key The dictionary key
	 * @param defaultValue the default value if the key does not exist
	 */
	public String getHeaderString(String key, String defaultValue) {
		String value = null;
		if (headers != null)
			value = headers.get(key);
		if (value == null)
			value = defaultValue;

		return value;
	}

	/**
	 * Get a value in the headers dictionary as a long.
	 * 
	 * @param key The dictionary key
	 * @param defaultValue the default value if the key does not exist
	 */
	public long getHeaderNumber(String key, long defaultValue) {
		long value = defaultValue;
		String string = null;
		if (headers != null)
			string = headers.get(key);
		if (string != null)
			value = Long.parseLong(string);

		return value;
	}

	/**
	 * Set a value in the headers dictionary.
	 * 
	 * @param key The dictionary key
	 * @param format The string format
	 * @param args The arguments used to build the string
	 */
	public void putHeader(String key, String format, Object... args) {
		//  Format string into buffer
		String string = String.format(format, args);

		//  Store string in hash table
		if (headers == null)
			headers = new HashMap<String, String>();
		headers.put(key, string);
		headersBytes += key.length() + 1 + string.length();
	}

	/**
	 * Set the headers dictionary.
	 * 
	 * @param value The new headers dictionary
	 */
	public void setHeaders(Map<String, String> value) {
		if (value != null)
			headers = new HashMap<String, String>(value); 
		else
			headers = value;
	}

	/**
	 * Get the content field.
	 * 
	 * @return The content field
	 */
	public ZFrame getContent() {
		return content;
	}

	/**
	 * Set the content field, and takes ownership of supplied frame.
	 * 
	 * @param frame The new content frame
	 */
	public void setContent(ZFrame frame) {
		if (content != null)
			content.destroy();
		content = frame;
	}

	/**
	 * Get the cluster field.
	 * 
	 * @return The cluster field
	 */
	public String getCluster() {
		return cluster;
	}

	/**
	 * Set the cluster field.
	 * 
	 * @param cluster The cluster field
	 */
	public void setCluster(String format, Object... args) {
		//  Format into newly allocated string
		cluster = String.format(format, args);
	}
}


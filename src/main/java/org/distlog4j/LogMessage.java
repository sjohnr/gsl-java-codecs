/* ============================================================================
 * LogMessage.java
 * 
 * Generated codec class for LogMessage
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
package org.distlog4j;

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
 * LogMessage codec.
 * 
 * The specification for this class is as follows:
 * <pre class="text">
 *  LOG - Log message containing information about a log, including originating host, file, etc.
 *	  sequence		number 4
 *	  headers		dictionary
 *	  ip		string
 *	  port		number 2
 *	  file_name		string
 *	  line_num		number 4
 *	  message		string
 * </pre>
 * 
 * @author sriesenberg
 */
public class LogMessage implements Cloneable {
	public static final int LOG_MESSAGE_VERSION	= 1;
	
	public static final int LOG			= 1;
	
	//  Structure of our class
	private ZFrame address;		//  Address of peer if any
	private int id;				//  LogMessage message ID
	private ByteBuffer needle;	//  Read/write pointer for serialization
	private long sequence;
	private Map<String, String> headers;
	private int headersBytes;
	private String ip;
	private int port;
	private String file_name;
	private long line_num;
	private String message;
	
	/**
	 * Create a new LogMessage.
	 * 
	 * @param id The Message ID
	 */
	public LogMessage(int id) {
		this.id = id;
	}
	
	/**
	 * Destroy the LogMessage.
	 */
	public void destroy() {
		//  Free class properties
		if (address != null)
			address.destroy();
		address = null;

        //  Destroy frame fields
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
	 * Receive and parse a LogMessage from the socket. Returns new object or
	 * null if error. Will block if there's no message waiting.
	 * 
	 * @param input The socket used to receive this LogMessage
	 */
	public static LogMessage receive(Socket input) {
		assert (input != null);
		LogMessage self = new LogMessage(0);
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
				case LOG:
				{
					self.sequence = self.getNumber4();
					int headersHashSize = self.getNumber1();
					self.headers = new HashMap<String, String>();
					while (headersHashSize-- > 0) {
						String string = self.getString();
						String[] kv = string.split("=");
						self.headers.put(kv[0], kv[1]);
					}

					self.ip = self.getString();
					self.port = self.getNumber2();
					self.file_name = self.getString();
					self.line_num = self.getNumber4();
					self.message = self.getString();
					break;
                }

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
	private static void headersCount(final Map.Entry<String, String> entry, LogMessage self) {
		self.headersBytes += entry.getKey().length() + 1 + entry.getValue().length() + 1;
	}

	//  Serialize headers key=value pair
	private static void headersWrite(final Map.Entry<String, String> entry, LogMessage self) {
		String string = entry.getKey() + "=" + entry.getValue();
		self.putString(string);
	}

	/**
	 * Send the LogMessage to the socket, and destroy it.
	 * 
	 * @param socket The socket used to send this LogMessage
	 */
	public boolean send(Socket socket) {
		assert (socket != null);

		//  Calculate size of serialized data
		int frameSize = 2 + 1;		  //  Signature and message ID
		switch (id) {
			case LOG:
				//  sequence is a 4-byte integer
				frameSize += 4;
				//  headers is an array of key=value strings
				frameSize++;		//  Size is one octet
				if (headers != null) {
					headersBytes = 0;
					for (Map.Entry<String, String> entry: headers.entrySet()) {
						headersCount(entry, this);
					}
					frameSize += headersBytes;
				}
				//  ip is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (ip != null)
					frameSize += ip.length();
				//  port is a 2-byte integer
				frameSize += 2;
				//  file_name is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (file_name != null)
					frameSize += file_name.length();
				//  line_num is a 4-byte integer
				frameSize += 4;
				//  message is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (message != null)
					frameSize += message.length();
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
			case LOG:
				putNumber4(sequence);
				if (headers != null) {
					putNumber1((byte) headers.size());
					for (Map.Entry<String, String> entry: headers.entrySet()) {
						headersWrite(entry, this);
					}
				}
				else
					putNumber1((byte) 0);	  //  Empty dictionary
				if (ip != null)
					putString(ip);
				else
					putNumber1((byte) 0);	  //  Empty string
				putNumber2(port);
				if (file_name != null)
					putString(file_name);
				else
					putNumber1((byte) 0);	  //  Empty string
				putNumber4(line_num);
				if (message != null)
					putString(message);
				else
					putNumber1((byte) 0);	  //  Empty string
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
		}

		//  Destroy LogMessage object
		destroy();
		return true;
	}

	/**
	 * Send the LOG to the socket in one step.
	 */
	public static void sendLog(Socket output,
			long sequence,
			Map<String, String> headers,
			String ip,
			int port,
			String file_name,
			long line_num,
			String message) {
		LogMessage self = new LogMessage(LogMessage.LOG);
		self.setSequence(sequence);
		self.setHeaders(new HashMap<String, String>(headers));
		self.setIp(ip);
		self.setPort(port);
		self.setFile_Name(file_name);
		self.setLine_Num(line_num);
		self.setMessage(message);
		self.send(output); 
	}

	/**
	 * Duplicate the LogMessage message.
	 * 
	 * @param self The instance of LogMessage to duplicate
	 */
	@Override
	public LogMessage clone() {
		LogMessage self = this;
		LogMessage copy = new LogMessage(self.id);
		if (self.address != null)
			copy.address = self.address.duplicate();
		switch (self.id) {
			case LOG:
				copy.sequence = self.sequence;
				copy.headers = new HashMap<String, String>(self.headers);
				copy.ip = self.ip;
				copy.port = self.port;
				copy.file_name = self.file_name;
				copy.line_num = self.line_num;
				copy.message = self.message;
				break;
		}
		return copy;
	}

	/**
	 * Dump headers key=value pair to stdout.
	 * 
	 * @param entry The entry to dump
	 * @param self The LogMessage instance
	 */
	public static void headersDump(Map.Entry<String, String> entry, LogMessage self) {
		System.out.printf("		%s=%s\n", entry.getKey(), entry.getValue());
	}

	/**
	 * Print contents of message to stdout.
	 */
	@SuppressWarnings("boxing")
	public void dump() {
		switch (id) {
			case LOG:
				System.out.println("LOG:");
				System.out.printf("	sequence=%d\n", sequence);
				System.out.printf("	headers={\n");
				if (headers != null) {
					for (Map.Entry<String, String> entry : headers.entrySet())
						headersDump(entry, this);
				}
				System.out.printf("	}\n");
				if (ip != null)
					System.out.printf("	ip='%s'\n", ip);
				else
					System.out.printf("	ip=\n");
				System.out.printf("	port=%d\n", port);
				if (file_name != null)
					System.out.printf("	file_name='%s'\n", file_name);
				else
					System.out.printf("	file_name=\n");
				System.out.printf("	line_num=%d\n", line_num);
				if (message != null)
					System.out.printf("	message='%s'\n", message);
				else
					System.out.printf("	message=\n");
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
	 * Get the LogMessage id.
	 * 
	 * @return The LogMessage id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the LogMessage id.
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
	public long getSequence() {
		return sequence;
	}

	/**
	 * Set the sequence field.
	 * 
	 * @param sequence The sequence field
	 */
	public void setSequence(long sequence) {
		this.sequence = sequence;
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
	 * Get the file_name field.
	 * 
	 * @return The file_name field
	 */
	public String getFile_Name() {
		return file_name;
	}

	/**
	 * Set the file_name field.
	 * 
	 * @param file_name The file_name field
	 */
	public void setFile_Name(String format, Object... args) {
		//  Format into newly allocated string
		file_name = String.format(format, args);
	}

	/**
	 * Get the line_num field.
	 * 
	 * @return The line_num field
	 */
	public long getLine_Num() {
		return line_num;
	}

	/**
	 * Set the line_num field.
	 * 
	 * @param line_num The line_num field
	 */
	public void setLine_Num(long line_num) {
		this.line_num = line_num;
	}

	/**
	 * Get the message field.
	 * 
	 * @return The message field
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message field.
	 * 
	 * @param message The message field
	 */
	public void setMessage(String format, Object... args) {
		//  Format into newly allocated string
		message = String.format(format, args);
	}
}


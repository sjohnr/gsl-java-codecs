/* ============================================================================
 * GridLogMessage.java
 * 
 * Generated codec class for GridLogMessage
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
 * GridLogMessage codec.
 * 
 * The specification for this class is as follows:
 * <pre class="text">
 *  LOG - Log an event.
 *	  level		number 1
 *	  event		number 1
 *	  node		number 2
 *	  peer		number 2
 *	  time		number 8
 *	  message		string
 * </pre>
 * 
 * @author sriesenberg
 */
public class GridLogMessage implements Cloneable {
	public static final int GRID_LOG_MESSAGE_VERSION	= 1;
	public static final int GRID_LOG_MESSAGE_LEVEL_ERROR	= 1;
	public static final int GRID_LOG_MESSAGE_LEVEL_WARN	= 2;
	public static final int GRID_LOG_MESSAGE_LEVEL_INFO	= 3;
	public static final int GRID_LOG_MESSAGE_LEVEL_DEBUG	= 4;
	public static final int GRID_LOG_MESSAGE_LEVEL_TRACE	= 5;
	public static final int GRID_LOG_MESSAGE_EVENT_JOIN	= 1;
	public static final int GRID_LOG_MESSAGE_EVENT_EXIT	= 2;
	public static final int GRID_LOG_MESSAGE_EVENT_CONNECT	= 3;
	public static final int GRID_LOG_MESSAGE_EVENT_TIMEOUT	= 4;
	
	public static final int LOG			= 1;
	
	//  Structure of our class
	private ZFrame address;		//  Address of peer if any
	private int id;				//  GridLogMessage message ID
	private ByteBuffer needle;	//  Read/write pointer for serialization
	private int level;
	private int event;
	private int node;
	private int peer;
	private long time;
	private String message;
	
	/**
	 * Create a new GridLogMessage.
	 * 
	 * @param id The Message ID
	 */
	public GridLogMessage(int id) {
		this.id = id;
	}
	
	/**
	 * Destroy the GridLogMessage.
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
	 * Receive and parse a GridLogMessage from the socket. Returns new object or
	 * null if error. Will block if there's no message waiting.
	 * 
	 * @param input The socket used to receive this GridLogMessage
	 */
	public static GridLogMessage receive(Socket input) {
		assert (input != null);
		GridLogMessage self = new GridLogMessage(0);
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
				if (signature ==(0xAAA0 | 2))
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
					self.level = self.getNumber1();
					self.event = self.getNumber1();
					self.node = self.getNumber2();
					self.peer = self.getNumber2();
					self.time = self.getNumber8();
					self.message = self.getString();
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


	/**
	 * Send the GridLogMessage to the socket, and destroy it.
	 * 
	 * @param socket The socket used to send this GridLogMessage
	 */
	public boolean send(Socket socket) {
		assert (socket != null);

		//  Calculate size of serialized data
		int frameSize = 2 + 1;		  //  Signature and message ID
		switch (id) {
			case LOG:
				//  level is a 1-byte integer
				frameSize += 1;
				//  event is a 1-byte integer
				frameSize += 1;
				//  node is a 2-byte integer
				frameSize += 2;
				//  peer is a 2-byte integer
				frameSize += 2;
				//  time is a 8-byte integer
				frameSize += 8;
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
		putNumber2(0xAAA0 | 2);
		putNumber1((byte) id);

		switch (id) {
			case LOG:
				putNumber1(level);
				putNumber1(event);
				putNumber2(node);
				putNumber2(peer);
				putNumber8(time);
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

		//  Destroy GridLogMessage object
		destroy();
		return true;
	}

	/**
	 * Send the LOG to the socket in one step.
	 */
	public static void sendLog(Socket output,
			int level,
			int event,
			int node,
			int peer,
			long time,
			String message) {
		GridLogMessage self = new GridLogMessage(GridLogMessage.LOG);
		self.setLevel(level);
		self.setEvent(event);
		self.setNode(node);
		self.setPeer(peer);
		self.setTime(time);
		self.setMessage(message);
		self.send(output); 
	}

	/**
	 * Duplicate the GridLogMessage message.
	 * 
	 * @param self The instance of GridLogMessage to duplicate
	 */
	@Override
	public GridLogMessage clone() {
		GridLogMessage self = this;
		GridLogMessage copy = new GridLogMessage(self.id);
		if (self.address != null)
			copy.address = self.address.duplicate();
		switch (self.id) {
			case LOG:
				copy.level = self.level;
				copy.event = self.event;
				copy.node = self.node;
				copy.peer = self.peer;
				copy.time = self.time;
				copy.message = self.message;
				break;
		}
		return copy;
	}


	/**
	 * Print contents of message to stdout.
	 */
	@SuppressWarnings("boxing")
	public void dump() {
		switch (id) {
			case LOG:
				System.out.println("LOG:");
				System.out.printf("	level=%d\n", level);
				System.out.printf("	event=%d\n", event);
				System.out.printf("	node=%d\n", node);
				System.out.printf("	peer=%d\n", peer);
				System.out.printf("	time=%d\n", time);
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
	 * Get the GridLogMessage id.
	 * 
	 * @return The GridLogMessage id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the GridLogMessage id.
	 * 
	 * @param id The new message id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the level field.
	 * 
	 * @return The level field
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Set the level field.
	 * 
	 * @param level The level field
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Get the event field.
	 * 
	 * @return The event field
	 */
	public int getEvent() {
		return event;
	}

	/**
	 * Set the event field.
	 * 
	 * @param event The event field
	 */
	public void setEvent(int event) {
		this.event = event;
	}

	/**
	 * Get the node field.
	 * 
	 * @return The node field
	 */
	public int getNode() {
		return node;
	}

	/**
	 * Set the node field.
	 * 
	 * @param node The node field
	 */
	public void setNode(int node) {
		this.node = node;
	}

	/**
	 * Get the peer field.
	 * 
	 * @return The peer field
	 */
	public int getPeer() {
		return peer;
	}

	/**
	 * Set the peer field.
	 * 
	 * @param peer The peer field
	 */
	public void setPeer(int peer) {
		this.peer = peer;
	}

	/**
	 * Get the time field.
	 * 
	 * @return The time field
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Set the time field.
	 * 
	 * @param time The time field
	 */
	public void setTime(long time) {
		this.time = time;
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


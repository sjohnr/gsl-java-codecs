/* ============================================================================
 * ZreLogMsg.java
 * 
 * Generated codec class for ZreLogMsg
 * ----------------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy  
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights  
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     
 * copies of the Software, and to permit persons to whom the Software is         
 * furnished to do so, subject to the following conditions:                      
 *                                                                               
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.                               
 *                                                                               
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.                                                                     
 * ============================================================================
 */
package org.zyre;

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
 * ZreLogMsg codec.
 * 
 * The specification for this class is as follows:
 * <pre class="text">
 *  LOG - Log an event
 *	  level		number 1
 *	  event		number 1
 *	  node		number 2
 *	  peer		number 2
 *	  time		number 8
 *	  data		string
 * </pre>
 * 
 * @author sriesenberg
 */
public class ZreLogMsg {
	public static final int ZRE_LOG_MSG_VERSION	= 1;
	public static final int ZRE_LOG_MSG_LEVEL_ERROR	= 1;
	public static final int ZRE_LOG_MSG_LEVEL_WARNING	= 2;
	public static final int ZRE_LOG_MSG_LEVEL_INFO	= 3;
	public static final int ZRE_LOG_MSG_EVENT_JOIN	= 1;
	public static final int ZRE_LOG_MSG_EVENT_LEAVE	= 2;
	public static final int ZRE_LOG_MSG_EVENT_ENTER	= 3;
	public static final int ZRE_LOG_MSG_EVENT_EXIT	= 4;
	
	public static final int LOG			= 1;
	
	//  Structure of our class
	private ZFrame address;		//  Address of peer if any
	private int id;				//  ZreLogMsg message ID
	private ByteBuffer needle;	//  Read/write pointer for serialization
	private int level;
	private int event;
	private int node;
	private int peer;
	private long time;
	private String data;
	
	/**
	 * Create a new ZreLogMsg.
	 * 
	 * @param id The Message ID
	 */
	public ZreLogMsg(int id) {
		this.id = id;
	}
	
	/**
	 * Destroy the ZreLogMsg.
	 */
	public void destroy() {
		//  Free class properties
		if (address != null)
			address.destroy();
		address = null;
	}
	
	//  --------------------------------------------------------------------------
	//  Network data encoding macros

	//  Put a 1-byte number to the frame
	private final void putNumber1(int value) {
		needle.put((byte) value);
	}

	//  Get a 1-byte number to the frame
	//  then make it unsigned
	private int getNumber1() { 
		int value = needle.get(); 
		if (value < 0)
			value =(0xff) & value;
		return value;
	}

	//  Put a 2-byte number to the frame
	private final void putNumber2(int value) {
		needle.putShort((short) value);
	}

	//  Get a 2-byte number to the frame
	private int getNumber2() { 
		int value = needle.getShort(); 
		if (value < 0)
			value =(0xffff) & value;
		return value;
	}

	//  Put a 4-byte number to the frame
	private final void putNumber4(long value)  {
		needle.putInt((int) value);
	}

	//  Get a 4-byte number to the frame
	//  then make it unsigned
	private long getNumber4()  { 
		long value = needle.getInt(); 
		if (value < 0)
			value =(0xffffffff) & value;
		return value;
	}

	//  Put a 8-byte number to the frame
	public void putNumber8(long value)  {
		needle.putLong(value);
	}

	//  Get a 8-byte number to the frame
	public long getNumber8()  {
		return needle.getLong();
	}


	//  Put a block to the frame
	private void putBlock(byte[] value, int size)  {
		needle.put(value, 0, size);
	}

	private byte[] getBlock(int size)  {
		byte[] value = new byte[size]; 
		needle.get(value);

		return value;
	}

	//  Put a string to the frame
	public void putString(String value)  {
		needle.put((byte) value.length());
		needle.put(value.getBytes());
	}

	//  Get a string from the frame
	public String getString()  {
		int size = getNumber1();
		byte[] value = new byte[size];
		needle.get(value);

		return new String(value);
	}

	/**
	 * Receive and parse a ZreLogMsg from the socket. Returns new object or
	 * null if error. Will block if there's no message waiting.
	 * 
	 * @param input The socket used to receive this ZreLogMsg
	 */
	public static ZreLogMsg recv(Socket input) {
		assert (input != null);
		ZreLogMsg self = new ZreLogMsg(0);
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
			int listSize;
			int hashSize;

			switch (self.id) {
				case LOG:
					self.level = self.getNumber1();
					self.event = self.getNumber1();
					self.node = self.getNumber2();
					self.peer = self.getNumber2();
					self.time = self.getNumber8();
					self.data = self.getString();
					break;

				default:
					throw new IllegalArgumentException();
			}

			return self;
		} catch (Exception e) {
			//  Error returns
			System.out.printf("E: malformed message '%d'\n", self.id);
			self.destroy();
			return null;
		} finally {
			if (frame != null)
				frame.destroy();
		}
	}


	/**
	 * Send the ZreLogMsg to the socket, and destroy it.
	 * 
	 * @param socket The socket used to send this ZreLogMsg
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
				//  data is a string with 1-byte length
				frameSize++;		//  Size is one octet
				if (data != null)
					frameSize += data.length();
				break;
				
			default:
				System.out.printf("E: bad message type '%d', not sent\n", id);
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
				if (data != null)
					putString(data);
				else
					putNumber1((byte) 0);	  //  Empty string
				break;
			
		}

		//  If we're sending to a ROUTER, we send the address first
		if (socket.getType() == ZMQ.ROUTER) {
			assert (address != null);
			if (!address.sendAndDestroy(socket, ZMQ.SNDMORE)) {
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

		//  Destroy ZreLogMsg object
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
			String data) {
		ZreLogMsg self = new ZreLogMsg(ZreLogMsg.LOG);
		self.setLevel(level);
		self.setEvent(event);
		self.setNode(node);
		self.setPeer(peer);
		self.setTime(time);
		self.setData(data);
		self.send(output); 
	}

	/**
	 * Duplicate the ZreLogMsg message.
	 * 
	 * @param self The instance of ZreLogMsg to duplicate
	 */
	public ZreLogMsg dup(ZreLogMsg self) {
		if (self == null)
			return null;

		ZreLogMsg copy = new ZreLogMsg(self.id);
		if (self.address != null)
			copy.address = self.address.duplicate();
		switch (self.id) {
			case LOG:
				copy.level = self.level;
				copy.event = self.event;
				copy.node = self.node;
				copy.peer = self.peer;
				copy.time = self.time;
				copy.data = self.data;
				break;
		}
		return copy;
	}


	/**
	 * Print contents of message to stdout.
	 */
	public void dump() {
		switch (id) {
			case LOG:
				System.out.println("LOG:");
				System.out.printf("	level=%d\n",(long)level);
				System.out.printf("	event=%d\n",(long)event);
				System.out.printf("	node=%d\n",(long)node);
				System.out.printf("	peer=%d\n",(long)peer);
				System.out.printf("	time=%d\n",(long)time);
				if (data != null)
					System.out.printf("	data='%s'\n", data);
				else
					System.out.printf("	data=\n");
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
	 * Get the ZreLogMsg id.
	 * 
	 * @return The ZreLogMsg id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the ZreLogMsg id.
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
	 * Get the data field.
	 * 
	 * @return The data field
	 */
	public String getData() {
		return data;
	}

	/**
	 * Set the data field.
	 * 
	 * @param data The data field
	 */
	public void setData(String format, Object... args) {
		//  Format into newly allocated string
		data = String.format(format, args);
	}
}


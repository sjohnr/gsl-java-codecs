/* ============================================================================
 * LogSocket.java
 * 
 * Generated codec class for LogSocket
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

import java.util.*;
import java.io.Closeable;
import java.nio.ByteBuffer;

import org.zeromq.api.*;
import org.zeromq.api.Message.Frame;
import org.zeromq.ZMQ;

/**
 * LogSocket codec.
 * 
 * The specification for this class is as follows:
 * <pre class="text">
 *  LOG - Log message containing information about a log, including originating host, file, etc.
 *    sequence                     number 4
 *    headers                      dictionary
 *    ip                           string
 *    port                         number 2
 *    fileName                     string
 *    lineNum                      number 4
 *    message                      string
 *  REQUEST - Request for a replay of messages between start and end line_num values.
 *    sequence                     number 4
 *    fileName                     string
 *    start                        number 4
 *    end                          number 4
 *  REPLY - Reply containing the requested sequence of replay log messages.
 *    sequence                     number 4
 *    headers                      dictionary
 *    messages                     strings
 * </pre>
 * 
 * @author sriesenberg
 */
public class LogSocket implements Closeable {
    //  Protocol constants
    public static final int LOG_VERSION                     = 1;

    //  Enumeration of message types
    public static enum MessageType {
        LOG,
        REQUEST,
        REPLY
    }

    //  Structure of our class
    private Socket socket;     //  Internal socket handle
    private Frame address;     //  Address of peer if any
    private ByteBuffer needle; //  Read/write pointer for serialization

    private LogMessage log;
    private RequestMessage request;
    private ReplyMessage reply;

    /**
     * Create a new LogSocket.
     * 
     * @param socket The internal socket
     */
    public LogSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Destroy the LogSocket.
     */
    @Override
    public void close() {
        socket.close();
    }

    //  --------------------------------------------------------------------------
    //  Network data encoding macros

    //  Put a 1-byte number to the frame
    protected final void putNumber1(int value) {
        needle.put((byte) value);
    }

    //  Get a 1-byte number from the frame
    //  then make it unsigned
    protected final int getNumber1() { 
        int value = needle.get(); 
        if (value < 0)
            value = (0xff) & value;
        return value;
    }

    //  Put a 2-byte number to the frame
    protected final void putNumber2(int value) {
        needle.putShort((short) value);
    }

    //  Get a 2-byte number from the frame
    protected final int getNumber2() { 
        int value = needle.getShort(); 
        if (value < 0)
            value = (0xffff) & value;
        return value;
    }

    //  Put a 4-byte number to the frame
    protected final void putNumber4(long value)  {
        needle.putInt((int) value);
    }

    //  Get a 4-byte number from the frame
    //  then make it unsigned
    protected final long getNumber4()  { 
        long value = needle.getInt(); 
        if (value < 0)
            value = (0xffffffff) & value;
        return value;
    }

    //  Put a 8-byte number to the frame
    protected final void putNumber8(long value)  {
        needle.putLong(value);
    }

    //  Get a 8-byte number from the frame
    protected final long getNumber8()  {
        return needle.getLong();
    }

    //  Put a block to the frame
    protected final void putBlock(byte[] value, int size)  {
        needle.put(value, 0, size);
    }

    //  Get a block from the frame
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

    //  Put a dictionary to the frame
    protected final void writeDictionary(Map<String, String> dictionary) {
        putNumber1((byte) dictionary.size());
        for (Map.Entry<String, String> entry: dictionary.entrySet()) {
            putString(entry.getKey() + "=" + entry.getValue());
        }
    }

    //  Calculate the size of the dictionary in bytes
    protected final int countDictionary(Map<String, String> dictionary) {
        int nBytes = 0;
        for (Map.Entry<String, String> entry: dictionary.entrySet()) {
            nBytes += entry.getKey().length() + 1 + entry.getValue().length() + 1;
        }
        
        return nBytes;
    }

    /**
     * Get the message address.
     * 
     * @return The message address frame
     */
    public Frame getAddress() {
        return address;
    }

    /**
     * Set the message address.
     * 
     * @param address The new message address
     */
    public void setAddress(Frame address) {
        this.address = address;
    }

    /**
     * Receive a message on the socket.
     */
    public MessageType receive() {
        assert (socket != null);

        int id = 0;
        byte[] frame = null;
        try {
            //  Read valid message frame from socket; we loop over any
            //  garbage data we might receive from badly-connected peers
            while (true) {
                //  If we're reading from a ROUTER socket, get address
                if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
                    this.address = new Frame(socket.receive());
                    if (this.address == null)
                        return null;         //  Interrupted
                    if (!socket.hasMoreToReceive())
                        throw new IllegalArgumentException();
                }
                //  Read and parse command in frame
                frame = socket.receive();
                if (frame == null)
                    return null;             //  Interrupted

                //  Get and check protocol signature
                this.needle = ByteBuffer.wrap(frame); 
                int signature = getNumber2();
                if (signature ==(0xAAA0 | 1))
                    break;                //  Valid signature

                //  Protocol assertion, drop message
                while (socket.hasMoreToReceive()) {
                    socket.receive();
                }
            }

            //  Get message id, which is first byte in frame
            id = getNumber1();

            switch (id) {
                case 0:
                {
                    LogMessage message = new LogMessage();
                    this.log = message;
                    message.sequence = getNumber4();
                    int headersHashSize = getNumber1();
                    message.headers = new HashMap<String, String>();
                    while (headersHashSize-- > 0) {
                        String string = getString();
                        String[] kv = string.split("=");
                        message.headers.put(kv[0], kv[1]);
                    }

                    message.ip = getString();
                    message.port = getNumber2();
                    message.fileName = getString();
                    message.lineNum = getNumber4();
                    message.message = getString();
                    break;
                }

                case 1:
                {
                    RequestMessage message = new RequestMessage();
                    this.request = message;
                    message.sequence = getNumber4();
                    message.fileName = getString();
                    message.start = getNumber4();
                    message.end = getNumber4();
                    break;
                }

                case 2:
                {
                    ReplyMessage message = new ReplyMessage();
                    this.reply = message;
                    message.sequence = getNumber4();
                    int headersHashSize = getNumber1();
                    message.headers = new HashMap<String, String>();
                    while (headersHashSize-- > 0) {
                        String string = getString();
                        String[] kv = string.split("=");
                        message.headers.put(kv[0], kv[1]);
                    }

                    int messagesListSize = getNumber1();
                    message.messages = new ArrayList<String>();
                    while (messagesListSize-- > 0) {
                        String string = getString();
                        message.messages.add(string);
                    }
                    break;
                }

                default:
                    throw new IllegalArgumentException();
            }

            return MessageType.values()[id];
        } catch (Exception e) {
            //  Error returns
            System.out.printf("E: malformed message '%d'\n", Integer.valueOf(id));
            return null;
        }
    }

    /**
     * Get a LOG message from the socket.
     */
    public LogMessage getLog() {
        if (log == null) {
            throw new IllegalStateException("E: message not available");
        }

        try {
            return log;
        } finally {
            log = null;
        }
    }

    /**
     * Get a REQUEST message from the socket.
     */
    public RequestMessage getRequest() {
        if (request == null) {
            throw new IllegalStateException("E: message not available");
        }

        try {
            return request;
        } finally {
            request = null;
        }
    }

    /**
     * Get a REPLY message from the socket.
     */
    public ReplyMessage getReply() {
        if (reply == null) {
            throw new IllegalStateException("E: message not available");
        }

        try {
            return reply;
        } finally {
            reply = null;
        }
    }

    /**
     * Send the LOG to the socket in one step.
     */
    public boolean sendLog(LogMessage message) {
        //  Calculate size of serialized data
        int frameSize = 2 + 1;        //  Signature and message ID
        //  sequence is a 4-byte integer
        frameSize += 4;
        //  headers is an array of key=value strings
        frameSize++;        //  Size is one octet
        if (message.headers != null)
            frameSize += countDictionary(message.headers);
        //  ip is a string with 1-byte length
        frameSize++;        //  Size is one octet
        if (message.ip != null)
            frameSize += message.ip.length();
        //  port is a 2-byte integer
        frameSize += 2;
        //  fileName is a string with 1-byte length
        frameSize++;        //  Size is one octet
        if (message.fileName != null)
            frameSize += message.fileName.length();
        //  lineNum is a 4-byte integer
        frameSize += 4;
        //  message is a string with 1-byte length
        frameSize++;        //  Size is one octet
        if (message.message != null)
            frameSize += message.message.length();

        //  Now serialize message into the frame
        byte[] frame = new byte[frameSize];
        needle = ByteBuffer.wrap(frame); 
        MessageFlag frameFlag = MessageFlag.NONE;
        putNumber2(0xAAA0 | 1);
        putNumber1((byte) MessageType.LOG.ordinal());

        putNumber4(message.sequence);
        if (message.headers != null)
            writeDictionary(message.headers);
        else
            putNumber1((byte) 0);     //  Empty dictionary
        if (message.ip != null)
            putString(message.ip);
        else
            putNumber1((byte) 0);     //  Empty string
        putNumber2(message.port);
        if (message.fileName != null)
            putString(message.fileName);
        else
            putNumber1((byte) 0);     //  Empty string
        putNumber4(message.lineNum);
        if (message.message != null)
            putString(message.message);
        else
            putNumber1((byte) 0);     //  Empty string

        //  If we're sending to a ROUTER, we send the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            if (!socket.send(address.getData(), MessageFlag.SEND_MORE)) {
                return false;
            }
        }

        //  Now send the data frame
        if (!socket.send(frame, frameFlag)) {
            return false;
        }

        //  Now send any frame fields, in order

        return true;
    }

    /**
     * Send the REQUEST to the socket in one step.
     */
    public boolean sendRequest(RequestMessage message) {
        //  Calculate size of serialized data
        int frameSize = 2 + 1;        //  Signature and message ID
        //  sequence is a 4-byte integer
        frameSize += 4;
        //  fileName is a string with 1-byte length
        frameSize++;        //  Size is one octet
        if (message.fileName != null)
            frameSize += message.fileName.length();
        //  start is a 4-byte integer
        frameSize += 4;
        //  end is a 4-byte integer
        frameSize += 4;

        //  Now serialize message into the frame
        byte[] frame = new byte[frameSize];
        needle = ByteBuffer.wrap(frame); 
        MessageFlag frameFlag = MessageFlag.NONE;
        putNumber2(0xAAA0 | 1);
        putNumber1((byte) MessageType.REQUEST.ordinal());

        putNumber4(message.sequence);
        if (message.fileName != null)
            putString(message.fileName);
        else
            putNumber1((byte) 0);     //  Empty string
        putNumber4(message.start);
        putNumber4(message.end);

        //  If we're sending to a ROUTER, we send the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            if (!socket.send(address.getData(), MessageFlag.SEND_MORE)) {
                return false;
            }
        }

        //  Now send the data frame
        if (!socket.send(frame, frameFlag)) {
            return false;
        }

        //  Now send any frame fields, in order

        return true;
    }

    /**
     * Send the REPLY to the socket in one step.
     */
    public boolean sendReply(ReplyMessage message) {
        //  Calculate size of serialized data
        int frameSize = 2 + 1;        //  Signature and message ID
        //  sequence is a 4-byte integer
        frameSize += 4;
        //  headers is an array of key=value strings
        frameSize++;        //  Size is one octet
        if (message.headers != null)
            frameSize += countDictionary(message.headers);
        //  messages is an array of strings
        frameSize++;        //  Size is one octet
        if (message.messages != null)
            for (String value : message.messages) 
                frameSize += 1 + value.length();

        //  Now serialize message into the frame
        byte[] frame = new byte[frameSize];
        needle = ByteBuffer.wrap(frame); 
        MessageFlag frameFlag = MessageFlag.NONE;
        putNumber2(0xAAA0 | 1);
        putNumber1((byte) MessageType.REPLY.ordinal());

        putNumber4(message.sequence);
        if (message.headers != null)
            writeDictionary(message.headers);
        else
            putNumber1((byte) 0);     //  Empty dictionary
        if (message.messages != null) {
            putNumber1((byte) message.messages.size());
            for (String value : message.messages) {
                putString(value);
            }
        }
        else
            putNumber1((byte) 0);     //  Empty string array

        //  If we're sending to a ROUTER, we send the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            if (!socket.send(address.getData(), MessageFlag.SEND_MORE)) {
                return false;
            }
        }

        //  Now send the data frame
        if (!socket.send(frame, frameFlag)) {
            return false;
        }

        //  Now send any frame fields, in order

        return true;
    }
}


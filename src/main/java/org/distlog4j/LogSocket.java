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
import org.zeromq.api.Message.FrameBuilder;
import org.zeromq.ZMQ;

/**
 * LogSocket class.
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
 *  LOGS - Message containing information about a batch of logs, including originating host, file, etc.
 *    sequence                     number 4
 *    headers                      dictionary
 *    ip                           string
 *    port                         number 2
 *    fileName                     string
 *    lineNum                      number 4
 *    messages                     strings
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
    public static final int VERSION           = 1;

    //  Enumeration of message types
    public enum MessageType {
        LOG,
        LOGS,
        REQUEST,
        REPLY
    }

    //  Structure of our class
    private Socket socket;        //  Internal socket handle
    private Frame address;        //  Address of peer if any

    private LogMessage log;
    private LogsMessage logs;
    private RequestMessage request;
    private ReplyMessage reply;

    /**
     * Create a new LogSocket.
     * 
     * @param socket The internal socket
     */
    public LogSocket(Socket socket) {
        assert (socket != null);
        this.socket = socket;
    }

    /**
     * Destroy the LogSocket.
     */
    @Override
    public void close() {
        socket.close();
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
        int id = 0;
        Message frames;
        Frame needle;
        MessageType type;
        try {
            //  Read valid message frame from socket; we loop over any
            //  garbage data we might receive from badly-connected peers
            while (true) {
                frames = socket.receiveMessage();

                //  If we're reading from a ROUTER socket, get address
                if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
                    this.address = frames.popFrame();
                }

                //  Read and parse command in frame
                needle = frames.popFrame();

                //  Get and check protocol signature
                int signature = (0xffff) & needle.getShort();
                if (signature == (0xAAA0 | 1))
                    break;                //  Valid signature

                //  Protocol assertion, drop message
            }

            //  Get message id, which is first byte in frame
            id = (0xff) & needle.getByte();
            type = MessageType.values()[id-1];
            switch (type) {
                case LOG: {
                    LogMessage message = this.log = new LogMessage();
                    message.sequence = needle.getInt();
                    int headersHashSize = (0xff) & needle.getByte();
                    message.headers = new HashMap<>(headersHashSize);
                    while (headersHashSize-- > 0) {
                        String string = needle.getChars();
                        String[] kv = string.split("=");
                        message.headers.put(kv[0], kv[1]);
                    }
                    message.ip = needle.getChars();
                    message.port = (0xffff) & needle.getShort();
                    message.fileName = needle.getChars();
                    message.lineNum = needle.getInt();
                    message.message = needle.getChars();
                    break;
                }
                case LOGS: {
                    LogsMessage message = this.logs = new LogsMessage();
                    message.sequence = needle.getInt();
                    int headersHashSize = (0xff) & needle.getByte();
                    message.headers = new HashMap<>(headersHashSize);
                    while (headersHashSize-- > 0) {
                        String string = needle.getChars();
                        String[] kv = string.split("=");
                        message.headers.put(kv[0], kv[1]);
                    }
                    message.ip = needle.getChars();
                    message.port = (0xffff) & needle.getShort();
                    message.fileName = needle.getChars();
                    message.lineNum = needle.getInt();
                    int messagesListSize = (0xff) & needle.getByte();
                    message.messages = new ArrayList<>(messagesListSize);
                    while (messagesListSize-- > 0) {
                        message.messages.add(needle.getChars());
                    }
                    break;
                }
                case REQUEST: {
                    RequestMessage message = this.request = new RequestMessage();
                    message.sequence = needle.getInt();
                    message.fileName = needle.getChars();
                    message.start = needle.getInt();
                    message.end = needle.getInt();
                    break;
                }
                case REPLY: {
                    ReplyMessage message = this.reply = new ReplyMessage();
                    message.sequence = needle.getInt();
                    int headersHashSize = (0xff) & needle.getByte();
                    message.headers = new HashMap<>(headersHashSize);
                    while (headersHashSize-- > 0) {
                        String string = needle.getChars();
                        String[] kv = string.split("=");
                        message.headers.put(kv[0], kv[1]);
                    }
                    int messagesListSize = (0xff) & needle.getByte();
                    message.messages = new ArrayList<>(messagesListSize);
                    while (messagesListSize-- > 0) {
                        message.messages.add(needle.getChars());
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Invalid message: unrecognized type: " + type);
            }

            return type;
        } catch (Exception ex) {
            //  Error returns
            System.out.println("Malformed message: " + id);
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get a LOG message from the socket.
     */
    public LogMessage getLog() {
        return log;
    }

    /**
     * Get a LOGS message from the socket.
     */
    public LogsMessage getLogs() {
        return logs;
    }

    /**
     * Get a REQUEST message from the socket.
     */
    public RequestMessage getRequest() {
        return request;
    }

    /**
     * Get a REPLY message from the socket.
     */
    public ReplyMessage getReply() {
        return reply;
    }

    /**
     * Send the LOG to the socket in one step.
     */
    public boolean send(LogMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 1);       //  Message ID

        builder.putInt(message.sequence);
        if (message.headers != null) {
            builder.putByte((byte) message.headers.size());
            for (Map.Entry<String, String> entry: message.headers.entrySet()) {
                builder.putChars(entry.getKey() + "=" + entry.getValue());
            }
        } else {
            builder.putByte((byte) 0);   //  Empty dictionary
        }
        if (message.ip != null) {
            builder.putChars(message.ip);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putShort((short) (int) message.port);
        if (message.fileName != null) {
            builder.putChars(message.fileName);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putInt(message.lineNum);
        if (message.message != null) {
            builder.putChars(message.message);
        } else {
            builder.putChars("");        //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            frames.addFrame(address);
        }

        //  Now add the data frame
        frames.addFrame(builder.build());

        return socket.send(frames);
    }

    /**
     * Send the LOGS to the socket in one step.
     */
    public boolean send(LogsMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 2);       //  Message ID

        builder.putInt(message.sequence);
        if (message.headers != null) {
            builder.putByte((byte) message.headers.size());
            for (Map.Entry<String, String> entry: message.headers.entrySet()) {
                builder.putChars(entry.getKey() + "=" + entry.getValue());
            }
        } else {
            builder.putByte((byte) 0);   //  Empty dictionary
        }
        if (message.ip != null) {
            builder.putChars(message.ip);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putShort((short) (int) message.port);
        if (message.fileName != null) {
            builder.putChars(message.fileName);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putInt(message.lineNum);
        if (message.messages != null) {
            builder.putByte((byte) message.messages.size());
            for (String value : message.messages) {
                builder.putChars(value);
            }
        } else {
            builder.putByte((byte) 0);   //  Empty string array
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            frames.addFrame(address);
        }

        //  Now add the data frame
        frames.addFrame(builder.build());

        return socket.send(frames);
    }

    /**
     * Send the REQUEST to the socket in one step.
     */
    public boolean send(RequestMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 3);       //  Message ID

        builder.putInt(message.sequence);
        if (message.fileName != null) {
            builder.putChars(message.fileName);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putInt(message.start);
        builder.putInt(message.end);

        //  Create multi-frame message
        Message frames = new Message();

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            frames.addFrame(address);
        }

        //  Now add the data frame
        frames.addFrame(builder.build());

        return socket.send(frames);
    }

    /**
     * Send the REPLY to the socket in one step.
     */
    public boolean send(ReplyMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 4);       //  Message ID

        builder.putInt(message.sequence);
        if (message.headers != null) {
            builder.putByte((byte) message.headers.size());
            for (Map.Entry<String, String> entry: message.headers.entrySet()) {
                builder.putChars(entry.getKey() + "=" + entry.getValue());
            }
        } else {
            builder.putByte((byte) 0);   //  Empty dictionary
        }
        if (message.messages != null) {
            builder.putByte((byte) message.messages.size());
            for (String value : message.messages) {
                builder.putChars(value);
            }
        } else {
            builder.putByte((byte) 0);   //  Empty string array
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            frames.addFrame(address);
        }

        //  Now add the data frame
        frames.addFrame(builder.build());

        return socket.send(frames);
    }
}


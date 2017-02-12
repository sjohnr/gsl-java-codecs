/* ============================================================================
 * ZreSocket.java
 * 
 * Generated codec class for ZreSocket
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
package org.zyre;

import java.util.*;
import java.io.Closeable;
import java.nio.ByteBuffer;

import org.zeromq.api.*;
import org.zeromq.api.Message.Frame;
import org.zeromq.api.Message.FrameBuilder;
import org.zeromq.ZMQ;

/**
 * ZreSocket class.
 * 
 * The specification for this class is as follows:
 * <pre class="text">
 *  HELLO - Greet a peer so it can connect back to us
 *    sequence                     number 2
 *    ip-Address                   string
 *    mailbox                      number 2
 *    groups                       strings
 *    status                       number 1
 *    headers                      dictionary
 *  WHISPER - Send a message to a peer
 *    sequence                     number 2
 *    content                      frame
 *  SHOUT - Send a message to a group
 *    sequence                     number 2
 *    group                        string
 *    content                      frame
 *  JOIN - Join a group
 *    sequence                     number 2
 *    group                        string
 *    status                       number 1
 *  LEAVE - Leave a group
 *    sequence                     number 2
 *    group                        string
 *    status                       number 1
 *  PING - Ping a peer that has gone silent
 *    sequence                     number 2
 *  PING_OK - Reply to a peer's ping
 *    sequence                     number 2
 * </pre>
 * 
 * @author sriesenberg
 */
public class ZreSocket implements Closeable {
    //  Protocol constants
    public static final int VERSION           = 1;

    //  Enumeration of message types
    public enum MessageType {
        HELLO,
        WHISPER,
        SHOUT,
        JOIN,
        LEAVE,
        PING,
        PING_OK
    }

    //  Structure of our class
    private Socket socket;        //  Internal socket handle
    private Frame address;        //  Address of peer if any

    private HelloMessage hello;
    private WhisperMessage whisper;
    private ShoutMessage shout;
    private JoinMessage join;
    private LeaveMessage leave;
    private PingMessage ping;
    private PingOkMessage pingOk;

    /**
     * Create a new ZreSocket.
     * 
     * @param socket The internal socket
     */
    public ZreSocket(Socket socket) {
        assert (socket != null);
        this.socket = socket;
    }

    /**
     * Destroy the ZreSocket.
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
                case HELLO: {
                    HelloMessage message = this.hello = new HelloMessage();
                    message.sequence = (0xffff) & needle.getShort();
                    message.ipAddress = needle.getChars();
                    message.mailbox = (0xffff) & needle.getShort();
                    int groupsListSize = (0xff) & needle.getByte();
                    message.groups = new ArrayList<>(groupsListSize);
                    while (groupsListSize-- > 0) {
                        message.groups.add(needle.getChars());
                    }
                    message.status = (0xff) & needle.getByte();
                    int headersHashSize = (0xff) & needle.getByte();
                    message.headers = new HashMap<>(headersHashSize);
                    while (headersHashSize-- > 0) {
                        String string = needle.getChars();
                        String[] kv = string.split("=");
                        message.headers.put(kv[0], kv[1]);
                    }
                    break;
                }
                case WHISPER: {
                    WhisperMessage message = this.whisper = new WhisperMessage();
                    message.sequence = (0xffff) & needle.getShort();
                    //  Get next frame, leave current untouched
                    if (!frames.isEmpty()) {
                        message.content = frames.popFrame();
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case SHOUT: {
                    ShoutMessage message = this.shout = new ShoutMessage();
                    message.sequence = (0xffff) & needle.getShort();
                    message.group = needle.getChars();
                    //  Get next frame, leave current untouched
                    if (!frames.isEmpty()) {
                        message.content = frames.popFrame();
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case JOIN: {
                    JoinMessage message = this.join = new JoinMessage();
                    message.sequence = (0xffff) & needle.getShort();
                    message.group = needle.getChars();
                    message.status = (0xff) & needle.getByte();
                    break;
                }
                case LEAVE: {
                    LeaveMessage message = this.leave = new LeaveMessage();
                    message.sequence = (0xffff) & needle.getShort();
                    message.group = needle.getChars();
                    message.status = (0xff) & needle.getByte();
                    break;
                }
                case PING: {
                    PingMessage message = this.ping = new PingMessage();
                    message.sequence = (0xffff) & needle.getShort();
                    break;
                }
                case PING_OK: {
                    PingOkMessage message = this.pingOk = new PingOkMessage();
                    message.sequence = (0xffff) & needle.getShort();
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
     * Get a HELLO message from the socket.
     */
    public HelloMessage getHello() {
        return hello;
    }

    /**
     * Get a WHISPER message from the socket.
     */
    public WhisperMessage getWhisper() {
        return whisper;
    }

    /**
     * Get a SHOUT message from the socket.
     */
    public ShoutMessage getShout() {
        return shout;
    }

    /**
     * Get a JOIN message from the socket.
     */
    public JoinMessage getJoin() {
        return join;
    }

    /**
     * Get a LEAVE message from the socket.
     */
    public LeaveMessage getLeave() {
        return leave;
    }

    /**
     * Get a PING message from the socket.
     */
    public PingMessage getPing() {
        return ping;
    }

    /**
     * Get a PING_OK message from the socket.
     */
    public PingOkMessage getPingOk() {
        return pingOk;
    }

    /**
     * Send the HELLO to the socket in one step.
     */
    public boolean send(HelloMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 1);       //  Message ID

        builder.putShort((short) (int) message.sequence);
        if (message.ipAddress != null) {
            builder.putChars(message.ipAddress);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putShort((short) (int) message.mailbox);
        if (message.groups != null) {
            builder.putByte((byte) message.groups.size());
            for (String value : message.groups) {
                builder.putChars(value);
            }
        } else {
            builder.putByte((byte) 0);   //  Empty string array
        }
        builder.putByte((byte) (int) message.status);
        if (message.headers != null) {
            builder.putByte((byte) message.headers.size());
            for (Map.Entry<String, String> entry: message.headers.entrySet()) {
                builder.putChars(entry.getKey() + "=" + entry.getValue());
            }
        } else {
            builder.putByte((byte) 0);   //  Empty dictionary
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
     * Send the WHISPER to the socket in one step.
     */
    public boolean send(WhisperMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 2);       //  Message ID

        builder.putShort((short) (int) message.sequence);

        //  Create multi-frame message
        Message frames = new Message();

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert (address != null);
            frames.addFrame(address);
        }

        //  Now add the data frame
        frames.addFrame(builder.build());

        //  Now add any frame fields, in order
        frames.addFrame(message.content);

        return socket.send(frames);
    }

    /**
     * Send the SHOUT to the socket in one step.
     */
    public boolean send(ShoutMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 3);       //  Message ID

        builder.putShort((short) (int) message.sequence);
        if (message.group != null) {
            builder.putChars(message.group);
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

        //  Now add any frame fields, in order
        frames.addFrame(message.content);

        return socket.send(frames);
    }

    /**
     * Send the JOIN to the socket in one step.
     */
    public boolean send(JoinMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 4);       //  Message ID

        builder.putShort((short) (int) message.sequence);
        if (message.group != null) {
            builder.putChars(message.group);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putByte((byte) (int) message.status);

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
     * Send the LEAVE to the socket in one step.
     */
    public boolean send(LeaveMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 5);       //  Message ID

        builder.putShort((short) (int) message.sequence);
        if (message.group != null) {
            builder.putChars(message.group);
        } else {
            builder.putChars("");        //  Empty string
        }
        builder.putByte((byte) (int) message.status);

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
     * Send the PING to the socket in one step.
     */
    public boolean send(PingMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 6);       //  Message ID

        builder.putShort((short) (int) message.sequence);

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
     * Send the PING_OK to the socket in one step.
     */
    public boolean send(PingOkMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xAAA0 | 1));
        builder.putByte((byte) 7);       //  Message ID

        builder.putShort((short) (int) message.sequence);

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


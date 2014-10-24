package org.distlog4j;

import static org.junit.Assert.*;

import org.junit.*;
import org.zeromq.api.*;
import org.zeromq.jzmq.*;

/**
 * Test LogSocket.
 */
public class LogSocketTest {
    private Context context;
    private Socket dealer;
    private Socket router;
    
    @Before
    public void setUp() {
        context = new ManagedContext();
        dealer = context.buildSocket(SocketType.DEALER)
            .bind("inproc://selftest");
        router = context.buildSocket(SocketType.ROUTER)
            .connect("inproc://selftest");
    }

    @Test
    public void testLog() {
        LogSocket out = new LogSocket(dealer);
        LogSocket in = new LogSocket(router);
        
        LogMessage message = new LogMessage();
        message.setSequence((byte) 123);
        message.putHeader("Name", "Brutus");
        message.putHeader("Age", "%d", Integer.valueOf(43));
        message.setIp("Life is short but Now lasts for ever");
        message.setPort((byte) 123);
        message.setFileName("Life is short but Now lasts for ever");
        message.setLineNum((byte) 123);
        message.setMessage("Life is short but Now lasts for ever");
        
        assertTrue(out.sendLog(message));
        assertEquals(LogSocket.MessageType.LOG, in.receive());
        message = in.getLog();
        assertEquals(message.getSequence(), 123);
        assertEquals(message.getHeaders().size(), 2);
        assertEquals(message.getHeader("Name", "?"), "Brutus");
        assertEquals(message.getHeader("Age", 0), 43);
        assertEquals(message.getIp(), "Life is short but Now lasts for ever");
        assertEquals(message.getPort(), 123);
        assertEquals(message.getFileName(), "Life is short but Now lasts for ever");
        assertEquals(message.getLineNum(), 123);
        assertEquals(message.getMessage(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testRequest() {
        LogSocket out = new LogSocket(dealer);
        LogSocket in = new LogSocket(router);
        
        RequestMessage message = new RequestMessage();
        message.setSequence((byte) 123);
        message.setFileName("Life is short but Now lasts for ever");
        message.setStart((byte) 123);
        message.setEnd((byte) 123);
        
        assertTrue(out.sendRequest(message));
        assertEquals(LogSocket.MessageType.REQUEST, in.receive());
        message = in.getRequest();
        assertEquals(message.getSequence(), 123);
        assertEquals(message.getFileName(), "Life is short but Now lasts for ever");
        assertEquals(message.getStart(), 123);
        assertEquals(message.getEnd(), 123);
        
        out.close();
        in.close();
    }

    @Test
    public void testReply() {
        LogSocket out = new LogSocket(dealer);
        LogSocket in = new LogSocket(router);
        
        ReplyMessage message = new ReplyMessage();
        message.setSequence((byte) 123);
        message.putHeader("Name", "Brutus");
        message.putHeader("Age", "%d", Integer.valueOf(43));
        message.addMessage("Name: %s", "Brutus");
        message.addMessage("Age: %d", Integer.valueOf(43));
        
        assertTrue(out.sendReply(message));
        assertEquals(LogSocket.MessageType.REPLY, in.receive());
        message = in.getReply();
        assertEquals(message.getSequence(), 123);
        assertEquals(message.getHeaders().size(), 2);
        assertEquals(message.getHeader("Name", "?"), "Brutus");
        assertEquals(message.getHeader("Age", 0), 43);
        assertEquals(message.getMessages().size(), 2);
        assertEquals(message.getMessages().get(0), "Name: Brutus");
        assertEquals(message.getMessages().get(1), "Age: 43");
        
        out.close();
        in.close();
    }
}
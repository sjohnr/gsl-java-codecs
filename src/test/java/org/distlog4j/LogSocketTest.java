package org.distlog4j;

import static org.junit.Assert.*;

import org.junit.*;
import org.zeromq.api.*;
import org.zeromq.api.Message.Frame;
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
        message.setSequence(123);
        message.putHeader("Name", "Brutus");
        message.putHeader("Age", 43);
        message.setIp("Life is short but Now lasts for ever");
        message.setPort(123);
        message.setFileName("Life is short but Now lasts for ever");
        message.setLineNum(123);
        message.setMessage("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(LogSocket.MessageType.LOG, in.receive());
        message = in.getLog();
        assertEquals(message.getSequence(), Integer.valueOf(123));
        assertEquals(message.getHeaders().size(), 2);
        assertEquals(message.getHeader("Name", "?"), "Brutus");
        assertEquals(message.getHeader("Age", 0), 43);
        assertEquals(message.getIp(), "Life is short but Now lasts for ever");
        assertEquals(message.getPort(), Integer.valueOf(123));
        assertEquals(message.getFileName(), "Life is short but Now lasts for ever");
        assertEquals(message.getLineNum(), Integer.valueOf(123));
        assertEquals(message.getMessage(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testLogs() {
        LogSocket out = new LogSocket(dealer);
        LogSocket in = new LogSocket(router);
        
        LogsMessage message = new LogsMessage();
        message.setSequence(123);
        message.putHeader("Name", "Brutus");
        message.putHeader("Age", 43);
        message.setIp("Life is short but Now lasts for ever");
        message.setPort(123);
        message.setFileName("Life is short but Now lasts for ever");
        message.setLineNum(123);
        message.addMessage("Name: Brutus");
        message.addMessage("Age: 43");
        
        assertTrue(out.send(message));
        assertEquals(LogSocket.MessageType.LOGS, in.receive());
        message = in.getLogs();
        assertEquals(message.getSequence(), Integer.valueOf(123));
        assertEquals(message.getHeaders().size(), 2);
        assertEquals(message.getHeader("Name", "?"), "Brutus");
        assertEquals(message.getHeader("Age", 0), 43);
        assertEquals(message.getIp(), "Life is short but Now lasts for ever");
        assertEquals(message.getPort(), Integer.valueOf(123));
        assertEquals(message.getFileName(), "Life is short but Now lasts for ever");
        assertEquals(message.getLineNum(), Integer.valueOf(123));
        assertEquals(message.getMessages().size(), 2);
        assertEquals(message.getMessages().get(0), "Name: Brutus");
        assertEquals(message.getMessages().get(1), "Age: 43");
        
        out.close();
        in.close();
    }

    @Test
    public void testRequest() {
        LogSocket out = new LogSocket(dealer);
        LogSocket in = new LogSocket(router);
        
        RequestMessage message = new RequestMessage();
        message.setSequence(123);
        message.setFileName("Life is short but Now lasts for ever");
        message.setStart(123);
        message.setEnd(123);
        
        assertTrue(out.send(message));
        assertEquals(LogSocket.MessageType.REQUEST, in.receive());
        message = in.getRequest();
        assertEquals(message.getSequence(), Integer.valueOf(123));
        assertEquals(message.getFileName(), "Life is short but Now lasts for ever");
        assertEquals(message.getStart(), Integer.valueOf(123));
        assertEquals(message.getEnd(), Integer.valueOf(123));
        
        out.close();
        in.close();
    }

    @Test
    public void testReply() {
        LogSocket out = new LogSocket(dealer);
        LogSocket in = new LogSocket(router);
        
        ReplyMessage message = new ReplyMessage();
        message.setSequence(123);
        message.putHeader("Name", "Brutus");
        message.putHeader("Age", 43);
        message.addMessage("Name: Brutus");
        message.addMessage("Age: 43");
        
        assertTrue(out.send(message));
        assertEquals(LogSocket.MessageType.REPLY, in.receive());
        message = in.getReply();
        assertEquals(message.getSequence(), Integer.valueOf(123));
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
package org.distlog4j;

import static org.junit.Assert.*;
import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZFrame;
import org.zeromq.ZContext;

/**
 * Test LogMessage.
 */
public class TestLogMessage {
	@Test
	public void testLogMessage() {
		System.out.printf(" * log_message: ");

		//  Simple create/destroy test
		LogMessage self = new LogMessage(0);
		assert (self != null);
		self.destroy();

		//  Create pair of sockets we can send through
		ZContext ctx = new ZContext();
		assert (ctx != null);

		Socket output = ctx.createSocket(ZMQ.DEALER);
		assert (output != null);
		output.bind("inproc://selftest");
		Socket input = ctx.createSocket(ZMQ.ROUTER);
		assert (input != null);
		input.connect("inproc://selftest");
		
		//  Encode/send/decode and verify each message type

		self = new LogMessage(LogMessage.LOG);
		self.setSequence((byte) 123);
		self.putHeader("Name", "Brutus");
		self.putHeader("Age", "%d", Integer.valueOf(43));
		self.setIp("Life is short but Now lasts for ever");
		self.setPort((byte) 123);
		self.setFile_Name("Life is short but Now lasts for ever");
		self.setLine_Num((byte) 123);
		self.setMessage("Life is short but Now lasts for ever");
		self.send(output);
	
		self = LogMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertEquals(self.getHeaders().size(), 2);
		assertEquals(self.getHeaderString("Name", "?"), "Brutus");
		assertEquals(self.getHeaderNumber("Age", 0), 43);
		assertEquals(self.getIp(), "Life is short but Now lasts for ever");
		assertEquals(self.getPort(), 123);
		assertEquals(self.getFile_Name(), "Life is short but Now lasts for ever");
		assertEquals(self.getLine_Num(), 123);
		assertEquals(self.getMessage(), "Life is short but Now lasts for ever");
		self.destroy();

		ctx.close();
		System.out.printf("OK\n");
	}
}
package org.distlog4j;

import static org.junit.Assert.*;
import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZFrame;
import org.zeromq.ZContext;

/**
 * Test ReplayMessage.
 */
public class TestReplayMessage {
	@Test
	public void testReplayMessage() {
		System.out.printf(" * replay_message: ");

		//  Simple create/destroy test
		ReplayMessage self = new ReplayMessage(0);
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

		self = new ReplayMessage(ReplayMessage.REQUEST);
		self.setSequence((byte) 123);
		self.setFile_Name("Life is short but Now lasts for ever");
		self.setStart((byte) 123);
		self.setEnd((byte) 123);
		self.send(output);
	
		self = ReplayMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertEquals(self.getFile_Name(), "Life is short but Now lasts for ever");
		assertEquals(self.getStart(), 123);
		assertEquals(self.getEnd(), 123);
		self.destroy();

		self = new ReplayMessage(ReplayMessage.REPLY);
		self.setSequence((byte) 123);
		self.putHeader("Name", "Brutus");
		self.putHeader("Age", "%d", Integer.valueOf(43));
		self.addMessage("Name: %s", "Brutus");
		self.addMessage("Age: %d", Integer.valueOf(43));
		self.send(output);
	
		self = ReplayMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertEquals(self.getHeaders().size(), 2);
		assertEquals(self.getHeaderString("Name", "?"), "Brutus");
		assertEquals(self.getHeaderNumber("Age", 0), 43);
		assertEquals(self.getMessages().size(), 2);
		assertEquals(self.getMessages().get(0), "Name: Brutus");
		assertEquals(self.getMessages().get(1), "Age: 43");
		self.destroy();

		ctx.close();
		System.out.printf("OK\n");
	}
}
package grid.zmq;

import static org.junit.Assert.*;
import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZFrame;
import org.zeromq.ZContext;

/**
 * Test GridMessage.
 */
public class TestGridMessage {
	@Test
	public void testGridMessage() {
		System.out.printf(" * grid_message: ");

		//  Simple create/destroy test
		GridMessage self = new GridMessage(0);
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

		self = new GridMessage(GridMessage.CONNECT);
		self.setSequence((byte) 123);
		self.setIp("Life is short but Now lasts for ever");
		self.setPort((byte) 123);
		self.addCluster("Name: %s", "Brutus");
		self.addCluster("Age: %d", Integer.valueOf(43));
		self.setStatus((byte) 123);
		self.putHeader("Name", "Brutus");
		self.putHeader("Age", "%d", Integer.valueOf(43));
		self.send(output);
	
		self = GridMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertEquals(self.getIp(), "Life is short but Now lasts for ever");
		assertEquals(self.getPort(), 123);
		assertEquals(self.getClusters().size(), 2);
		assertEquals(self.getClusters().get(0), "Name: Brutus");
		assertEquals(self.getClusters().get(1), "Age: 43");
		assertEquals(self.getStatus(), 123);
		assertEquals(self.getHeaders().size(), 2);
		assertEquals(self.getHeaderString("Name", "?"), "Brutus");
		assertEquals(self.getHeaderNumber("Age", 0), 43);
		self.destroy();

		self = new GridMessage(GridMessage.MESSAGE);
		self.setSequence((byte) 123);
		self.setContent(new ZFrame("Captcha Diem"));
		self.send(output);
	
		self = GridMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertTrue(self.getContent().streq("Captcha Diem"));
		self.destroy();

		self = new GridMessage(GridMessage.BROADCAST);
		self.setSequence((byte) 123);
		self.setCluster("Life is short but Now lasts for ever");
		self.setContent(new ZFrame("Captcha Diem"));
		self.send(output);
	
		self = GridMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertEquals(self.getCluster(), "Life is short but Now lasts for ever");
		assertTrue(self.getContent().streq("Captcha Diem"));
		self.destroy();

		self = new GridMessage(GridMessage.JOIN);
		self.setSequence((byte) 123);
		self.setCluster("Life is short but Now lasts for ever");
		self.setStatus((byte) 123);
		self.send(output);
	
		self = GridMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertEquals(self.getCluster(), "Life is short but Now lasts for ever");
		assertEquals(self.getStatus(), 123);
		self.destroy();

		self = new GridMessage(GridMessage.LEAVE);
		self.setSequence((byte) 123);
		self.setCluster("Life is short but Now lasts for ever");
		self.setStatus((byte) 123);
		self.send(output);
	
		self = GridMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		assertEquals(self.getCluster(), "Life is short but Now lasts for ever");
		assertEquals(self.getStatus(), 123);
		self.destroy();

		self = new GridMessage(GridMessage.PING);
		self.setSequence((byte) 123);
		self.send(output);
	
		self = GridMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		self.destroy();

		self = new GridMessage(GridMessage.ECHO);
		self.setSequence((byte) 123);
		self.send(output);
	
		self = GridMessage.receive(input);
		assert (self != null);
		assertEquals(self.getSequence(), 123);
		self.destroy();

		ctx.close();
		System.out.printf("OK\n");
	}
}
package grid.zmq;

import static org.junit.Assert.*;
import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZFrame;
import org.zeromq.ZContext;

/**
 * Test GridLogMessage.
 */
public class TestGridLogMessage {
	@Test
	public void testGridLogMessage() {
		System.out.printf(" * grid_log_message: ");

		//  Simple create/destroy test
		GridLogMessage self = new GridLogMessage(0);
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

		self = new GridLogMessage(GridLogMessage.LOG);
		self.setLevel((byte) 123);
		self.setEvent((byte) 123);
		self.setNode((byte) 123);
		self.setPeer((byte) 123);
		self.setTime((byte) 123);
		self.setMessage("Life is short but Now lasts for ever");
		self.send(output);
	
		self = GridLogMessage.receive(input);
		assert (self != null);
		assertEquals(self.getLevel(), 123);
		assertEquals(self.getEvent(), 123);
		assertEquals(self.getNode(), 123);
		assertEquals(self.getPeer(), 123);
		assertEquals(self.getTime(), 123);
		assertEquals(self.getMessage(), "Life is short but Now lasts for ever");
		self.destroy();

		ctx.close();
		System.out.printf("OK\n");
	}
}
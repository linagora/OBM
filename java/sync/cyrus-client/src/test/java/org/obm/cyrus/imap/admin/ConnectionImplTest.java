package org.obm.cyrus.imap.admin;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.SlowGuiceRunner;
import org.obm.push.minig.imap.StoreClient;

@RunWith(SlowGuiceRunner.class)
public class ConnectionImplTest {
	private IMocksControl control;

	@Before
	public void setUp() {
		control = createControl();
	}
	
	@Test
	public void testCreateUserMailboxes() {
		StoreClient mockClient = control.createMock(StoreClient.class);
		
				
		expect(mockClient.create("user/ident4@vm.obm.org", "partition")).andReturn(true);
		expect(mockClient.create("user/ident4@vm.obm.org/Trash", "partition")).andReturn(true);
		
		control.replay();
	
		Connection conn = new ConnectionImpl(mockClient);
		conn.createUserMailboxes(Partition.of("partition"),
				ImapPath.builder().user("ident4@vm.obm.org").build(),
				ImapPath.builder().user("ident4@vm.obm.org").pathFragment("Trash").build()
				);
		control.verify();
	}

}

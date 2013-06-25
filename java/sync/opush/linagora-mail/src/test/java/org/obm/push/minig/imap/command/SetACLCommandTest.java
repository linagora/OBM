package org.obm.push.minig.imap.command;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;


@RunWith(SlowFilterRunner.class)
public class SetACLCommandTest {
	
	@Test
	public void testSetACLCommand() {
		assertThat(new SetACLCommand("user/admin@vm.obm.org", "admin@vm.obm.org", "lrswidc")
			.getImapCommand()).isEqualTo("SETACL \"user/admin@vm.obm.org\" \"admin@vm.obm.org\" \"lrswidc\"");
	}
}
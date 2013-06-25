package org.obm.push.minig.imap.command;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;


@RunWith(SlowFilterRunner.class)
public class GetACLCommandTest {
	
	@Test
	public void testSetACLCommand() {
		assertThat(new GetACLCommand("user/admin@vm.obm.org")
			.getImapCommand()).isEqualTo("GETACL \"user/admin@vm.obm.org\"");
	}
}

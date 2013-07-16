package org.obm.cyrus.imap.admin;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
public class ImapPathTest {

	@Test
	public void testImapPathFormat() {
		ImapPath imapPath = ImapPath.builder().user("ident4@vm.obm.org").pathFragment("folder1").pathFragment("folder2").build();
		assertThat(imapPath.format()).isEqualTo("user/ident4@vm.obm.org/folder1/folder2");
	}

	@Test
	public void testImapPathNoFragmentFormat() {
		ImapPath imapPath = ImapPath.builder().user("ident4@vm.obm.org").build();
		assertThat(imapPath.format()).isEqualTo("user/ident4@vm.obm.org");
	}
}

/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version, provided you comply
 * with the Additional Terms applicable for OBM connector by Linagora
 * pursuant to Section 7 of the GNU Affero General Public License,
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain
 * the “Message sent thanks to OBM, Free Communication by Linagora”
 * signature notice appended to any and all outbound messages
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks
 * and commercial brands. Other Additional Terms apply,
 * see <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and its applicable Additional Terms for OBM along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to
 * OBM connectors.
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.imap.archive.mailbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.sync.base.DomainName;


public class MailboxPathsTest {

	@Test(expected=NullPointerException.class)
	public void mainPathShouldNotBeNull() {
		new MailboxPaths.Builder().mainPath(null);
	}

	@Test(expected=NullPointerException.class)
	public void userShouldNotBeNull() {
		new MailboxPaths.Builder().user(null);
	}

	@Test(expected=NullPointerException.class)
	public void subPathsShouldNotBeNull() {
		new MailboxPaths.Builder().subPaths(null);
	}

	@Test(expected=NullPointerException.class)
	public void domainNameShouldNotBeNull() {
		new MailboxPaths.Builder().domainName(null);
	}

	@Test(expected=IllegalStateException.class)
	public void mainPathIsMandatory() {
		new MailboxPaths.Builder().build();
	}

	@Test(expected=IllegalStateException.class)
	public void mainPathShouldNotBeEmpty() {
		new MailboxPaths.Builder().mainPath("").build();
	}

	@Test(expected=IllegalStateException.class)
	public void userShouldIsMandatory() {
		new MailboxPaths.Builder().mainPath("user").build();
	}

	@Test(expected=IllegalStateException.class)
	public void userShouldNotBeEmpty() {
		new MailboxPaths.Builder().mainPath("user").user("").build();
	}

	@Test(expected=IllegalStateException.class)
	public void subPathsIsMandatory() {
		new MailboxPaths.Builder().mainPath("user").user("usera").build();
	}

	@Test(expected=IllegalStateException.class)
	public void subPathsShouldNotBeEmpty() {
		new MailboxPaths.Builder().mainPath("user").user("usera").subPaths("").build();
	}

	@Test(expected=IllegalStateException.class)
	public void domainNameIsMandatory() {
		new MailboxPaths.Builder().mainPath("user").user("usera").subPaths("INBOX").build();
	}
	
	@Test(expected=MailboxFormatException.class)
	public void fromShouldThrowWhenEmptyMailbox() throws Exception {
		MailboxPaths.from("");
	}
	
	@Test(expected=MailboxFormatException.class)
	public void fromShouldThrowWhenNoUser() throws Exception {
		MailboxPaths.from("user");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void fromShouldThrowWhenNoDomain() throws Exception {
		MailboxPaths.from("user/usera@");
	}
	
	@Test
	public void fromShouldBuild() throws Exception {
		MailboxPaths expectedMailboxPaths = new MailboxPaths("user", "usera", "path/subpath/subsubpath", new DomainName("mydomain.org"));
		
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org");
		
		assertThat(mailboxPaths).isEqualTo(expectedMailboxPaths);
	}
	
	@Test
	public void getMainPath() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org");
		assertThat(mailboxPaths.getMainPath()).isEqualTo("user");
	}

	@Test
	public void getUserWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org");
		assertThat(mailboxPaths.getUser()).isEqualTo("usera");
	}

	@Test
	public void getUserWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org");
		assertThat(mailboxPaths.getUser()).isEqualTo("usera");
	}

	@Test
	public void getUserWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org");
		assertThat(mailboxPaths.getUser()).isEqualTo("usera");
	}

	@Test
	public void getSubPathsWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("INBOX");
	}

	@Test
	public void getSubPathsWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("path");
	}

	@Test
	public void getSubPathsWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("path/subpath/subsubpath");
	}

	@Test(expected=IllegalArgumentException.class)
	public void prependShouldThrowWhenMainSubPathIsNull() throws Exception {
		MailboxPaths.from("user/usera@mydomain.org").prepend(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void prependShouldThrowWhenMainSubPathIsEmpty() throws Exception {
		MailboxPaths.from("user/usera@mydomain.org").prepend("");
	}

	@Test
	public void getSubPathsWhenNoLevelAndPrepend() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org").prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/INBOX");
	}

	@Test
	public void getSubPathsWhenOneLevelAndPrepend() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org").prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/path");
	}

	@Test
	public void getSubPathsWhenMultipleLevelsAndPrepend() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org").prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/path/subpath/subsubpath");
	}

	@Test
	public void getDomainNameWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org");
		assertThat(mailboxPaths.getDomainName()).isEqualTo(new DomainName("mydomain.org"));
	}

	@Test
	public void getDomainNameWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org");
		assertThat(mailboxPaths.getDomainName()).isEqualTo(new DomainName("mydomain.org"));
	}

	@Test
	public void getDomainNameWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org");
		assertThat(mailboxPaths.getDomainName()).isEqualTo(new DomainName("mydomain.org"));
	}
	
	@Test
	public void getNameWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org");
		assertThat(mailboxPaths.getName()).isEqualTo("user/usera/INBOX@mydomain.org");
	}
	
	@Test
	public void getNameWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org");
		assertThat(mailboxPaths.getName()).isEqualTo("user/usera/path@mydomain.org");
	}
	
	@Test
	public void getNameWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org");
		assertThat(mailboxPaths.getName()).isEqualTo("user/usera/path/subpath/subsubpath@mydomain.org");
	}
	
	@Test
	public void getUserAtDomainShoudWorkWhenMailboxIsINBOX() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org");
		assertThat(mailboxPaths.getUserAtDomain()).isEqualTo("usera@mydomain.org");
	}
	
	@Test
	public void getUserAtDomainShoudWorkWhenMailboxIsAFolder() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/Test@mydomain.org");
		assertThat(mailboxPaths.getUserAtDomain()).isEqualTo("usera@mydomain.org");
	}
	
	@Test
	public void getUserAtDomainShoudWorkWhenMailboxIsASubFolder() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/Test/subfolder@mydomain.org");
		assertThat(mailboxPaths.getUserAtDomain()).isEqualTo("usera@mydomain.org");
	}
}

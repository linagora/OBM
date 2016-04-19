/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
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
		MailboxPaths.from("", false);
	}
	
	@Test(expected=MailboxFormatException.class)
	public void fromShouldThrowWhenNoUser() throws Exception {
		MailboxPaths.from("user", false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void fromShouldThrowWhenNoDomain() throws Exception {
		MailboxPaths.from("user/usera@", false);
	}
	
	@Test
	public void fromShouldBuild() throws Exception {
		MailboxPaths expectedMailboxPaths = new MailboxPaths("user", "usera", "path/subpath/subsubpath", new DomainName("mydomain.org"), false);
		
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org", false);
		
		assertThat(mailboxPaths).isEqualTo(expectedMailboxPaths);
	}
	
	@Test
	public void fromShouldBuildWhenSharedMailbox() throws Exception {
		MailboxPaths expectedMailboxPaths = new MailboxPaths("shared", null, null, new DomainName("mydomain.org"), true);
		
		MailboxPaths mailboxPaths = MailboxPaths.from("shared@mydomain.org", true);
		
		assertThat(mailboxPaths).isEqualTo(expectedMailboxPaths);
	}
	
	@Test
	public void fromShouldBuildWhenSharedMailboxWithFolder() throws Exception {
		MailboxPaths expectedMailboxPaths = new MailboxPaths("shared", null, "path/subpath/subsubpath", new DomainName("mydomain.org"), true);
		
		MailboxPaths mailboxPaths = MailboxPaths.from("shared/path/subpath/subsubpath@mydomain.org", true);
		
		assertThat(mailboxPaths).isEqualTo(expectedMailboxPaths);
	}
	
	@Test
	public void getMainPath() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org", false);
		assertThat(mailboxPaths.getMainPath()).isEqualTo("user");
	}

	@Test
	public void getUserWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org", false);
		assertThat(mailboxPaths.getUser()).isEqualTo("usera");
	}

	@Test
	public void getUserWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org", false);
		assertThat(mailboxPaths.getUser()).isEqualTo("usera");
	}

	@Test
	public void getUserWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org", false);
		assertThat(mailboxPaths.getUser()).isEqualTo("usera");
	}

	@Test
	public void getSubPathsWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org", false);
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("INBOX");
	}

	@Test
	public void getSubPathsWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org", false);
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("path");
	}

	@Test
	public void getSubPathsWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org", false);
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("path/subpath/subsubpath");
	}

	@Test(expected=IllegalArgumentException.class)
	public void prependShouldThrowWhenMainSubPathIsNull() throws Exception {
		MailboxPaths.from("user/usera@mydomain.org", false).prepend(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void prependShouldThrowWhenMainSubPathIsEmpty() throws Exception {
		MailboxPaths.from("user/usera@mydomain.org", false).prepend("");
	}
	
	@Test
	public void getSubPathsWhenNoLevelAndPrepend() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org", false).prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/INBOX");
	}

	@Test
	public void getSubPathsWhenOneLevelAndPrepend() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org", false).prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/path");
	}

	@Test
	public void getSubPathsWhenMultipleLevelsAndPrepend() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org", false).prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/path/subpath/subsubpath");
	}
	
	@Test
	public void getSubPathsWhenNoLevelAndPrependOnSharedMailbox() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("shared@mydomain.org", true).prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath");
	}

	@Test
	public void getSubPathsWhenOneLevelAndPrependOnSharedMailbox() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("shared/path@mydomain.org", true).prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/path");
	}

	@Test
	public void getSubPathsWhenMultipleLevelsAndPrependOnSharedMailbox() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("shared/path/subpath/subsubpath@mydomain.org", true).prepend("mainSubPath");
		assertThat(mailboxPaths.getSubPaths()).isEqualTo("mainSubPath/path/subpath/subsubpath");
	}

	@Test
	public void getDomainNameWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org", false);
		assertThat(mailboxPaths.getDomainName()).isEqualTo(new DomainName("mydomain.org"));
	}

	@Test
	public void getDomainNameWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org", false);
		assertThat(mailboxPaths.getDomainName()).isEqualTo(new DomainName("mydomain.org"));
	}

	@Test
	public void getDomainNameWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org", false);
		assertThat(mailboxPaths.getDomainName()).isEqualTo(new DomainName("mydomain.org"));
	}
	
	@Test
	public void getNameWhenNoLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org", false);
		assertThat(mailboxPaths.getName()).isEqualTo("user/usera/INBOX@mydomain.org");
	}
	
	@Test
	public void getNameWhenOneLevel() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path@mydomain.org", false);
		assertThat(mailboxPaths.getName()).isEqualTo("user/usera/path@mydomain.org");
	}
	
	@Test
	public void getNameWhenMultipleLevels() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/path/subpath/subsubpath@mydomain.org", false);
		assertThat(mailboxPaths.getName()).isEqualTo("user/usera/path/subpath/subsubpath@mydomain.org");
	}
	
	@Test
	public void getNameWhenNoLevelOnSharedMailbox() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("shared@mydomain.org", true);
		assertThat(mailboxPaths.getName()).isEqualTo("shared@mydomain.org");
	}
	
	@Test
	public void getNameWhenOneLevelOnSharedMailbox() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("shared/path@mydomain.org", true);
		assertThat(mailboxPaths.getName()).isEqualTo("shared/path@mydomain.org");
	}
	
	@Test
	public void getNameWhenMultipleLevelsOnSharedMailbox() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("shared/path/subpath/subsubpath@mydomain.org", true);
		assertThat(mailboxPaths.getName()).isEqualTo("shared/path/subpath/subsubpath@mydomain.org");
	}
	
	@Test
	public void getUserAtDomainShoudWorkWhenMailboxIsINBOX() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera@mydomain.org", false);
		assertThat(mailboxPaths.getUserAtDomain()).isEqualTo("usera@mydomain.org");
	}
	
	@Test
	public void getUserAtDomainShoudWorkWhenMailboxIsAFolder() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/Test@mydomain.org", false);
		assertThat(mailboxPaths.getUserAtDomain()).isEqualTo("usera@mydomain.org");
	}
	
	@Test
	public void getUserAtDomainShoudWorkWhenMailboxIsASubFolder() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/Test/subfolder@mydomain.org", false);
		assertThat(mailboxPaths.getUserAtDomain()).isEqualTo("usera@mydomain.org");
	}
	
	@Test
	public void getUserAtDomainShoudBeNullWhenSharedMailbox() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("shared@mydomain.org", true);
		assertThat(mailboxPaths.getUserAtDomain()).isNull();
	}
	
	@Test
	public void belongsToShouldThrowWhenNull() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/Test/subfolder@mydomain.org", false);
		boolean belongsTo = mailboxPaths.belongsTo(null);
		assertThat(belongsTo).isFalse();
	}
	
	@Test
	public void belongsToShouldReturnFalseWhenOtherDomain() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/Test/subfolder@mydomain.org", false);
		boolean belongsTo = mailboxPaths.belongsTo(new DomainName("otherdomain.org"));
		assertThat(belongsTo).isFalse();
	}
	
	@Test
	public void belongsToShouldReturnTrueWhenMatching() throws Exception {
		MailboxPaths mailboxPaths = MailboxPaths.from("user/usera/Test/subfolder@mydomain.org", false);
		boolean belongsTo = mailboxPaths.belongsTo(new DomainName("mydomain.org"));
		assertThat(belongsTo).isTrue();
	}
}

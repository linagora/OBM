/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.configuration;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration.MailboxNameCheckPolicy;
import org.obm.push.utils.IniFile;
import org.obm.push.utils.IniFile.Factory;

public class EmailConfigurationImplTest {

	private EmailConfigurationImpl config;
	private IMocksControl mocksControl;
	private IniFile iniFile;

	@Before
	public void setup() {
		mocksControl = createControl();
		iniFile = mocksControl.createMock(IniFile.class);

		Factory factory = createMock(IniFile.Factory.class);

		expect(factory.build(anyObject(String.class))).andReturn(iniFile);
		replay(factory);

		config = new EmailConfigurationImpl(factory);
	}

	@Test
	public void testMailboxNameCheckPolicyWhenNotDefined() {
		expect(iniFile.getStringValue(EmailConfigurationImpl.BACKEND_IMAP_MAILBOX_NAME_CHECK_POLICY)).andReturn(null);
		mocksControl.replay();

		assertThat(config.mailboxNameCheckPolicy()).isEqualTo(EmailConfigurationImpl.MAILBOX_NAME_CHECK_POLICY_DEFAULT);
	}

	@Test
	public void testMailboxNameCheckPolicyWhenDefined() {
		expect(iniFile.getStringValue(EmailConfigurationImpl.BACKEND_IMAP_MAILBOX_NAME_CHECK_POLICY)).andReturn("ALWAYS");
		mocksControl.replay();

		assertThat(config.mailboxNameCheckPolicy()).isEqualTo(MailboxNameCheckPolicy.ALWAYS);
	}

	@Test
	public void testMailboxNameCheckPolicyWhenDefinedLowerCase() {
		expect(iniFile.getStringValue(EmailConfigurationImpl.BACKEND_IMAP_MAILBOX_NAME_CHECK_POLICY)).andReturn("always");
		mocksControl.replay();

		assertThat(config.mailboxNameCheckPolicy()).isEqualTo(MailboxNameCheckPolicy.ALWAYS);
	}

	@Test
	public void testMailboxNameCheckPolicyWhenDefinedWeirdCase() {
		expect(iniFile.getStringValue(EmailConfigurationImpl.BACKEND_IMAP_MAILBOX_NAME_CHECK_POLICY)).andReturn("aLWayS");
		mocksControl.replay();

		assertThat(config.mailboxNameCheckPolicy()).isEqualTo(MailboxNameCheckPolicy.ALWAYS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMailboxNameCheckPolicyWhenInvalidValue() {
		expect(iniFile.getStringValue(EmailConfigurationImpl.BACKEND_IMAP_MAILBOX_NAME_CHECK_POLICY)).andReturn("nonexistent");
		mocksControl.replay();

		config.mailboxNameCheckPolicy();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMailboxNameCheckPolicyWhenEmptyString() {
		expect(iniFile.getStringValue(EmailConfigurationImpl.BACKEND_IMAP_MAILBOX_NAME_CHECK_POLICY)).andReturn("");
		mocksControl.replay();

		config.mailboxNameCheckPolicy();
	}

}

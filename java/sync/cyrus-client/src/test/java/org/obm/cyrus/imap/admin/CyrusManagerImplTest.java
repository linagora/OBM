/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
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
package org.obm.cyrus.imap.admin;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.cyrus.imap.admin.CyrusManagerImpl.DRAFTS;
import static org.obm.cyrus.imap.admin.CyrusManagerImpl.SENT;
import static org.obm.cyrus.imap.admin.CyrusManagerImpl.SPAM;
import static org.obm.cyrus.imap.admin.CyrusManagerImpl.TEMPLATES;
import static org.obm.cyrus.imap.admin.CyrusManagerImpl.TRASH;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.ConfigurationService;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;

public class CyrusManagerImplTest {

	private IMocksControl control;
	private Connection connection;
	private ConfigurationService configurationService;

	private CyrusManagerImpl manager;

	@Before
	public void setUp() {
		control = createControl();
		connection = control.createMock(Connection.class);
		configurationService = control.createMock(ConfigurationService.class);

		manager = new CyrusManagerImpl(connection, configurationService);
	}

	@After
	public void tearDown() {
		control.verify();
	}

	@Test
	public void testCreateShouldNotCreateOnPartitionIfConfIsFalse() throws Exception {
		ObmUser obmUser = ToolBox.getDefaultObmUser();
		String user = obmUser.getLogin(), domain = obmUser.getDomain().getName();

		expect(configurationService.isCyrusPartitionEnabled()).andReturn(false);
		connection.createUserMailboxes(
				ImapPath.builder().user(user).domain(domain).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(TRASH).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(DRAFTS).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(SPAM).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(TEMPLATES).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(SENT).build());
		expectLastCall();
		control.replay();

		manager.create(obmUser);
	}

	@Test
	public void testCreateShouldCreateOnPartitionIfConfIsTrue() throws Exception {
		ObmUser obmUser = ToolBox.getDefaultObmUser();
		String user = obmUser.getLogin(), domain = obmUser.getDomain().getName();

		expect(configurationService.isCyrusPartitionEnabled()).andReturn(true);
		connection.createUserMailboxes(
				Partition.fromObmDomain(domain),
				ImapPath.builder().user(user).domain(domain).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(TRASH).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(DRAFTS).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(SPAM).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(TEMPLATES).build(),
				ImapPath.builder().user(user).domain(domain).pathFragment(SENT).build());
		expectLastCall();
		control.replay();

		manager.create(obmUser);
	}

}

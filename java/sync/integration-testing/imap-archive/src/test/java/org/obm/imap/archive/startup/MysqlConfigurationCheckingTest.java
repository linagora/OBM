/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.startup;

import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.exception.UnsupportedDatabaseFlavourException;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MysqlConfigurationCheckingTest {

	private ClientDriverRule driver = new ClientDriverRule();

	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new TemporaryFolder())
			.around(new GuiceRule(this, new TestImapArchiveModules.Mysql(driver, new Provider<TemporaryFolder>() {

				@Override
				public TemporaryFolder get() {
					return temporaryFolder;
				}
				
			})));
	
	private @Inject TemporaryFolder temporaryFolder;
	private @Inject WebServer server;
	private @Inject IMocksControl control;

	@Test(expected=UnsupportedDatabaseFlavourException.class)
	public void startingShouldFailWhenMysql() throws Exception {
		try {
			control.replay();
			server.start();
		} finally {
			assertThat(server.isStarted()).isFalse();
			control.verify();
		}
	}
}

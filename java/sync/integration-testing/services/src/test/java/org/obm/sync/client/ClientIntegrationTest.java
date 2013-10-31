/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.ServicesClientModule;
import org.obm.sync.ServicesClientModule.ArquillianLocatorService;
import org.obm.sync.ServicesClientModule.CookiesFromClient;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.login.LoginClient;

import com.google.inject.Inject;

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
@GuiceModule(ServicesClientModule.class)
public class ClientIntegrationTest extends ObmSyncIntegrationTest {

	@Inject ArquillianLocatorService locatorService;
	@Inject CalendarClient calendarClient;
	@Inject BookClient bookClient;
	@Inject LoginClient loginClient;
	@Inject CookiesFromClient cookiesFromClient;
	
	@Test
	@RunAsClient
	public void testClientKeepsCookie(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);
		
		AccessToken token = loginClient.login("user1@domain.org", "user1");
		String sid = cookiesFromClient.getSid();
		
		calendarClient.listCalendars(token);
		assertThat(sid).isEqualTo(cookiesFromClient.getSid());

		bookClient.listAllBooks(token);
		assertThat(sid).isEqualTo(cookiesFromClient.getSid());

		loginClient.logout(token);
		assertThat(sid).isEqualTo(cookiesFromClient.getSid());
	}

	@DeployForEachTests
	@Deployment(managed=false, name=ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils.createDeployment();
	}
}
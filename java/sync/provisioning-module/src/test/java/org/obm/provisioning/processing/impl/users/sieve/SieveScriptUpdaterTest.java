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
package org.obm.provisioning.processing.impl.users.sieve;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.sieve.SieveClient;
import org.obm.imap.sieve.SieveScript;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserLogin;

public class SieveScriptUpdaterTest {

	private IMocksControl mocksControl;
	private SieveClient mockSieveClient;
	private SieveBuilder mockSieveBuilder;

	private final ObmUser TEST_USER = ObmUser
			.builder()
			.uid(1)
			.login(UserLogin.valueOf("scipio.africanus"))
			.domain(ObmDomain.builder().name("carthage.tn").build())
			.build();

	@Before
	public void setUp() throws Exception {
		mocksControl = createControl();
		mockSieveClient = mocksControl.createMock(SieveClient.class);
		mockSieveBuilder = mocksControl.createMock(SieveBuilder.class);

		mockSieveClient.logout();
		expectLastCall();
	}

	@Test
	public void updaterShouldUpdateExistingScript() {
		expect(mockSieveClient.listscripts()).andReturn(ImmutableList.of(
				new SieveScript("i am not active", false),
				new SieveScript("i am active", true),
				new SieveScript("i am also not active", false)));
		expect(mockSieveClient.getScriptContent("i am active")).andReturn("old content");
		expect(mockSieveBuilder.buildFromOldContent("old content")).andReturn(
				Optional.of("new content"));
		expect(mockSieveClient.putscript("i am active", "new content")).andReturn(true);

		mocksControl.replay();
		SieveScriptUpdater updater = new SieveScriptUpdater(TEST_USER, mockSieveClient, mockSieveBuilder);
		updater.update();
		mocksControl.verify();
	}

	@Test
	public void updaterShouldDeleteExistingScriptIfUserContentIsMissingAndNoObmRules() {
		Optional<String> absent = Optional.absent();
		expect(mockSieveClient.listscripts()).andReturn(ImmutableList.of(
				new SieveScript("i am not active", false),
				new SieveScript("i am active", true),
				new SieveScript("i am also not active", false)));
		expect(mockSieveClient.getScriptContent("i am active")).andReturn("old content");
		expect(mockSieveBuilder.buildFromOldContent("old content")).andReturn(
				absent);
		mockSieveClient.activate("");
		expectLastCall();
		expect(mockSieveClient.deletescript("i am active")).andReturn(true);

		mocksControl.replay();
		SieveScriptUpdater updater = new SieveScriptUpdater(TEST_USER, mockSieveClient, mockSieveBuilder);
		updater.update();
		mocksControl.verify();
	}

	@Test
	public void updaterShouldCreateNewScriptIfNoScriptIsActive() {
		expect(mockSieveClient.listscripts()).andReturn(ImmutableList.of(
				new SieveScript("i am not active", false),
				new SieveScript("i am also not active", false)));
		expect(mockSieveBuilder.build()).andReturn(Optional.of("new content"));
		expect(
				mockSieveClient.putscript("scipio.africanus-carthage.tn.sieve", "new content"))
				.andReturn(true);
		mockSieveClient.activate("scipio.africanus-carthage.tn.sieve");
		expectLastCall();

		mocksControl.replay();
		SieveScriptUpdater updater = new SieveScriptUpdater(TEST_USER, mockSieveClient, mockSieveBuilder);
		updater.update();
		mocksControl.verify();
	}

	@Test
	public void updaterShouldCreateNewScriptIfNoExistingScripts() {
		List<SieveScript> empty = ImmutableList.of();
		expect(mockSieveClient.listscripts()).andReturn(empty);
		expect(mockSieveBuilder.build()).andReturn(Optional.of("new content"));
		expect(
				mockSieveClient.putscript("scipio.africanus-carthage.tn.sieve", "new content"))
				.andReturn(true);
		mockSieveClient.activate("scipio.africanus-carthage.tn.sieve");
		expectLastCall();

		mocksControl.replay();
		SieveScriptUpdater updater = new SieveScriptUpdater(TEST_USER, mockSieveClient, mockSieveBuilder);
		updater.update();
		mocksControl.verify();
	}

	@Test
	public void updaterShouldNotCreateNewScriptIfNoExistingScriptsAndNoObmRule() {
		List<SieveScript> empty = ImmutableList.of();
		Optional<String> absent = Optional.absent();
		expect(mockSieveClient.listscripts()).andReturn(empty);
		expect(mockSieveBuilder.build()).andReturn(absent);
		expectLastCall();

		mocksControl.replay();
		SieveScriptUpdater updater = new SieveScriptUpdater(TEST_USER, mockSieveClient, mockSieveBuilder);
		updater.update();
		mocksControl.verify();
	}

	@Test(expected = RuntimeException.class)
	public void testUpdateShouldCallLogoutOnFailure() {
		expect(mockSieveClient.listscripts()).andThrow(new RuntimeException());

		mocksControl.replay();
		try {
			new SieveScriptUpdater(TEST_USER, mockSieveClient, mockSieveBuilder).update();
		} finally {
			mocksControl.verify();
		}
	}

}

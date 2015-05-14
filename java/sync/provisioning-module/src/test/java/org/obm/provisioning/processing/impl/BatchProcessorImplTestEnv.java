/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.provisioning.processing.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.obm.provisioning.CommonDomainEndPointEnvTest;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.provisioning.ldap.client.LdapService;
import org.obm.provisioning.mailchooser.ImapBackendChooser;
import org.obm.provisioning.mailchooser.LeastMailboxesImapBackendChooser;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.BatchTracker;
import org.obm.sync.date.DateProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.util.Modules;
import com.google.inject.util.Providers;

public class BatchProcessorImplTestEnv extends CommonDomainEndPointEnvTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			install(Modules.override(new CommonDomainEndPointEnvTest.Env())
					.with(new AbstractModule() {

						@Override
						protected void configure() {
							ImapBackendChooser imapBackendChooser = new LeastMailboxesImapBackendChooser();

							requestInjection(imapBackendChooser);

							bind(BatchProcessor.class).to(BatchProcessorImpl.class);
							bind(BatchTracker.class).to(BatchTrackerImpl.class);
							bind(ImapBackendChooser.class).toProvider(Providers.of(imapBackendChooser));
						}

					}));
		}
	}

	@Inject
	protected BatchProcessor processor;
	@Inject
	protected DateProvider dateProvider;
	@Inject
	private LdapService ldapService;

	protected LdapManager expectLdapBuild() {
		LdapManager ldapManager = mocksControl.createMock(LdapManager.class);

		expect(ldapService.buildManager(isA(LdapConnectionConfig.class)))
				.andReturn(ldapManager);
		return ldapManager;
	}
}

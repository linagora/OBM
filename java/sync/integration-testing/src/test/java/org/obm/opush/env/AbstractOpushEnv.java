/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.opush.env;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.Collections;
import java.util.Date;

import org.easymock.IMocksControl;
import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.ActiveSyncServletModule;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Modules;

public abstract class AbstractOpushEnv extends ActiveSyncServletModule {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private final ClassToInstanceAgregateView<Object> mockMap;
	private final IMocksControl mocksControl;
	private Configuration configuration;
	
	public AbstractOpushEnv() {
		mockMap = new ClassToInstanceAgregateView<Object>();
		mocksControl = createControl();
		configuration = new Configuration();
		configuration.dataDir = Files.createTempDir();
	}

	@Provides
	public ClassToInstanceAgregateView<Object> makeInstanceMapInjectable() {
		return mockMap;
	}
	
	@Provides
	public IMocksControl getMocksControl() {
		return mocksControl;
	}
	
	@Override
	protected Module overrideModule() throws Exception {
		ImmutableList<AbstractOverrideModule> modules = ImmutableList.of( 
			dao(),
			email(),
			obmSync()
		);
		for (AbstractOverrideModule module: modules) {
			mockMap.addMap(module.getMockMap());
		}
		return Modules.combine(
				ImmutableList.<Module>builder()
					.addAll(modules)
					.add(configuration())
					.add(emailConfiguration())
					.build());
	}

	protected ObmSyncModule obmSync() {
		return new ObmSyncModule(mocksControl);
	}

	protected EmailModule email() {
		return new EmailModule(mocksControl);
	}

	protected DaoModule dao() {
		return new DaoModule(mocksControl);
	}

	protected ConfigurationModule configuration() {
		return new ConfigurationModule(configuration);
	}

	protected Module emailConfiguration() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(EmailConfiguration.class).toInstance(new StaticConfigurationService.Email(configuration.mail));
			}
		};
	}
	
	@Provides
	public Configuration configurationProvider() {
		return configuration;
	}
	
	public ClassToInstanceAgregateView<Object> getMockMap() {
		return mockMap;
	}
	
	@Override
	protected void onModuleInstalled() {
		expectOpushStartupRequirements();
	}
	
	private void expectOpushStartupRequirements() {
		if (ObmSyncModule.PUSH_ENABLED) {
			expectDaoRequirements();
		}
	}

	private void expectDaoRequirements() {
		try {
			Date initialSyncDate = new Date(0);
			Date newSyncDate = DateUtils.date("1988-05-27T04:38:01");
			ChangedCollections noChange = new ChangedCollections(newSyncDate, Collections.<SyncCollection>emptySet());

			CollectionDao collectionDao = mockMap.get(CollectionDao.class);
			expect(collectionDao.getContactChangedCollections(initialSyncDate)).andReturn(noChange).once();
			expect(collectionDao.getCalendarChangedCollections(initialSyncDate)).andReturn(noChange).once();
		} catch (DaoException e) {
			// Cannot append
		}
	}
}

/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.CollectionPath.Builder;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CollectionDao;

import com.google.common.collect.Lists;
import com.google.inject.Provider;


public class MappingServiceImplTest {

	@Test
	public void testListCollection() throws DaoException, CollectionPathException {
		SyncKey incomingSyncKey = new SyncKey("1234-12345678-1234");

		String collectionEmail = "obm:\\\\login@domain\\email\\INBOX";
		String collectionCalendar = "obm:\\\\login@domain\\calendar\\login@domain";
		String collectionContacts = "obm:\\\\login@domain\\contacts";
		String collectionTasks = "obm:\\\\login@domain\\tasks\\login@domain";
		CollectionPath expectedCollectionPathEmail = new CollectionPathTest(collectionEmail, PIMDataType.EMAIL, "INBOX");
		CollectionPath expectedCollectionPathCalendar = new CollectionPathTest(collectionCalendar, PIMDataType.CALENDAR, "login@domain");
		CollectionPath expectedCollectionPathContact = new CollectionPathTest(collectionContacts, PIMDataType.CONTACTS, "");
		CollectionPath expectedCollectionPathTasks = new CollectionPathTest(collectionTasks, PIMDataType.TASKS, "login@domain");
		
		UserDataRequest udr = createMock(UserDataRequest.class);
		expect(udr.getDevice()).andReturn(new Device(1, "DevType", new DeviceId("DevId"), null, null));

		CollectionDao collectionDao = mockCollectionDaoForCollectionPaths(
				collectionEmail, collectionCalendar, collectionContacts, collectionTasks);

		Provider<Builder> collectionPathBuilderProvider = createMock(Provider.class);
		mockCollectionPathBuilder(collectionPathBuilderProvider, udr, collectionEmail, expectedCollectionPathEmail);
		mockCollectionPathBuilder(collectionPathBuilderProvider, udr, collectionCalendar, expectedCollectionPathCalendar);
		mockCollectionPathBuilder(collectionPathBuilderProvider, udr, collectionContacts, expectedCollectionPathContact);
		mockCollectionPathBuilder(collectionPathBuilderProvider, udr, collectionTasks, expectedCollectionPathTasks);
		
		replay(udr, collectionDao, collectionPathBuilderProvider);
		
		MappingServiceImpl mappingServiceImpl= new MappingServiceImpl(collectionDao, null, null,collectionPathBuilderProvider);
		List<CollectionPath> listCollections = mappingServiceImpl.listCollections(udr, FolderSyncState.builder()
				.syncKey(incomingSyncKey)
				.build());
		
		assertThat(listCollections).containsOnly(
				expectedCollectionPathEmail,
				expectedCollectionPathCalendar,
				expectedCollectionPathContact,
				expectedCollectionPathTasks);
	}

	private void mockCollectionPathBuilder(Provider<Builder> provider, UserDataRequest userDataRequest,
			String fullyQualifiedCollectionPath, CollectionPath expectedCollectionPath) throws CollectionPathException {
		
		Builder provided = createMock(Builder.class);
		expect(provided.userDataRequest(userDataRequest)).andReturn(provided).once();
		expect(provided.fullyQualifiedCollectionPath(fullyQualifiedCollectionPath)).andReturn(provided).once();
		expect(provided.build()).andReturn(expectedCollectionPath);
		expect(provider.get()).andReturn(provided);
		replay(provided);
	}

	private CollectionDao mockCollectionDaoForCollectionPaths(String...paths) throws DaoException {
		CollectionDao collectionDao = createMock(CollectionDao.class);
		expect(collectionDao.getUserCollections(anyObject(FolderSyncState.class)))
			.andReturn(Lists.newArrayList(paths));
		return collectionDao;
	}
	
	private static class CollectionPathTest extends CollectionPath {
		
		public CollectionPathTest(String collectionPath, PIMDataType pimType, String displayName) {
			super(collectionPath, pimType, displayName);
		}
	}
}

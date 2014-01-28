/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.push.dao.testsuite;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.store.MonitoredCollectionDao;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
public abstract class MonitoredCollectionDaoTest {

	@Inject protected MonitoredCollectionDao monitoredCollectionDao;
	
	private User user;
	private Device device;
	private Credentials credentials;

	@Before
	public void setUp() {
		user = Factory.create().createUser("login@domain", "email@domain", "displayName");
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
		credentials = new Credentials(user, "password");
	}

	
	@Test
	public void testListWhenNoPut() {
		assertThat(monitoredCollectionDao.list(credentials, device)).isEmpty();
	}
	
	@Test
	public void testListWhenPutWithOtherDevice() {
		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		monitoredCollectionDao.put(credentials, otherDevice, buildListCollection(1));
		
		assertThat(monitoredCollectionDao.list(credentials, device)).isEmpty();
	}
	
	@Test
	public void testListWhenPutWithOtherCredentials() {
		Credentials otherCredentials = new Credentials(user, "other");
		monitoredCollectionDao.put(otherCredentials, device, buildListCollection(1));
		
		assertThat(monitoredCollectionDao.list(credentials, device)).isEmpty();
	}
	
	@Test
	public void testOnePut() {
		monitoredCollectionDao.put(credentials, device, buildListCollection(1));
		
		Collection<AnalysedSyncCollection> syncCollections = monitoredCollectionDao.list(credentials, device);
		
		assertThat(monitoredCollectionDao.list(credentials, device)).hasSize(1);
		containsCollectionWithId(syncCollections, 1);
	}
	
	@Test
	public void testTwoPut() {
		monitoredCollectionDao.put(credentials, device, buildListCollection(1));
		monitoredCollectionDao.put(credentials, device, buildListCollection(2, 3));

		Collection<AnalysedSyncCollection> syncCollections = monitoredCollectionDao.list(credentials, device);
		
		assertThat(monitoredCollectionDao.list(credentials, device)).hasSize(2);
		containsCollectionWithId(syncCollections, 2);
		containsCollectionWithId(syncCollections, 3);
	}
	
	private void containsCollectionWithId(Collection<AnalysedSyncCollection> syncCollections, Integer id) {
		boolean find = false;
		for(AnalysedSyncCollection col : syncCollections){
			if(col.getCollectionId() == id){
				find = true;
			}
		}
		assertThat(find).isTrue();
	}

	private Set<AnalysedSyncCollection> buildListCollection(Integer... ids) {
		Set<AnalysedSyncCollection> cols = Sets.newHashSet();
		for(Integer id : ids){
			cols.add(AnalysedSyncCollection.builder()
					.collectionId(id)
					.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
					.build());
		}
		return cols;
	}
}

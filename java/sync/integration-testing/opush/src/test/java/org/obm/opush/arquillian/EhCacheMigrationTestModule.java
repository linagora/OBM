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
package org.obm.opush.arquillian;

import static org.easymock.EasyMock.expect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.obm.opush.env.arquillian.AuthenticatedArquillianModule;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Credentials;
import org.obm.push.store.HeartbeatDao;
import org.obm.push.store.MonitoredCollectionDao;

import com.google.common.collect.ImmutableSet;

public class EhCacheMigrationTestModule  extends AuthenticatedArquillianModule {
	
	@Override
	protected void expectedBehaviour() throws Exception {
		super.expectedBehaviour();
		expectHeartbeatDao();
		expectMonitoredCollectionDao();
		copyCacheFilesInDataFolder();
	}
	
	private void expectHeartbeatDao() {
		HeartbeatDao heartbeatDao = mockMap.get(HeartbeatDao.class);
		expect(heartbeatDao.findLastHeartbeat(device))
			.andReturn(Long.valueOf(1));
	}
	
	private void expectMonitoredCollectionDao() {
		MonitoredCollectionDao monitoredCollectionDao = mockMap.get(MonitoredCollectionDao.class);
		expect(monitoredCollectionDao.list(new Credentials(user, password), device))
			.andReturn(ImmutableSet.<AnalysedSyncCollection> of());
	}
	
	private void copyCacheFilesInDataFolder() throws Exception {
		copyFileInDataFolder("mailSnapshotStore.data");
		copyFileInDataFolder("mailSnapshotStore.index");
		copyFileInDataFolder("mailWindowingChunksStore.data");
		copyFileInDataFolder("mailWindowingChunksStore.index");
		copyFileInDataFolder("mailWindowingIndexStore.data");
		copyFileInDataFolder("mailWindowingIndexStore.index");
		copyFileInDataFolder("monitoredCollectionService.data");
		copyFileInDataFolder("monitoredCollectionService.index");
		copyFileInDataFolder("syncedCollectionStoreService.data");
		copyFileInDataFolder("syncedCollectionStoreService.index");
		copyFileInDataFolder("syncKeysStore.data");
		copyFileInDataFolder("syncKeysStore.index");
		copyFileInDataFolder("unsynchronizedItemService.data");
		copyFileInDataFolder("unsynchronizedItemService.index");
	}

	private void copyFileInDataFolder(String fileName) throws IOException, FileNotFoundException {
		IOUtils.copy(ClassLoader.getSystemResourceAsStream("ehcache" + File.separator + fileName), 
				new FileOutputStream(new File(configuration.dataDir.getCanonicalPath() + File.separatorChar + fileName)));
	}
}

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
package org.obm.push;

import java.util.Date;
import java.util.Map.Entry;

import org.obm.push.backend.FolderBackend;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.HierarchyItemsChanges.Builder;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HierarchyExporter implements IHierarchyExporter {

	private final FolderBackend folderExporter;
	private final Backends backends;

	@Inject
	@VisibleForTesting HierarchyExporter(FolderBackend folderExporter, Backends backends) {
		this.folderExporter = folderExporter;
		this.backends = backends;
	}

	@Override
	public HierarchyItemsChanges getChanged(UserDataRequest udr, Date lastSync) throws DaoException {
		Builder builder = new HierarchyItemsChanges.Builder();
		for (Entry<PIMDataType, PIMBackend> entry: backends.getBackends().entrySet()) {
			HierarchyItemsChanges hierarchyChanges = entry.getValue().getHierarchyChanges(udr, lastSync);
			// FIXME : The last sync is only used by contact folders sync
			if (entry.getKey() == PIMDataType.CONTACTS) {
				builder.lastSync(hierarchyChanges.getLastSync());
			}
			builder.mergeItems(hierarchyChanges);
		}
		return builder.build();
	}
	
	@Override
	public String getRootFolderUrl(UserDataRequest udr) {
		return folderExporter.getColName(udr);
	}
}

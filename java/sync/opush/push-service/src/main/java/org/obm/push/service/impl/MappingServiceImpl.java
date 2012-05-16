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
package org.obm.push.service.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MappingServiceImpl implements MappingService {

	private final CollectionDao collectionDao;

	@Inject
	private MappingServiceImpl(CollectionDao collectionDao) {
		this.collectionDao = collectionDao;
	}

	@Override
	public Integer getItemIdFromServerId(String serverId) {
		if (serverId != null) {
			String[] idx = serverId.split(":");
			if (idx.length == 2) {
				return Integer.parseInt(idx[1]);
			}
		}
		return null;
	}

	@Override
	public Integer getCollectionIdFromServerId(String serverId) {
		if (serverId != null) {
			String[] idx = serverId.split(":");
			if (idx.length == 2) {
				return Integer.parseInt(idx[0]);
			}
		}
		return null;
	}
	

	
	@Override
	public String collectionIdToString(Integer collectionId) {
		return String.valueOf(collectionId);
	}

	@Override
	public String createCollectionMapping(Device device, String col) throws DaoException {
		return collectionDao.addCollectionMapping(device, col).toString();
	}
	
	@Override
	public String getCollectionPathFor(Integer collectionId) throws CollectionNotFoundException, DaoException {
		return collectionDao.getCollectionPath(collectionId);
	}
	
	@Override
	public List<ItemChange> buildItemsToDeleteFromUids(Integer collectionId, Collection<Long> uids) {
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (Long uid: uids) {
			deletions.add( getItemChange(collectionId, uid.toString()) );
		}
		return deletions;
	}


	@Override
	public ItemChange getItemChange(Integer collectionId, String clientId) {
		return new ItemChange( getServerIdFor(collectionId, clientId) );
	}

	@Override
	public String getServerIdFor(Integer collectionId, String clientId) {
		if (collectionId == null || Strings.isNullOrEmpty(clientId)) {
			return null;
		}
		StringBuilder sb = new StringBuilder(10);
		sb.append(collectionId);
		sb.append(':');
		sb.append(clientId);
		return sb.toString();
	}
	
	@Override
	public Integer getCollectionIdFor(Device device, String collection) throws CollectionNotFoundException, DaoException {
		Integer collectionId = collectionDao.getCollectionMapping(device, collection);
		if (collectionId == null) {
			throw new CollectionNotFoundException("Collection {" + collection + "} not found.");
		}
		return collectionId;
	}

	
}

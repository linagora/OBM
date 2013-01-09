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
package org.obm.push.protocol.data;

import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.exception.activesync.ServerErrorException;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncRequestCollection;
import org.obm.push.protocol.bean.SyncRequestCollectionCommand;
import org.obm.push.protocol.bean.SyncRequestCollectionCommands;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncAnalyser {

	private static final Logger logger = LoggerFactory.getLogger(SyncAnalyser.class);

	private static final int DEFAULT_WAIT = 0;
	private static final int DEFAULT_WINDOW_SIZE = 100;
	
	private final CollectionDao collectionDao;
	private final SyncedCollectionDao syncedCollectionStoreService;
	private final CollectionPathHelper collectionPathHelper;
	private final DecoderFactory decoderFactory;

	@Inject
	protected SyncAnalyser(SyncedCollectionDao syncedCollectionStoreService,
			CollectionDao collectionDao, CollectionPathHelper collectionPathHelper,
			DecoderFactory decoderFactory) {
		this.collectionDao = collectionDao;
		this.syncedCollectionStoreService = syncedCollectionStoreService;
		this.collectionPathHelper = collectionPathHelper;
		this.decoderFactory = decoderFactory;
	}

	public Sync analyseSync(UserDataRequest userDataRequest, SyncRequest syncRequest) 
			throws PartialException, ProtocolException, DaoException, CollectionPathException {
		assertNotPartialRequest(syncRequest);

		Sync.Builder builder = Sync.builder()
				.waitInMinutes(getWait(syncRequest));
	
		int defaultRequestWindowSize = getWindowSize(syncRequest);

		for (SyncRequestCollection syncRequestCollection : syncRequest.getCollections()) {
			builder.addCollection(getCollection(userDataRequest, syncRequestCollection,
					defaultRequestWindowSize, false));
		}
		return builder.build();
	}

	private void assertNotPartialRequest(SyncRequest syncRequest) {
		if (syncRequest.isPartial() != null && syncRequest.isPartial()) {
			throw new PartialException();
		}
	}
	
	private int getWindowSize(SyncRequest syncRequest) {
		if (syncRequest.getWindowSize() != null) {
			return syncRequest.getWindowSize();
		}
		return DEFAULT_WINDOW_SIZE;
	}

	private int getWait(SyncRequest syncRequest) {
		if (syncRequest.getWaitInMinute() != null) {
			return syncRequest.getWaitInMinute();
		}
		return DEFAULT_WAIT;
	}

	private SyncCollection getCollection(UserDataRequest udr, SyncRequestCollection requestCollection,
			int requestDefaultWindowSize, boolean isPartial)
			throws PartialException, ProtocolException, DaoException, CollectionPathException{
		
		SyncCollection collection = new SyncCollection();
		int collectionId = requestCollection.getId();
		try {
			collection.setCollectionId(collectionId);
			collection.setSyncKey(requestCollection.getSyncKey());

			if (requestCollection.hasWindowSize()) {
				collection.setWindowSize(requestCollection.getWindowSize());
			} else {
				collection.setWindowSize(requestDefaultWindowSize);
			}

			SyncCollection lastSyncCollection = findLastSyncedCollectionOptions(udr, isPartial, collectionId);
			collection.setOptions(getUpdatedOptions(lastSyncCollection, requestCollection));
			
			recognizeCollection(collection, collectionId, requestCollection.getDataClass());
			syncedCollectionStoreService.put(udr.getCredentials(), udr.getDevice(), collection);
			appendCommand(collection, requestCollection);
		} catch (CollectionNotFoundException e) {
			collection.setStatus(SyncStatus.OBJECT_NOT_FOUND);
		}
		// TODO sync supported
		// TODO sync <getchanges/>

		return collection;
	}

	private SyncCollection findLastSyncedCollectionOptions(UserDataRequest udr, boolean isPartial, int collectionId) {
		SyncCollection lastSyncCollection = 
				syncedCollectionStoreService.get(udr.getCredentials(), udr.getDevice(), collectionId);
		if (isPartial && lastSyncCollection == null) {
			throw new PartialException();
		}
		return lastSyncCollection;
	}

	private void recognizeCollection(SyncCollection collection, int collectionId, String dataClass) {
		try {
 			String collectionPath = collectionDao.getCollectionPath(collectionId);
 			collection.setCollectionPath(collectionPath);
 			PIMDataType dataType = collectionPathHelper.recognizePIMDataType(collectionPath);
 			collection.setDataType(dataType);
 			if (isDifferentClassThanType(dataClass, dataType)) {
 				String msg = "The type of the collection found:{%s} is not the same than received in DataClass:{%s}";
				throw new ServerErrorException(String.format(msg, dataType.asXmlValue() , dataClass));
 			}
		} catch (CollectionNotFoundException e) {
			collection.setStatus(SyncStatus.OBJECT_NOT_FOUND);
 		}
	}

	private boolean isDifferentClassThanType(String dataClass, PIMDataType dataType) {
		return !Strings.isNullOrEmpty(dataClass) && !dataType.asXmlValue().equals(dataClass);
	}

	private SyncCollectionOptions getUpdatedOptions(SyncCollection lastSyncCollection, SyncRequestCollection requestCollection) {
		SyncCollectionOptions lastUsedOptions = null;
		if (lastSyncCollection != null) {
			lastUsedOptions = lastSyncCollection.getOptions();
		}
		
		if (!requestCollection.hasOptions() && lastUsedOptions != null) {
			return lastUsedOptions;
		} else if (requestCollection.hasOptions()) {
			return SyncCollectionOptions.cloneOnlyByExistingFields(requestCollection.getOptions());
		}
		return new SyncCollectionOptions();
	}

	private void appendCommand(SyncCollection collection, SyncRequestCollection requestCollection) {
		SyncRequestCollectionCommands commands = requestCollection.getCommands();
		if (commands != null) {
			collection.setFetchIds(commands.getFetchIds());
			
			for (SyncRequestCollectionCommand command : commands.getCommands()) {
				collection.addChange(getChange(collection, command));
			}
		}
	}

	private SyncCollectionChange getChange(SyncCollection collection, SyncRequestCollectionCommand command) {
		SyncCommand syncCommand = SyncCommand.fromSpecificationValue(command.getName());
		String serverId = command.getServerId();
		String clientId = command.getClientId();
		Element syncData = command.getApplicationData();
		
		IApplicationData data = decode(syncData, collection.getDataType());
		if (data == null) {
			logger.error("Decoding failed for " + collection.getDataType());
		}
		return new SyncCollectionChange(serverId, clientId, syncCommand, data, collection.getDataType());
	}

	protected IApplicationData decode(Element data, PIMDataType dataType) {
		return decoderFactory.decode(data, dataType);
	}

}

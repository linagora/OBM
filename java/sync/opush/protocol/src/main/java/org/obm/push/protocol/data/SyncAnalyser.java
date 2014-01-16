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
package org.obm.push.protocol.data;

import java.util.List;

import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollectionCommand;
import org.obm.push.bean.SyncCollectionCommands;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncCollectionRequest;
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
import org.obm.push.store.CollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.w3c.dom.Element;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncAnalyser {

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
				.waitInMinutes(syncRequest.getWaitInMinute());
	
		for (SyncCollectionRequest syncCollectionRequest : syncRequest.getCollections()) {
			builder.addCollection(getCollection(userDataRequest, syncCollectionRequest, false));
		}
		return builder.build();
	}

	private void assertNotPartialRequest(SyncRequest syncRequest) {
		if (syncRequest.isPartial() != null && syncRequest.isPartial()) {
			throw new PartialException();
		}
	}
	
	private AnalysedSyncCollection getCollection(UserDataRequest udr, SyncCollectionRequest collectionRequest, boolean isPartial)
			throws PartialException, ProtocolException, DaoException, CollectionPathException{
		
		AnalysedSyncCollection.Builder builder = AnalysedSyncCollection.builder();
		int collectionId = collectionRequest.getCollectionId();
		try {
			builder.collectionId(collectionId)
				.syncKey(collectionRequest.getSyncKey())
				.windowSize(collectionRequest.getWindowSize())
				.options(getUpdatedOptions(
						findLastSyncedCollectionOptions(udr, isPartial, collectionId), collectionRequest))
				.status(SyncStatus.OK);
			
			PIMDataType dataType = recognizeCollection(builder, collectionId, collectionRequest.getDataClass());
			builder.commands(analyseCommands(collectionRequest.getCommands(), dataType));
			
			AnalysedSyncCollection analysed = builder.build();
			syncedCollectionStoreService.put(udr.getCredentials(), udr.getDevice(), analysed);
			return analysed;
		} catch (CollectionNotFoundException e) {
			return builder.status(SyncStatus.OBJECT_NOT_FOUND).build();
		}
		// TODO sync supported
		// TODO sync <getchanges/>

	}

	private AnalysedSyncCollection findLastSyncedCollectionOptions(UserDataRequest udr, boolean isPartial, int collectionId) {
		AnalysedSyncCollection lastSyncCollection = 
				syncedCollectionStoreService.get(udr.getCredentials(), udr.getDevice(), collectionId);
		if (isPartial && lastSyncCollection == null) {
			throw new PartialException();
		}
		return lastSyncCollection;
	}

	private PIMDataType recognizeCollection(AnalysedSyncCollection.Builder builder, int collectionId, String dataClass) {
		String collectionPath = collectionDao.getCollectionPath(collectionId);
		PIMDataType dataType = collectionPathHelper.recognizePIMDataType(collectionPath);
		builder.collectionPath(collectionPath)
			.dataType(dataType);
		
		if (isDifferentClassThanType(dataClass, dataType)) {
			String msg = "The type of the collection found:{%s} is not the same than received in DataClass:{%s}";
			throw new ServerErrorException(String.format(msg, dataType.asXmlValue() , dataClass));
		}
		return dataType;
	}

	private boolean isDifferentClassThanType(String dataClass, PIMDataType dataType) {
		return !Strings.isNullOrEmpty(dataClass) && !dataType.asXmlValue().equals(dataClass);
	}

	private SyncCollectionOptions getUpdatedOptions(AnalysedSyncCollection lastSyncCollection, SyncCollectionRequest requestCollection) {
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

	private SyncCollectionCommands.Response analyseCommands(SyncCollectionCommands.Request requestCommands, PIMDataType dataType) {
		SyncCollectionCommands.Response.Builder commandsResponseBuilder = SyncCollectionCommands.Response.builder();
		List<SyncCollectionCommand.Request> commands = requestCommands.getCommands();
		if (commands != null) {
			for (SyncCollectionCommand.Request command : commands) {
				SyncCommand type = command.getType();
				commandsResponseBuilder.addCommand(
					SyncCollectionCommand.Response.builder()
						.commandType(type)
						.serverId(command.getServerId())
						.clientId(command.getClientId())
						.applicationData(decodeApplicationData(command.getApplicationData(), dataType, type))
						.build());
			}
		}
		return commandsResponseBuilder.build();
	}

	private IApplicationData decodeApplicationData(Element applicationData, PIMDataType dataType, SyncCommand syncCommand) {
		if (syncCommand.requireApplicationData()) {
			IApplicationData data = decode(applicationData, dataType);
			if (data == null) {
				throw new ProtocolException("No decodable " + dataType + " data for " + applicationData);
			}
			return data;
		}
		return null;
	}

	protected IApplicationData decode(Element data, PIMDataType dataType) {
		return decoderFactory.decode(data, dataType);
	}

}

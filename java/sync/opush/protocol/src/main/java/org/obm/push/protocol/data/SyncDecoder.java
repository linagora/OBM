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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncDecoder {

	private static final Logger logger = LoggerFactory.getLogger(SyncDecoder.class);
	
	private final CollectionDao collectionDao;
	private final SyncedCollectionDao syncedCollectionStoreService;
	private final Map<PIMDataType, IDataDecoder> decoders;

	private final CollectionPathHelper collectionPathHelper;

	@Inject
	private SyncDecoder(SyncedCollectionDao syncedCollectionStoreService,
			CollectionDao collectionDao, CollectionPathHelper collectionPathHelper,
			Base64ASTimeZoneDecoder base64AsTimeZoneDecoder, ASTimeZoneConverter asTimeZoneConverter) {
		this.collectionDao = collectionDao;
		this.syncedCollectionStoreService = syncedCollectionStoreService;
		this.collectionPathHelper = collectionPathHelper;
		this.decoders = ImmutableMap.<PIMDataType, IDataDecoder>builder()
				.put(PIMDataType.CONTACTS, new ContactDecoder(base64AsTimeZoneDecoder, asTimeZoneConverter))
				.put(PIMDataType.CALENDAR, new CalendarDecoder(base64AsTimeZoneDecoder, asTimeZoneConverter))
				.put(PIMDataType.EMAIL, new EmailDecoder(base64AsTimeZoneDecoder, asTimeZoneConverter))
				.put(PIMDataType.TASKS, new TaskDecoder(base64AsTimeZoneDecoder, asTimeZoneConverter))
				.build();
	}

	public Sync decodeSync(Document doc, UserDataRequest userDataRequest) 
			throws PartialException, ProtocolException, DaoException, CollectionPathException {
		Sync ret = new Sync();
		Element root = doc.getDocumentElement();
		ret.setWait(getWait(root));

		Boolean isPartial = getPartial(root);
		if (isPartial) {
			throw new PartialException();
		}
		NodeList nl = root.getElementsByTagName("Collection");
		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			SyncCollection collec = getCollection(userDataRequest, col, isPartial);
			ret.addCollection(collec);
		}
		syncedCollectionStoreService.put(userDataRequest.getCredentials(), userDataRequest.getDevice(), ret.getCollections());
		return ret;
	}

	private boolean getPartial(Element root) {
		return root.getElementsByTagName("Partial").getLength() > 0;
	}

	private Integer getWait(Element root) {
		Integer ret = 0;
		String wait = DOMUtils.getElementText(root, "Wait");
		if (wait != null && wait.length() > 0) {
			try {
				ret = Integer.parseInt(wait);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}

	private SyncCollection getCollection(UserDataRequest udr, Element col, boolean isPartial)
			throws PartialException, ProtocolException, DaoException, CollectionPathException{
		
		SyncCollection collection = new SyncCollection();
		Integer collectionId = getCollectionId(col);
		if (collectionId == null) {
			throw new ProtocolException("CollectionId can't be null");
		}
		SyncCollection lastSyncCollection = 
				syncedCollectionStoreService.get(udr.getCredentials(), udr.getDevice(), collectionId);
		if (isPartial && lastSyncCollection == null) {
			throw new PartialException();
		}
		try {
			collection.setCollectionId(collectionId);
			String collectionPath = collectionDao.getCollectionPath(collectionId);
			collection.setCollectionPath(collectionPath);
			PIMDataType dataType = collectionPathHelper.recognizePIMDataType(collectionPath);
			collection.setDataType(dataType);
			collection.setDataClass(DOMUtils.getElementText(col, "Class"));
			collection.setSyncKey(DOMUtils.getElementText(col, "SyncKey"));

			Element windowSizeElement = DOMUtils.getUniqueElement(col, "WindowSize");
			if (windowSizeElement != null) {
				collection.setWindowSize(Integer.parseInt(windowSizeElement.getTextContent()));
			}
			
			SyncCollectionOptions options = getUpdatedOptions(lastSyncCollection, col);
			collection.setOptions(options);
			
			appendCommand(col, collection);
		} catch (CollectionNotFoundException e) {
			collection.setError(SyncStatus.OBJECT_NOT_FOUND);
		}
		// TODO sync supported
		// TODO sync <getchanges/>

		return collection;
	}

	private SyncCollectionOptions getUpdatedOptions(SyncCollection lastSyncCollection,
			Element collectionElement) {
		SyncCollectionOptions options = null;
		if(lastSyncCollection != null){
			options = lastSyncCollection.getOptions();
		}
		if(options == null){
			options = new SyncCollectionOptions();
		}
		
		Element optionsElement = DOMUtils.getUniqueElement(collectionElement, "Options");
		if (optionsElement != null) {
			String filterTypeElement = DOMUtils.getElementText(optionsElement, "FilterType");
			options.setFilterType(FilterType.getFilterType(filterTypeElement));
			
			String mimeSupport = DOMUtils.getElementText(optionsElement, "MIMESupport");
			if (mimeSupport != null) {
				options.setMimeSupport(Integer.parseInt(mimeSupport));
			}
			
			String mimeTruncation = DOMUtils.getElementText(optionsElement,	"MIMETruncation");
			if (mimeTruncation != null) {
				options.setMimeTruncation(Integer.parseInt(mimeTruncation));
			}
			
			String conflict = DOMUtils.getElementText(optionsElement, "Conflict");
			if (conflict != null) {
				options.setConflict(Integer.parseInt(conflict));
			}

			String truncation = DOMUtils.getElementText(optionsElement, "Truncation");
			if (truncation != null) {
				options.setTruncation(Integer.parseInt(truncation));
			}
			
			String deletesAsMoves = DOMUtils.getElementText(optionsElement,	"DeletesAsMoves");
			if(deletesAsMoves != null){
				if ("0".equals(deletesAsMoves)) {
					options.setDeletesAsMoves(false);
				} else {
					options.setDeletesAsMoves(true);
				}
			}

			options.setBodyPreferences(getBodyPreference(optionsElement));
		}
		return options;
	}

	private List<BodyPreference> getBodyPreference(Element optionsElement) {
		NodeList bodyPreferences = optionsElement.getElementsByTagName("BodyPreference");
		List<BodyPreference> preferences = new ArrayList<BodyPreference>();
		if (bodyPreferences != null) {
			for (int i = 0; i < bodyPreferences.getLength(); i++) {
				Element bodyPreference = (Element) bodyPreferences.item(i);
				String truncationSize = DOMUtils.getElementText(bodyPreference, "TruncationSize");
				String type = DOMUtils.getElementText(bodyPreference, "Type");
				String allOrNone = DOMUtils.getElementText(bodyPreference, "AllOrNone");
				BodyPreference.Builder bp = new BodyPreference.Builder().
						bodyType(MSEmailBodyType.getValueOf(Integer.parseInt(type))).allOrNone(Boolean.valueOf(allOrNone));
				if (truncationSize != null) {
					bp.truncationSize(Integer.parseInt(truncationSize));
				}
				preferences.add(bp.build());
			}
		}
		return preferences;
	}

	private Integer getCollectionId(Element col) {
		Element fid = DOMUtils.getUniqueElement(col, "CollectionId");
		if (fid != null) {
			try {
				return Integer.parseInt(fid.getTextContent());
			} catch (NumberFormatException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	private void appendCommand(Element col, SyncCollection collection) {
		Element perform = DOMUtils.getUniqueElement(col, "Commands");
		if (perform != null) {
			NodeList fetchs = perform.getElementsByTagName("Fetch");
			List<String> fetchIds = new LinkedList<String>();
			for (int i = 0; i < fetchs.getLength(); i++) {
				Element fetch = (Element) fetchs.item(i);
				fetchIds.add(DOMUtils.getElementText(fetch, "ServerId"));
			}
			collection.setFetchIds(fetchIds);
			// get our sync state for this collection
			NodeList mod = perform.getChildNodes();
			for (int j = 0; j < mod.getLength(); j++) {
				Element modification = (Element) mod.item(j);
				collection.addChange(getChange(collection, modification));
			}
		}
	}

	private SyncCollectionChange getChange(SyncCollection collection,
			Element modification) {
		String modType = modification.getNodeName();
		String serverId = DOMUtils.getElementText(modification, "ServerId");
		String clientId = DOMUtils.getElementText(modification, "ClientId");
		Element syncData = DOMUtils.getUniqueElement(modification,
				"ApplicationData");
		IDataDecoder dd = getDecoder(collection.getDataType());
		IApplicationData data = null;
		if (dd != null) {
			if (syncData != null) {
				data = dd.decode(syncData);
			}
		} else {
			logger.error("no decoder for " + collection.getDataType());
			if (modType.equals("Fetch")) {
				logger.info("adding id to fetch " + serverId);
				collection.getFetchIds().add(serverId);
			}
		}
		return new SyncCollectionChange(serverId, clientId, modType, data,
				collection.getDataType());
	}

	protected IDataDecoder getDecoder(PIMDataType dataClass) {
		return decoders.get(dataClass);
	}

}

package org.obm.push.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.Sync;
import org.obm.push.data.CalendarDecoder;
import org.obm.push.data.ContactDecoder;
import org.obm.push.data.EmailDecoder;
import org.obm.push.data.IDataDecoder;
import org.obm.push.data.TaskDecoder;
import org.obm.push.exception.PartialException;
import org.obm.push.exception.ProtocolException;
import org.obm.push.store.BodyPreference;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.FilterType;
import org.obm.push.store.IApplicationData;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.MSEmailBodyType;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncCollectionChange;
import org.obm.push.store.SyncStatus;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncDecoder {

	private static final Logger logger = LoggerFactory
			.getLogger(SyncDecoder.class);
	
	private ISyncStorage store;
	private Map<PIMDataType, IDataDecoder> decoders;

	@Inject
	private SyncDecoder(ISyncStorage store) {
		this.store = store;
		this.decoders = new HashMap<PIMDataType, IDataDecoder>();
		decoders.put(PIMDataType.CONTACTS, new ContactDecoder());
		decoders.put(PIMDataType.CALENDAR, new CalendarDecoder());
		decoders.put(PIMDataType.EMAIL, new EmailDecoder());
		decoders.put(PIMDataType.TASKS, new TaskDecoder());
	}

	public Sync decodeSync(Document doc, BackendSession bs)
			throws PartialException, ProtocolException {
		Sync ret = new Sync();
		Element root = doc.getDocumentElement();
		ret.setWait(getWait(root));

		Boolean isPartial = getPartial(root);
		if (isPartial) {
			logger.info("Partial element has been found. Collection(s) will be from cache");
		}
		NodeList nl = root.getElementsByTagName("Collection");
		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			SyncCollection collec = getCollection(bs, col, isPartial,
					bs.getLastMonitoredById());
			ret.addCollection(collec);
		}
		if (ret.getWaitInSecond() > 0 && ret.getCollections().size() == 0
				&& bs.getLastSync() != null) {
			ret.getCollections().addAll(bs.getLastSync().getCollections());
		}
		if (ret.getCollections().size() == 0) {
			throw new PartialException();
		}
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

	private SyncCollection getCollection(BackendSession bs, Element col,
			boolean isPartial, Map<Integer, SyncCollection> lastMonitored)
			throws PartialException, ProtocolException{
		SyncCollection collection = new SyncCollection();
		Integer collectionId = getCollectionId(col);
		if (collectionId == null) {
			throw new ProtocolException("CollectionId can't be null");
		}
		if (isPartial && lastMonitored.get(collectionId) == null) {
			throw new PartialException();
		}
		try {
			collection.setCollectionId(collectionId);
			String collectionPath = store.getCollectionPath(collectionId);
			collection.setCollectionPath(collectionPath);
			PIMDataType dataType = store.getDataClass(collectionPath);
			collection.setDataType(dataType);
			collection.setDataClass(DOMUtils.getElementText(col, "Class"));
			collection.setSyncKey(DOMUtils.getElementText(col, "SyncKey"));

			Element wse = DOMUtils.getUniqueElement(col, "WindowSize");
			if (wse != null) {
				collection.setWindowSize(Integer.parseInt(wse.getTextContent()));
			}
			
			Element option = DOMUtils.getUniqueElement(col, "Options");
			if (option != null) {
				String truncation = DOMUtils.getElementText(option, "Truncation");

				String mimeSupport = DOMUtils.getElementText(option, "MIMESupport");
				String mimeTruncation = DOMUtils.getElementText(option,
						"MIMETruncation");
				String conflict = DOMUtils.getElementText(option, "Conflict");
				String deletesAsMoves = DOMUtils.getElementText(option,
						"DeletesAsMoves");
				NodeList bodyPreferences = col
						.getElementsByTagName("BodyPreference");

				collection.setFilterType(getFilterType(bs, collectionId, option));

				if (conflict != null) {
					collection.setConflict(Integer.parseInt(conflict));
				}
				if (mimeSupport != null) {
					collection.setMimeSupport(Integer.parseInt(mimeSupport));
				}
				if (mimeTruncation != null) {
					collection.setMimeTruncation(Integer.parseInt(mimeTruncation));
				}
				if (truncation != null) {
					collection.setTruncation(Integer.parseInt(truncation));
				}
				if ("0".equals(deletesAsMoves)) {
					collection.setDeletesAsMoves(false);
				} else {
					collection.setDeletesAsMoves(true);
				}

				if (bodyPreferences != null) {
					for (int i = 0; i < bodyPreferences.getLength(); i++) {
						Element bodyPreference = (Element) bodyPreferences.item(i);
						String truncationSize = DOMUtils.getElementText(
								bodyPreference, "TruncationSize");
						String type = DOMUtils.getElementText(bodyPreference,
								"Type");
						BodyPreference bp = new BodyPreference();
						// nokia n900 sets type without truncationsize
						if (truncationSize != null) {
							bp.setTruncationSize(Integer.parseInt(truncationSize));
						}
						bp.setType(MSEmailBodyType.getValueOf(Integer
								.parseInt(type)));
						collection.addBodyPreference(bp);
					}
				}
			} else {
				collection.setFilterType( getFilterType(bs, collectionId));
			}
			appendCommand(col, collection);
		} catch (CollectionNotFoundException e) {
			collection.setError(SyncStatus.OBJECT_NOT_FOUND);
		}
		// TODO sync supported
		// TODO sync <getchanges/>
		// TODO sync options

		return collection;
	}

	private FilterType getFilterType(BackendSession bs, Integer collectionId,
			Element option) {
		String filterType = DOMUtils.getElementText(option, "FilterType");
		if (filterType != null) {
			return FilterType.getFilterType(filterType);
		} else {
			return getFilterType(bs, collectionId);
		}
	}
	
	private FilterType getFilterType(BackendSession bs, Integer collectionId) {
		return bs.getLastFilterType(collectionId);
	}

	private Integer getCollectionId(Element col) {
		Element fid = DOMUtils.getUniqueElement(col, "CollectionId");
		if (fid != null) {
			try {
				return Integer.parseInt(fid.getTextContent());
			} catch (NumberFormatException e) {
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

package org.obm.push.protocol;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.bean.SyncRequest;
import org.obm.push.bean.SyncResponse;
import org.obm.push.data.EncoderFactory;
import org.obm.push.data.IDataEncoder;
import org.obm.push.exception.NoDocumentException;
import org.obm.push.exception.PartialException;
import org.obm.push.exception.ProtocolException;
import org.obm.push.impl.SyncDecoder;
import org.obm.push.store.BodyPreference;
import org.obm.push.store.IApplicationData;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncStatus;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncProtocol {
	
	private final SyncDecoder syncDecoder;

	@Inject
	private SyncProtocol(SyncDecoder syncDecoder) {
		this.syncDecoder = syncDecoder;
	}
	
	public SyncRequest getRequest(Document doc, BackendSession bs) throws NoDocumentException, PartialException, ProtocolException {
		if (doc == null) {
			throw new NoDocumentException();
		}
		return new SyncRequest( syncDecoder.decodeSync(doc, bs) );
	}

	public Document endcodeResponse(SyncResponse syncResponse) {
		Document reply = DOMUtils.createDoc(null, "Sync");
		Element root = reply.getDocumentElement();
		
		final Element cols = DOMUtils.createElement(root, "Collections");
		for (SyncCollection syncCollection : syncResponse.listChangedFolders()) {

			Element ce = DOMUtils.createElement(cols, "Collection");
			if (syncCollection.getDataClass() != null) {
				DOMUtils.createElementAndText(ce, "Class", syncCollection.getDataClass());
			}
			
			if (!syncCollection.isSyncStatevalid()) {
				DOMUtils.createElementAndText(ce, "CollectionId", syncCollection.getCollectionId().toString());
				DOMUtils.createElementAndText(ce, "Status", SyncStatus.INVALID_SYNC_KEY.asXmlValue());
				DOMUtils.createElementAndText(ce, "SyncKey", "0");
			} else {
				Element sk = DOMUtils.createElement(ce, "SyncKey");
				DOMUtils.createElementAndText(ce, "CollectionId", syncCollection.getCollectionId().toString());
				DOMUtils.createElementAndText(ce, "Status", SyncStatus.OK.asXmlValue());

				if (!syncCollection.getSyncKey().equals("0")) {
					if (syncCollection.getFetchIds().size() == 0) {
						buildUpdateItemChange(syncResponse.getBackendSession(), syncCollection, syncResponse.listProcessedClientIds(), ce, syncResponse.getEncoderFactory());
					} else {
						buildFetchItemChange(syncResponse.getBackendSession(), syncCollection, ce, syncResponse.getEncoderFactory());
					}
				}
				
				sk.setTextContent(syncCollection.getAllocateNewSyncKey());
			}
			
		}
		return reply;
	}
	
	public Document encodeResponse() {
		Document reply = DOMUtils.createDoc(null, "Sync");
		Element root = reply.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", SyncStatus.WAIT_INTERVAL_OUT_OF_RANGE.asXmlValue());
		DOMUtils.createElementAndText(root, "Limit", "59");
		return reply;
	}
	
	public Document encodeResponse(String error) {
		return buildErrorResponse("Sync", error);
	}	
	
	private Document buildErrorResponse(String type, String error) {
		Document ret = DOMUtils.createDoc(null, type);
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", error);
		return ret;
	}

	private void buildFetchItemChange(BackendSession bs, SyncCollection c, Element ce, EncoderFactory encoderFactory) {
		Element commands = DOMUtils.createElement(ce, "Responses");
		for (ItemChange ic : c.listItemChanges()) {
			Element add = DOMUtils.createElement(commands, "Fetch");
			DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
			DOMUtils.createElementAndText(add, "Status",
					SyncStatus.OK.asXmlValue());
			c.getOptions().setTruncation(null);
			for (BodyPreference bp : c.getOptions().getBodyPreferences().values()) {
				bp.setTruncationSize(null);
			}
			serializeChange(bs, add, c, ic, encoderFactory);
		}
	}
	
	private void serializeChange(BackendSession bs, Element col,
			SyncCollection c, ItemChange ic, EncoderFactory encoderFactory) {
		
		IApplicationData data = ic.getData();
		IDataEncoder encoder = encoderFactory.getEncoder(data);
		Element apData = DOMUtils.createElement(col, "ApplicationData");
		encoder.encode(bs, apData, data, c, true);
	}
	
	private void buildUpdateItemChange(BackendSession bs, SyncCollection c,	Map<String, String> processedClientIds, Element ce, EncoderFactory encoderFactory) {
		Element responses = DOMUtils.createElement(ce, "Responses");
		if (c.isMoreAvailable()) {
			// MoreAvailable has to be before Commands
			DOMUtils.createElement(ce, "MoreAvailable");
		}
		
		Element commands = DOMUtils.createElement(ce, "Commands");
		
		List<ItemChange> itemChangesDeletion = c.listItemChangesDeletion();
		for (ItemChange ic: itemChangesDeletion) {
			serializeDeletion(commands, ic);
		}
		
		for (ItemChange ic : c.listItemChanges()) {
			String clientId = processedClientIds.get(ic.getServerId());
			if (clientId != null) {
				// Acks Add done by pda
				Element add = DOMUtils.createElement(responses, "Add");
				DOMUtils.createElementAndText(add, "ClientId", clientId);
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status",
						SyncStatus.OK.asXmlValue());
			} else if (processedClientIds.keySet().contains(ic.getServerId())) {
				// Change asked by device
				Element add = DOMUtils.createElement(responses, "Change");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status",
						SyncStatus.OK.asXmlValue());
			} else {
				// New change done on server
				Element add = DOMUtils.createElement(commands, "Add");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				serializeChange(bs, add, c, ic, encoderFactory);
			}
			processedClientIds.remove(ic.getServerId());
		}

		// Send error for the remaining entry in the Map because the
		// client has requested the addition of a resource that already exists
		// on the server
		Set<Entry<String, String>> entries = new HashSet<Map.Entry<String, String>>(
				processedClientIds.entrySet());
		for (Entry<String, String> entry : entries) {
			if (entry.getKey() != null) {
				if (entry.getKey().startsWith(c.getCollectionId().toString())) {
					Element add = null;
					if (entry.getValue() != null) {
						add = DOMUtils.createElement(responses, "Add");
						DOMUtils.createElementAndText(add, "ClientId",
								entry.getValue());
					} else {
						add = DOMUtils.createElement(responses, "Change");
					}
					DOMUtils.createElementAndText(add, "ServerId",
							entry.getKey());
					// need send ok since we do not synchronize event with
					// ParticipationState need-action
					DOMUtils.createElementAndText(add, "Status",
							SyncStatus.OK.asXmlValue());
				}
				processedClientIds.remove(entry.getKey());
			}
		}
		if (responses.getChildNodes().getLength() == 0) {
			responses.getParentNode().removeChild(responses);
		}
		if (commands.getChildNodes().getLength() == 0) {
			commands.getParentNode().removeChild(commands);
		}
	}
	
	private static void serializeDeletion(Element commands, ItemChange ic) {
		Element del = DOMUtils.createElement(commands, "Delete");
		DOMUtils.createElementAndText(del, "ServerId", ic.getServerId());
	}
	
}

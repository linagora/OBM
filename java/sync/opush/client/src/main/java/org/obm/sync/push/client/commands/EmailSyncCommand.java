package org.obm.sync.push.client.commands;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Element;

public class EmailSyncCommand extends Sync {

	private final String syncKey;
	private final String collectionId;

	public EmailSyncCommand(String syncKey, String collectionId) {
		super("EmailSyncRequest.xml");
		this.syncKey = syncKey;
		this.collectionId = collectionId;
	}

	@Override
	protected void customizeTemplate(AccountInfos ai, OPClient opc) {
		Element sk = DOMUtils.getUniqueElement(tpl.getDocumentElement(), "SyncKey");
		sk.setTextContent(syncKey);
		Element collection = DOMUtils.getUniqueElement(tpl.getDocumentElement(), "CollectionId");
		collection.setTextContent(collectionId);
	}
}

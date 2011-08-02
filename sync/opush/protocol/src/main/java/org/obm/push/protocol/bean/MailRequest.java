package org.obm.push.protocol.bean;

import java.io.InputStream;

public class MailRequest {

	private final String collectionId;
	private final String serverId;
	private final boolean saveInSent;
	private final InputStream mailContent;

	public MailRequest(String collectionId, String serverId, boolean saveInSent, InputStream mailContent) {
		this.collectionId = collectionId;
		this.serverId = serverId;
		this.saveInSent = saveInSent;
		this.mailContent = mailContent;
	}
	
	public String getCollectionId() {
		return collectionId;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public boolean isSaveInSent() {
		return saveInSent;
	}
	
	public InputStream getMailContent() {
		return mailContent;
	}
	
}

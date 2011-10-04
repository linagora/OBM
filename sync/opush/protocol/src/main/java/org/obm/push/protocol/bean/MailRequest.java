package org.obm.push.protocol.bean;

public class MailRequest {

	private final String collectionId;
	private final String serverId;
	private final boolean saveInSent;
	private final byte[] mailContent;

	public MailRequest(String collectionId, String serverId, boolean saveInSent, byte[] mailContent) {
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
	
	public byte[] getMailContent() {
		return mailContent;
	}
	
}

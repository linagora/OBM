package org.obm.mail.message;

import org.minig.imap.mime.IMimePart;

public class MailMessageInvitation {

	private String atMgrId;
	private transient IMimePart part;
	
	public MailMessageInvitation(String atMgrId, IMimePart part) {
		super();
		this.atMgrId = atMgrId;
		this.part = part;
	}

	public String getAtMgrId() {
		return atMgrId;
	}

	public IMimePart getPart() {
		return part;
	}
	
}

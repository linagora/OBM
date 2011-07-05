package org.obm.mail.message;

import org.minig.imap.mime.IMimePart;

public class MailMessageAttachment {

	private String atMgrId;
	private String displayName;
	private transient IMimePart part;
	
	public MailMessageAttachment(String atMgrId, String displayName) {
		this(atMgrId, displayName, null);
	}
	
	public MailMessageAttachment(String atMgrId, String displayName, IMimePart part) {
		super();
		this.atMgrId = atMgrId;
		this.displayName = displayName;
		this.part = part;
	}

	public String getAtMgrId() {
		return atMgrId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public IMimePart getPart() {
		return part;
	}
	
}

package org.obm.push.backend;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.store.IApplicationData;
import org.obm.push.store.MSEmailBodyType;
import org.obm.push.store.PIMDataType;
/**
 * 
 * @author adrienp
 *
 */
public class MSEmail implements IApplicationData {

	@Override
	public PIMDataType getType() {
		return PIMDataType.EMAIL;
	}

	private static final long serialVersionUID = 1165242659388338781L;

	private String subject;
	private MSEmailBody body;
	private MSAddress from;
	private Date date;
	private Map<String, String> headers;
	private Set<MSEmail> forwardMessage;
	private Set<MSAttachement> attachements;
	private long uid;
	private MSEvent invitation;
	private MessageClass messageClass;
	private Importance importance;

	private List<MSAddress> to;
	private List<MSAddress> cc;
	private List<MSAddress> bcc;
	private InputStream mimeData;

	private String smtpId;
	
	private boolean read;
	private boolean starred;
	private boolean answered;

	public MSEmail() {
		this("subject", new MSEmailBody(MSEmailBodyType.PlainText, "body"),
				new HashSet<MSAttachement>(), new Date(), null, null, null,
				null, null);
	}

	public MSEmail(String subject, MSEmailBody body,
			Set<MSAttachement> attachements, Date d, MSAddress from,
			List<MSAddress>  to, List<MSAddress>  cc, List<MSAddress>  bcc,
			Map<String, String> headers) {
		this.subject = subject;
		this.body = body;
		this.date = d;
		this.from = from;
		this.headers = headers;
		this.attachements = attachements;
		this.forwardMessage = new HashSet<MSEmail>();
		if (to != null) {
			this.to = to;
		} else {
			this.to = new LinkedList<MSAddress>();
		}
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = new LinkedList<MSAddress>();
		}
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = new LinkedList<MSAddress>();
		}
		this.read = false;
		this.messageClass = MessageClass.Note;
		this.importance = Importance.NORMAL;
	}

	public String getSubject() {
		return subject;
	}

	public MSEmailBody getBody() {
		return body;
	}

	public MSAddress getFrom() {
		return from;
	}

	public void setFrom(MSAddress from) {
		this.from = from;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(MSEmailBody body) {
		this.body = body;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setTo(List<MSAddress> to) {
		if (to != null) {
			this.to = to;
		} else {
			this.to = new LinkedList<MSAddress>();
		}
	}

	public List<MSAddress> getTo() {
		return to;
	}

	public void setCc(List<MSAddress> cc) {
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = new LinkedList<MSAddress>();
		}
	}

	public List<MSAddress> getCc() {
		return cc;
	}

	public void setForwardMessage(Set<MSEmail> forwardMessage) {
		this.forwardMessage = forwardMessage;
	}

	public Set<MSEmail> getForwardMessage() {
		return forwardMessage;
	}

	public void addForwardMessage(MSEmail forwardMessage) {
		this.forwardMessage.add(forwardMessage);
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public List<MSAddress> getBcc() {
		return bcc;
	}

	public void setBcc(List<MSAddress> bcc) {
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = new LinkedList<MSAddress>();
		}
	}

	public MSEvent getInvitation() {
		return invitation;
	}

	public void setInvitation(MSEvent invitation, MessageClass messageClass) {
		this.invitation = invitation;
		if(messageClass != null){
			this.messageClass = messageClass;
		} else {
			this.messageClass = MessageClass.ScheduleMeetingRequest;
		}
	}

	public String getSmtpId() {
		return smtpId;
	}

	public void setSmtpId(String smtpId) {
		this.smtpId = smtpId;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
	
	public MessageClass getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(MessageClass messageClass) {
		this.messageClass = messageClass;
	}
	
	public Importance getImportance() {
		return importance;
	}

	public void setImportance(Importance importance) {
		this.importance = importance;
	}
	
	public Set<MSAttachement> getAttachements() {
		return attachements;
	}

	public void setAttachements(Set<MSAttachement> attachements) {
		this.attachements = attachements;
	}
	
	public InputStream getMimeData() {
		return mimeData;
	}

	public void setMimeData(InputStream mimeData) {
		this.mimeData = mimeData;
	}
	
}


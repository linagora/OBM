package org.minig.imap;

import java.util.Date;
import java.util.List;

public class Envelope {

	public Envelope(Date data, String subject, List<Address> to,
			List<Address> cc, List<Address> bcc, Address from,
			String messageId, String inReplyTo) {
		super();
		this.date = data;
		this.subject = subject;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.from = from;
		this.messageId = messageId;
		setInReplyTo(inReplyTo);
	}

	private long uid;
	private Date date;
	private String subject;
	private List<Address> to;
	private List<Address> cc;
	private List<Address> bcc;
	private Address from;
	private String messageId;
	private String inReplyTo;

	public Date getDate() {
		return date;
	}

	public void setDate(Date data) {
		this.date = data;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public List<Address> getTo() {
		return to;
	}

	public void setTo(List<Address> to) {
		this.to = to;
	}

	public List<Address> getCc() {
		return cc;
	}

	public void setCc(List<Address> cc) {
		this.cc = cc;
	}

	public Address getFrom() {
		return from;
	}

	public void setFrom(Address from) {
		this.from = from;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
		if ("NIL".equals(inReplyTo)) {
			this.inReplyTo = null;
		}
	}

	public List<Address> getBcc() {
		return bcc;
	}

	public void setBcc(List<Address> bcc) {
		this.bcc = bcc;
	}

}

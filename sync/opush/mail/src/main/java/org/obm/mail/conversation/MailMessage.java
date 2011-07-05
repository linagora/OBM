/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.mail.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minig.imap.Address;
import org.obm.mail.message.MailMessageAttachment;
import org.obm.mail.message.MailMessageInvitation;

public class MailMessage implements Serializable, Comparable<MailMessage> {

	private static final long serialVersionUID = 1165242659388338781L;

	private String subject;
	private MailBody body;
	private List<MailMessageAttachment> attachements;
	private Address sender;
	private Date date;
	private Map<String, String> headers;
	private Set<MailMessage> forwardMessage;
	private long uid;
	private MailMessageInvitation invitation;

	private List<Address> to;
	private List<Address> cc;
	private List<Address> bcc;
	private List<Address> dispositionNotificationTo;

	private String smtpId;
	
	private boolean read;
	private boolean starred;
	private boolean answered;

	public MailMessage() {
		this("subject", new MailBody("text/plain", "body"),
				new ArrayList<MailMessageAttachment>(), new Date(), null, null, null,
				null, null, null);
	}

	public MailMessage(String subject, MailBody body,
			List<MailMessageAttachment> attachements, Date d, Address sender,
			List<Address> to, List<Address> cc, List<Address> bcc,
			List<Address> dispositionNotificationTo, 
			Map<String, String> headers) {
		this.subject = subject;
		this.body = body;
		this.attachements = attachements;
		this.date = d;
		this.sender = sender;
		this.headers = headers;
		this.forwardMessage = new HashSet<MailMessage>();
		if (to != null) {
			this.to = to;
		} else {
			this.to = Collections.emptyList();
		}
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = Collections.emptyList();
		}
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = Collections.emptyList();
		}
		if (dispositionNotificationTo != null) {
			this.dispositionNotificationTo = dispositionNotificationTo;
		} else {
			this.dispositionNotificationTo = Collections.emptyList();
		}
	}

	public String getSubject() {
		return subject;
	}

	public MailBody getBody() {
		return body;
	}

	public List<MailMessageAttachment> getAttachments() {
		return attachements;
	}

	public Address getSender() {
		return sender;
	}

	public void setSender(Address sender) {
		this.sender = sender;
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

	@Override
	public int compareTo(MailMessage o) {
		return date.compareTo(o.date);
	}

	public void setAttachments(List<MailMessageAttachment> attachements) {
		this.attachements = attachements;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(MailBody body) {
		this.body = body;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setTo(List<Address> to) {
		if (to != null) {
			this.to = to;
		} else {
			this.to = Collections.emptyList();
		}
	}

	public List<Address> getTo() {
		return to;
	}

	public void setCc(List<Address> cc) {
		if (cc != null) {
			this.cc = cc;
		} else {
			this.cc = Collections.emptyList();
		}
	}

	public List<Address> getCc() {
		return cc;
	}

	public void setForwardMessage(Set<MailMessage> forwardMessage) {
		this.forwardMessage = forwardMessage;
	}

	public Set<MailMessage> getForwardMessage() {
		return forwardMessage;
	}

	public void addForwardMessage(MailMessage forwardMessage) {
		this.forwardMessage.add(forwardMessage);
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public List<Address> getBcc() {
		return bcc;
	}

	public void setBcc(List<Address> bcc) {
		if (bcc != null) {
			this.bcc = bcc;
		} else {
			this.bcc = Collections.emptyList();
		}
	}

	public MailMessageInvitation getInvitation() {
		return invitation;
	}

	public void setInvitation(MailMessageInvitation invitation) {
		this.invitation = invitation;
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

	public boolean isHighPriority() {
		// TODO read this value....
		return false;
	}

	public List<Address> getDispositionNotificationTo() {
		return dispositionNotificationTo;
	}
	
	public void setDispositionNotificationTo(
			List<Address> dispositionNotificationTo) {
		this.dispositionNotificationTo = dispositionNotificationTo;
	}
	
}

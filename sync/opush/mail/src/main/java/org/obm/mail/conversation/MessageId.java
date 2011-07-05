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


public class MessageId implements Comparable<MessageId>{

	private long imapId;
	private String smtpId;
	private boolean read;
	private boolean starred;
	private boolean answered;
	private boolean highPriority;

	public MessageId(long uid) {
		super();
		this.imapId = uid;
	}

	public long getImapId() {
		return imapId;
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

	public void setHighPriority(boolean highPriority) {
		this.highPriority = highPriority;
	}

	public boolean isHighPriority() {
		return highPriority;
	}

	@Override
	public int compareTo(MessageId o) {
		Long l = imapId;
		return l.compareTo(o.imapId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (imapId ^ (imapId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageId other = (MessageId) obj;
		if (imapId != other.imapId)
			return false;
		return true;
	}

	
	
}

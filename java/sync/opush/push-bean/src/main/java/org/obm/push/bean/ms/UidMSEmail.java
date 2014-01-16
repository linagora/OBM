/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.bean.ms;

import java.io.Serializable;

import org.obm.push.bean.PIMDataType;
import org.obm.push.utils.index.Indexed;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class UidMSEmail extends MSEmail implements Serializable, Indexed<Long> {

	public static Builder uidBuilder() {
		return new Builder() ;
	}
	
	public static class Builder {

		private Long uid;
		private MSEmail email;

		private Builder() {
			super();
		}
		
		public Builder uid(long uid) {
			this.uid = uid;
			return this;
		}
		
		public Builder email(MSEmail email) {
			this.email = email;
			return this;
		}
		
		public UidMSEmail build() {
			Preconditions.checkState(uid != null, "The uid is required");
			Preconditions.checkState(email != null, "The msEmail is required");
			return new UidMSEmail(uid, email);
		}

	}
	
	private static final long serialVersionUID = 2772321034677507149L;
	
	@Override
	public PIMDataType getType() {
		return PIMDataType.EMAIL;
	}

	private final long uid;

	private UidMSEmail(long uid, MSEmail email) {
		super(email.getSubject(), email.getHeader(), email.getBody(), email.getAttachments(), email.getMeetingRequest(),
				email.getMessageClass(), email.getImportance(), email.isRead(), email.isStarred(), email.isAnswered());
		this.uid = uid;
	}

	@Override
	public Long getIndex() {
		return getUid();
	}
	
	public long getUid() {
		return uid;
	}

	@Override
	public final int hashCode() {
		return super.hashCode() + Objects.hashCode(uid);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof UidMSEmail) {
			UidMSEmail other = (UidMSEmail) obj;
			if (other.canEquals(this)) {
				return Objects.equal(uid, other.uid)
					&& super.equals(obj);
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.toString() + super.toString();
	}
	
	@Override
	protected boolean canEquals(Object obj) {
		return obj instanceof UidMSEmail;
	}
}


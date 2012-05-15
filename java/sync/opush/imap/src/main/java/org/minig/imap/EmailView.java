/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.minig.imap;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class EmailView {

	private final long uid;
	private final Collection<Flag> flags;
	private final Envelope envelope;

	public static class Builder {

		private Long uid;
		private Collection<Flag> flags;
		private Envelope envelope;
		
		public Builder flags(Collection<Flag> flags) {
			this.flags = ImmutableSet.<Flag>builder().addAll(flags).build();
			return this;
		}
		
		public Builder envelope(Envelope envelope) {
			this.envelope = envelope;
			return this;
		}
		
		public Builder uid(long uid) {
			this.uid = uid;
			return this;
		}
		
		public EmailView build() {
			Preconditions.checkState(uid != null, "The uid is required");
			return new EmailView(uid, flags, envelope);
		}
		
	}
	
	private EmailView(long uid, Collection<Flag> flags, Envelope envelope) {
		this.uid = uid;
		this.flags = flags;
		this.envelope = envelope;
	}

	public long getUid() {
		return uid;
	}

	public Collection<Flag> getFlags() {
		return flags;
	}

	public Envelope getHeaders() {
		return envelope;
	}
	
	public List<Address> getFrom() {
		return envelope.getFrom();
	}

	public List<Address> getTo() {
		return envelope.getTo();
	}

	public List<Address> getCc() {
		return envelope.getCc();
	}

	public String getSubject() {
		return envelope.getSubject();
	}

	public Date getDate() {
		return envelope.getDate();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("flags", flags)
			.toString();
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(flags);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof EmailView) {
			EmailView other = (EmailView) obj;
			return new EqualsBuilder()
				.append(flags, other.flags)
				.isEquals();
		}
		return false;
	}
}

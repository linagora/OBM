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
package org.obm.push.mail.bean;

import org.obm.push.mail.mime.MimeMessage;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class EmailMetadata {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private Long uid;
		private Long size;
		private Envelope envelope;
		private FlagsList flags;
		private MimeMessage mimeMessage;
		
		private Builder() {
			super();
		}

		public Builder uid(long uid) {
			this.uid = uid;
			return this;
		}

		public Builder size(long size) {
			this.size = size;
			return this;
		}

		public Builder envelope(Envelope envelope) {
			this.envelope = envelope;
			return this;
		}

		public Builder flags(FlagsList flags) {
			this.flags = flags;
			return this;
		}

		public Builder mimeMessage(MimeMessage mimeMessage) {
			this.mimeMessage = mimeMessage;
			return this;
		}
		
		public EmailMetadata build() {
			Preconditions.checkArgument(uid != null, "uid is required");
			Preconditions.checkArgument(size != null, "size is required");
			Preconditions.checkArgument(flags != null, "flags are required");
			Preconditions.checkArgument(envelope != null, "envelope is required");
			Preconditions.checkArgument(mimeMessage != null, "mimeMessage is required");
			return new EmailMetadata(uid, size, envelope, flags, mimeMessage);
		}
		
	}
	
	private final long uid;
	private final long size;
	private final Envelope envelope;
	private final FlagsList flags;
	private final MimeMessage mimeMessage;

	private EmailMetadata(long uid, long size,
			Envelope envelope, FlagsList flags, MimeMessage mimeMessage) {
		this.uid = uid;
		this.size = size;
		this.envelope = envelope;
		this.flags = flags;
		this.mimeMessage = mimeMessage;
	}

	public long getUid() {
		return uid;
	}
	
	public long getSize() {
		return size;
	}

	public FlagsList getFlags() {
		return flags;
	}

	public MimeMessage getMimeMessage() {
		return mimeMessage;
	}

	public Envelope getEnvelope() {
		return envelope;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(uid, size, flags, envelope, mimeMessage);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof EmailMetadata) {
			EmailMetadata that = (EmailMetadata) object;
			return Objects.equal(this.uid, that.uid)
					&& Objects.equal(this.size, that.size)
					&& Objects.equal(this.flags, that.flags)
					&& Objects.equal(this.envelope, that.envelope)
					&& Objects.equal(this.mimeMessage, that.mimeMessage);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("size", size)
			.add("flags", flags)
			.add("envelope", envelope)
			.add("mimeMessage", mimeMessage)
			.toString();
	}

}

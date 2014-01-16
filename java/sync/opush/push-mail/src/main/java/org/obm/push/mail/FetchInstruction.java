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
package org.obm.push.mail;

import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.mime.MimePart;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


public class FetchInstruction {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private MimePart mimePart;
		private Integer truncation;
		private MSEmailBodyType bodyType;
		private MailTransformation mailTransformation;
		
		private Builder() {}
		
		public Builder mimePart(MimePart mimePart) {
			this.mimePart = mimePart;
			return this;
		}
		
		public Builder truncation(Integer truncation) {
			this.truncation = truncation;
			return this;
		}

		public Builder bodyType(MSEmailBodyType bodyType) {
			this.bodyType = bodyType;
			return this;
		}
		
		public Builder mailTransformation(MailTransformation mailTransformation) {
			this.mailTransformation = mailTransformation;
			return this;
		}
		
		public FetchInstruction build() {
			Preconditions.checkNotNull(this.mimePart, "MimePart can't be null.");
			return new FetchInstruction(
					this.mimePart, this.truncation, this.bodyType, 
					Objects.firstNonNull(this.mailTransformation, MailTransformation.NONE));
		}

	}
	
	private final MimePart mimePart;
	private final Integer truncation;
	private final MSEmailBodyType bodyType;
	private final MailTransformation mailTransformation;
	
	private FetchInstruction(MimePart mimePart, Integer truncation, MSEmailBodyType bodyType, MailTransformation mailTransformation) {
		this.mimePart = mimePart;
		this.truncation = truncation;
		this.bodyType = bodyType;
		this.mailTransformation = mailTransformation;
	}
	
	public MimePart getMimePart() {
		return mimePart;
	}
	
	public Integer getTruncation() {
		return truncation;
	}
	
	public MSEmailBodyType getBodyType() {
		return bodyType;
	}
	
	public MailTransformation getMailTransformation() {
		return mailTransformation;
	}
	
	public boolean hasMimePartAddressDefined() {
		if (mimePart.getAddress() != null) {
			return true;
		}
		return false;
	}
	
	public boolean mustTruncate() {
		if (truncation != null) {
			return mimePart.getSize() > truncation;			
		}
		return false;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(mimePart, truncation, bodyType, mailTransformation);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof FetchInstruction) {
			FetchInstruction that = (FetchInstruction) object;
			return Objects.equal(this.mimePart, that.mimePart)
				&& Objects.equal(this.truncation, that.truncation)
				&& Objects.equal(this.bodyType, that.bodyType)
				&& Objects.equal(this.mailTransformation, that.mailTransformation);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("mimePart", mimePart)
			.add("truncation", truncation)
			.add("bodyType", bodyType)
			.add("mailTransformation", mailTransformation)
			.toString();
	}
	
}
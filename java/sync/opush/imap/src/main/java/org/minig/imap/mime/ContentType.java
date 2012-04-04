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
package org.minig.imap.mime;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.base.Objects;

public class ContentType {

	public static class Builder {
		private String primaryType = null;
		private String subType = null;
		private Collection<BodyParam> bodyParams;
		
		public Builder() {
			bodyParams = Lists.newArrayList();
		}
		
		public Builder primaryType(String primaryType) {
			this.primaryType = primaryType;
			return this;
		}
		
		public Builder subType(String subtype) {
			this.subType = subtype;
			return this;
		}
		
		public Builder addBodyParams(Collection<BodyParam> bodyParams) {
			this.bodyParams = bodyParams;
			return this;
		}
		
		public Builder contentType(String contentType) {
			setPrimayTypeAndSubType(contentType);
			addBodyParams(contentType);
			return this;
		}

		private void setPrimayTypeAndSubType(String contentType) {
			Iterable<String> itr = Splitter.on("/").split(
					extractPrimaryTypeAndSubType(contentType) );
			this.primaryType = Iterables.get(itr, 0);
			this.subType = Iterables.get(itr, 1);
		}

		private String extractPrimaryTypeAndSubType(String contentType) {
			String primaryTypeAndSubType = Iterables.getFirst(Splitter.on(";").split(contentType), null);
			Preconditions.checkNotNull(primaryTypeAndSubType, 
					"The content type format of [" + contentType + "] is not valid.");
			return primaryTypeAndSubType;
		}
		
		private void addBodyParams(String contentType) {
			// multipart/mixed;boundary="----=_Part_0_1330682067197"
			Iterator<String> itr = Splitter.on(";").split(contentType).iterator();
			if (itr.hasNext()) {
				itr.next();
			}
			while (itr.hasNext()) {
				// boundary="----=_Part_2_1330682067197"
				String equalCharacter = "=";
				String next = itr.next();
				String key = Iterables.getFirst(Splitter.on("=").split(next), null);
				if (!Strings.isNullOrEmpty(key)) {
					String value = next.substring(key.length() + equalCharacter.length());
					BodyParam bodyParam = new BodyParam(key, value.trim());
					this.bodyParams.add(bodyParam);
				}
			}
		}
		
		public ContentType build() {
			Preconditions.checkNotNull(primaryType);
			Preconditions.checkNotNull(subType);
			return new ContentType(primaryType, subType, bodyParams);
		}
	}

	private final String primaryType;
	private final String subType;
	private final Collection<BodyParam> bodyParams;
	
	private ContentType(String primaryType, String subType, Collection<BodyParam> bodyParams) {
		this.primaryType = primaryType;
		this.subType = subType;
		this.bodyParams = bodyParams;
	}
	
	public String getPrimaryType() {
		return primaryType;
	}
	
	public String getSubType() {
		return subType;
	}
	
	public Collection<BodyParam> getBodyParams() {
		return bodyParams;
	}

	public String getContentType() {
		return primaryType + "/" + subType;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(primaryType, subType, bodyParams);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof ContentType) {
			ContentType that = (ContentType) object;
			return Objects.equal(this.primaryType, that.primaryType)
				&& Objects.equal(this.subType, that.subType)
				&& Objects.equal(this.bodyParams, that.bodyParams);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("primaryType", primaryType)
			.add("subType", subType)
			.add("bodyParams", bodyParams)
			.toString();
	}
}

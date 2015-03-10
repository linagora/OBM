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
package org.obm.push.mail.mime;

import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class ContentType {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements org.obm.push.bean.Builder<ContentType> {
		private String primaryType = null;
		private String subType = null;
		private final BodyParams.Builder bodyParamsBuilder;
		private String contentDisposition;
		
		private Builder() {
			bodyParamsBuilder = BodyParams.builder();
		}
		
		public Builder primaryType(String primaryType) {
			this.primaryType = primaryType;
			return this;
		}
		
		public Builder subType(String subtype) {
			this.subType = subtype;
			return this;
		}
		
		public Builder add(BodyParams bodyParams) {
			bodyParamsBuilder.addAll(bodyParams);
			return this;
		}
		
		public Builder add(Collection<BodyParam> bodyParams) {
			bodyParamsBuilder.addAll(bodyParams);
			return this;
		}
		
		public Builder contentType(String contentType) {
			setPrimayTypeAndSubType(contentType);
			addBodyParams(contentType);
			return this;
		}
		
		public Builder contentDisposition(String contentDisposition) {
			this.contentDisposition = contentDisposition;
			return this;
		}

		private void setPrimayTypeAndSubType(String contentType) {
			Iterable<String> itr = Splitter.on("/").split(
					extractPrimaryTypeAndSubType(contentType) );
			Preconditions.checkState(Iterables.size(itr) == 2, 
					"The content type format of [" + contentType + "] is not valid.");
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
			bodyParamsBuilder.addBodyParam(contentType);
		}
		
		public ContentType build() {
			Preconditions.checkNotNull(primaryType);
			Preconditions.checkNotNull(subType);
			BodyParams params = bodyParamsBuilder.build();
			
			return new ContentType(primaryType.toLowerCase(), 
					subType.toLowerCase(), buildContentDisposition(params), params);
		}

		private ContentDisposition buildContentDisposition(BodyParams params) {
			if (contentDisposition != null) {
				ContentDisposition parsedValue = ContentDisposition.fromString(contentDisposition);
				if (parsedValue != ContentDisposition.UNKNOWN) {
					return parsedValue;
				}
			}
			
			BodyParam contentDispositionParam = params.get("content-disposition");
			if (contentDispositionParam != null) {
				ContentDisposition parsedValue = ContentDisposition.fromString(contentDispositionParam.getValue());
				if (parsedValue != ContentDisposition.UNKNOWN) {
					return parsedValue;
				}
			}
			
			return ContentDisposition.INLINE;
		}

	}

	private final String primaryType;
	private final String subType;
	private final BodyParams bodyParams;
	private final ContentDisposition contentDisposition;
	
	private ContentType(String primaryType, String subType, ContentDisposition contentDisposition, BodyParams bodyParams) {
		this.primaryType = primaryType;
		this.subType = subType;
		this.contentDisposition = contentDisposition;
		this.bodyParams = bodyParams;
	}
	
	public String getPrimaryType() {
		return primaryType;
	}
	
	public String getSubType() {
		return subType;
	}
	
	public BodyParams getBodyParams() {
		return bodyParams;
	}

	public String getFullMimeType() {
		return primaryType + "/" + subType;
	}
	
	public ContentDisposition getContentDisposition() {
		return contentDisposition;
	}
	
	public boolean isAttachment() {
		return ContentDisposition.ATTACHMENT == contentDisposition;
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

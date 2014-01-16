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
package org.obm.push.mail.conversation;

import org.obm.push.mail.mime.ContentType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


public class EmailViewAttachment {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private String id;
		private String displayName;
		private String fileReference;
		private Integer size;
		private ContentType contentType;
		private Boolean inline;
		private String contentId;
		private String contentLocation;
		
		private Builder() {
			super();
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}


		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}


		public Builder fileReference(String fileReference) {
			this.fileReference = fileReference;
			return this;
		}


		public Builder size(Integer size) {
			this.size = size;
			return this;
		}
		
		public Builder contentType(ContentType contentType) {
			this.contentType = contentType;
			return this;
		}

		public Builder contentId(String contentId) {
			this.contentId = contentId;
			return this;
		}
		
		public Builder contentLocation(String contentLocation) {
			this.contentLocation = contentLocation;
			return this;
		}
		
		public Builder inline(boolean inline) {
			this.inline = inline;
			return this;
		}
		
		public EmailViewAttachment build() {
			Preconditions.checkState(inline != null);
			return new EmailViewAttachment(id, displayName, fileReference, size, contentType, 
					contentId, contentLocation, inline);
		}
	}
	
	private final String id;
	private final String displayName;
	private final String fileReference;
	private final Integer size;
	private final ContentType contentType;
	private final String contentId;
	private final String contentLocation;
	private final boolean inline;
	
	private EmailViewAttachment(String id, String displayName, String fileReference, 
			Integer size, ContentType contentType, String contentId, String contentLocation, boolean inline) {
		this.id = id;
		this.displayName = displayName;
		this.fileReference = fileReference;
		this.size = size;
		this.contentType = contentType;
		this.contentId = contentId;
		this.contentLocation = contentLocation;
		this.inline = inline;
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Integer getSize() {
		return size;
	}

	public String getFileReference() {
		return fileReference;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public String getContentId() {
		return contentId;
	}
	
	public String getContentLocation() {
		return contentLocation;
	}
	
	public boolean isInline() {
		return inline;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(id, displayName, fileReference, size, contentType);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof EmailViewAttachment) {
			EmailViewAttachment that = (EmailViewAttachment) object;
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.displayName, that.displayName)
				&& Objects.equal(this.fileReference, that.fileReference)
				&& Objects.equal(this.size, that.size)
				&& Objects.equal(this.contentType, that.contentType);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("displayName", displayName)
			.add("fileReference", fileReference)
			.add("size", size)
			.add("contentType", contentType)
			.toString();
	}
}

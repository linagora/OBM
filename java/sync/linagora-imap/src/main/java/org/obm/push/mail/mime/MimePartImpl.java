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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MimePartImpl extends AbstractMimePart implements MimePart {

	private static final String BASE64 = "BASE64";
	private static final String QUOTED_PRINTABLE = "QUOTED-PRINTABLE";
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder embeddedMessageBuilder() {
		return new EmbeddedMessageBuilder();
	}
	
	public static class Builder implements MimePart.Builder<MimePart> {
		
		protected String multipartSubType;
		protected String contentId;
		protected String encoding;
		protected Integer size;
		protected List<MimePart> children;
		protected org.obm.push.mail.mime.ContentType.Builder contentTypeBuilder;
		protected String contentLocation;

		private Builder() {
			children = Lists.newArrayList();
			contentTypeBuilder = ContentType.builder();
		}
		
		public Builder primaryMimeType(String primaryMimeType) {
			this.contentTypeBuilder.primaryType(primaryMimeType);
			return this;
		}
		
		public Builder subMimeType(String subMimeType) {
			this.contentTypeBuilder.subType(subMimeType);
			return this;
		}
		
		public Builder contentId(String contentId) {
			this.contentId = contentId;
			return this;
		}
		
		public Builder encoding(String encoding) {
			this.encoding = encoding;
			return this;
		}
		
		public Builder size(Integer size) {
			this.size = size;
			return this;
		}
		
		public Builder bodyParams(BodyParams bodyParams) {
			this.contentTypeBuilder.add(bodyParams);
			return this;
		}

		public Builder contentDisposition(String contentDisposition) {
			this.contentTypeBuilder.contentDisposition(contentDisposition);
			return this;
		}

		public Builder contentType(String contentType) {
			this.contentTypeBuilder.contentType(contentType);
			return this;
		}
		
		public Builder contentLocation(String contentLocation) {
			this.contentLocation = contentLocation;
			return this;
		}
		
		@Override
		public Builder addChild(MimePart mimePart) {
			this.children.add(mimePart);
			return this;
		}
		
		public Builder addChildren(MimePart... mimeParts) {
			return addChildren(Arrays.asList(mimeParts));
		}
		
		public Builder addChildren(Iterable<MimePart> mimeParts) {
			for (MimePart mimePart: mimeParts) {
				addChild(mimePart);
			}
			return this;
		}
		
		public MimePart build() {
			return new MimePartImpl(contentTypeBuilder.build(), children, contentId, contentLocation, 
					encoding, size, multipartSubType);
		}

	}

	public static class EmbeddedMessageBuilder extends Builder {
		
		private void mergeMultipartWithMessage() {
			MimePart firstChild = Iterables.getFirst(children, null);
			if (firstChild == null) {
				throw new IllegalStateException("Embedded Message/RFC822 must have at least one mime part");
			}
			if (firstChild.isMultipart()) {
				bodyParams(firstChild.getBodyParams());
				this.children = firstChild.getChildren();
				this.multipartSubType = firstChild.getSubtype();
			}
		}
		
		@Override
		public MimePart build() {
			mergeMultipartWithMessage();
			return super.build();
		}
	}
	
	private MimePart parent;
	private Integer idx;
	private final ContentType contentType;
	private final String contentTransfertEncoding;
	private final String contentId;
	private final Integer size;
	private final String multipartSubType;
	private final String contentLocation;
	
	private MimePartImpl(ContentType contentType, List<MimePart> children, String contentId, 
			String contentLocation, String encoding, Integer size, String multipartSubType) {
		super(children, contentType.getBodyParams());
		this.contentType = contentType;
		this.contentId = contentId;
		this.contentLocation = contentLocation;
		this.contentTransfertEncoding = encoding;
		this.size = size;
		this.multipartSubType = multipartSubType;
		if (!isMultipart()) {
			this.idx = 1;
		}
	}

	@Override
	public void defineParent(MimePart parent, int index) {
		idx = index;
		this.parent = parent;
	}
	
	@Override
	public ContentType getContentType() {
		return contentType;
	}
	
	@Override
	public String getPrimaryType() {
		return contentType.getPrimaryType();
	}
	
	@Override
	public String getSubtype() {
		return contentType.getSubType();
	}

	@Override
	public boolean isInline() {
		return contentType.getContentDisposition() == ContentDisposition.INLINE;
	}

	@Override
	public String getContentLocation() {
		return contentLocation;
	}
	
	@Override
	public MimeAddress getAddressInternal() {
		return MimeAddress.concat(getParentAddressInternal(), selfAddress());
	}

	private MimeAddress getParentAddressInternal() {
		if (parent != null) {
			return parent.getAddressInternal();
		}
		return null;
	}
	
	@Override
	public MimeAddress getAddress() {
		if (!isMultipart()) {
			return getAddressInternal();
		}
		return null;
	}

	@Override
	public boolean isMultipart() {
		return getPrimaryType() == null || getPrimaryType().equals("multipart");
	}
	
	private Integer selfAddress() {
		return idx;
	}

	@Override
	public String getFullMimeType() {
		return contentType.getFullMimeType();
	}

	@Override
	public String getContentTransfertEncoding() {
		return contentTransfertEncoding;
	}

	@Override
	public String getCharset() {
		BodyParam bodyParam = getBodyParam("charset");
		if (bodyParam != null) {
			return bodyParam.getValue();
		}
		return null;
	}
	
	@Override
	public boolean isAttachment() {
		return contentType.isAttachment()
				|| (!isFirstElementInParent()	
				&& !isMultipart()
				&& hasParentNonMultipartAlternative());
	}
	
	@Override
	public String getAttachmentExtension() {
		return AttachmentExtensionMapper.extensionFromContentType(getContentType());
	}
	
	private boolean hasParentNonMultipartAlternative() {
		if (parent != null) {
			if (!parent.getFullMimeType().equalsIgnoreCase("multipart/alternative")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getContentId() {
		return contentId;
	}

	@Override
	public MimePart getParent() {
		return parent;
	}

	private String retrieveMethodFromCalendarPart() {
		if ("text/calendar".equals(getFullMimeType())) {
			BodyParam method = getBodyParam("method");
			if (method != null) {
				return method.getValue();
			}
		}
		return null;
	}

	@Override
	public boolean containsCalendarMethod() {
		return !Strings.isNullOrEmpty(retrieveMethodFromCalendarPart());
	}

	@Override
	public boolean isInvitation() {
		String method = retrieveMethodFromCalendarPart();
		return "REQUEST".equalsIgnoreCase(method);
	}
	
	@Override
	public boolean isCancelInvitation() {
		String method = retrieveMethodFromCalendarPart();
		return "CANCEL".equalsIgnoreCase(method);
	}
	
	@Override
	public boolean isReplyInvitation() {
		String method = retrieveMethodFromCalendarPart();
		return "REPLY".equalsIgnoreCase(method);
	}
	
	@Override
	public boolean isNested() {
		return getFullMimeType().equalsIgnoreCase("message/rfc822");
	}
	
	@Override
	public String getName() {
		BodyParam name = getBodyParam("name");
		if (name != null && name.getValue() != null) {
			return name.getValue();
		}
		BodyParam filename = getBodyParam("filename");
		if (filename != null && filename.getValue() != null) {
			return filename.getValue();
		}
		return null;
	}
	
	@Override
	public String getMultipartSubtype() {
		if (multipartSubType != null) {
			return multipartSubType;
		}
		return getSubtype();
	}
	
	@Override
	public List<MimePart> getSibling() {
		if (parent != null) {
			ArrayList<MimePart> copy = Lists.newArrayList(parent.getChildren());
			copy.remove(this);
			return copy;
		}
		return ImmutableList.of();
	}
	
	@Override
	public Integer getSize() {
		return size;
	}

	@Override
	public boolean hasMultiPartMixedParent() {
		return getParent() != null && getParent().isMultiPartMixed();
	}
	
	@Override
	public boolean isMultiPartMixed() {
		return isMultipart() && getSubtype().equalsIgnoreCase("mixed");
	}

	@Override
	public boolean isFirstElementInParent() {
		return idx != null && idx == 1;
	}

	@Override
	public boolean hasMimePart(ContentType contentType) {
		return this.getFullMimeType().
				equalsIgnoreCase(contentType.getFullMimeType());
	}
	
	@Override
	public boolean isICSAttachment() {
		return contentType.getFullMimeType().equalsIgnoreCase("application/ics");
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(
				parent, idx, contentType, contentTransfertEncoding, contentId, 
				contentLocation, size, multipartSubType);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MimePartImpl) {
			MimePartImpl that = (MimePartImpl) object;
			return Objects.equal(this.parent, that.parent)
					&& Objects.equal(this.idx, that.idx)
					&& Objects.equal(this.contentType, that.contentType)
					&& Objects.equal(this.contentTransfertEncoding, that.contentTransfertEncoding)
					&& Objects.equal(this.contentId, that.contentId)
					&& Objects.equal(this.contentLocation, that.contentLocation)
					&& Objects.equal(this.size, that.size)
					&& Objects.equal(this.multipartSubType, that.multipartSubType);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(getClass())
			.add("contentType", contentType)
			.add("idx", idx).toString();
	}
	
	@Override
	public InputStream decodeMimeStream(InputStream rawStream) {
		if (QUOTED_PRINTABLE.equalsIgnoreCase(getContentTransfertEncoding())) {
			return new QuotedPrintableInputStream(rawStream);
		} else if (BASE64.equalsIgnoreCase(getContentTransfertEncoding())) {
			return new Base64InputStream(rawStream);
		} else {
			return rawStream;
		}
	}
}
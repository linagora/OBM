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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


public class MimePart extends AbstractMimePart implements IMimePart {

	private String mimeType;
	private String mimeSubtype;
	private IMimePart parent;
	private int idx;
	private String contentTransfertEncoding;
	private String contentId;
	private String multipartSubtype;
	private Integer size;
	
	public MimePart() {
		super();
	}

	@Override
	public void defineParent(IMimePart parent, int index) {
		idx = index;
		this.parent = parent;
	}
	
	@Override
	public String getMimeType() {
		return mimeType;
	}
	
	@Override
	public void setMimeType(ContentType contentType) {
		this.mimeSubtype = contentType.getSubType();
		this.mimeType = contentType.getPrimaryType();
		this.setBodyParams(contentType.getBodyParams());
	}
	
	@Override
	public String getMimeSubtype() {
		return mimeSubtype;
	}

	public void setMimeSubtype(String mimeSubtype) {
		this.mimeSubtype = mimeSubtype;
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
		return getMimeType() == null || getMimeType().equals("multipart");
	}
	
	private Integer selfAddress() {
		if (parent == null) {
			if (isMultipart()) {
				return null;
			}
			return 1;
		}
		return idx;
	}

	public String getFullMimeType() {
		StringBuilder sb = new StringBuilder(50);
		sb.append(getMimeType() != null ? getMimeType().toLowerCase() : "null");
		sb.append("/");
		sb.append(getMimeSubtype() != null ? getMimeSubtype().toLowerCase()
				: "null");
		return sb.toString();
	}

	public String getContentTransfertEncoding() {
		return contentTransfertEncoding;
	}

	public void setContentTransfertEncoding(String contentTransfertEncoding) {
		this.contentTransfertEncoding = contentTransfertEncoding;
	}

	@Override
	public String getCharset() {
		BodyParam bodyParam = getBodyParam("charset");
		if (bodyParam != null) {
			return bodyParam.getValue();
		}
		return null;
	}
	
	public boolean isAttachment() {
		return (idx > 1 && getMimeType() != null && !"html".equalsIgnoreCase(getMimeSubtype()))
				|| !"text".equalsIgnoreCase(getMimeType());
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public int getIdx() {
		return idx;
	}

	@Override
	public IMimePart getParent() {
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
	
	public boolean isInvitation() {
		String method = retrieveMethodFromCalendarPart();
		return "REQUEST".equalsIgnoreCase(method);
	}
	
	@Override
	public boolean isNested() {
		return getFullMimeType().equalsIgnoreCase("message/rfc822");
	}
	
	public boolean isCancelInvitation() {
		String method = retrieveMethodFromCalendarPart();
		return "CANCEL".equalsIgnoreCase(method);
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
		if (multipartSubtype != null) {
			return multipartSubtype;
		}
		return getMimeSubtype();
	}
	
	@Override
	public void setMultipartSubtype(String subtype) {
		this.multipartSubtype = subtype;
	}

	@Override
	public List<IMimePart> getSibling() {
		if (parent != null) {
			ArrayList<IMimePart> copy = Lists.newArrayList(parent.getChildren());
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
	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(getClass())
			.add("mime-type", getFullMimeType())
			.add("addr", getAddress()).toString();
	}
}
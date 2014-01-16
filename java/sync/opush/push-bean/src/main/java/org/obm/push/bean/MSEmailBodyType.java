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
package org.obm.push.bean;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This enum is serialized, take care of changes done there for older version compatibility
 */
public enum MSEmailBodyType {
	
	PlainText(1, "text/plain", true), 
	HTML(2, "text/html", false), 
	RTF(3, "text/rtf", true), 
	MIME(4, "message/rfc822", true);

	private final int xmlValue;
	private final String mimeType;
	private final boolean cDataEncoded;
	
	private static Map<Integer, MSEmailBodyType> values;
	static {
		Builder<Integer, MSEmailBodyType> builder = ImmutableMap.builder();
		for (MSEmailBodyType type: values()) {
			builder.put(type.asXmlValue(), type);
		}
		values = builder.build();
	}
	
	private static Map<String, MSEmailBodyType> mimeTypeMap;
	static {
		Builder<String, MSEmailBodyType> builder = ImmutableMap.builder();
		for (MSEmailBodyType type: values()) {
			builder.put(type.getMimeType(), type);
		}
		mimeTypeMap = builder.build();
	}
	
	private MSEmailBodyType(int xmlValue, String mimeType, boolean cDataEncoded) {
		this.xmlValue = xmlValue;
		this.mimeType = mimeType;
		this.cDataEncoded = cDataEncoded;
	}
	
	public int asXmlValue() {
		return xmlValue;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public boolean isCDataEncoded() {
		return cDataEncoded;
	}

	public static final MSEmailBodyType getValueOf(Integer xmlValue) {
		if (xmlValue == null) {
			return null;
		}
		return values.get(xmlValue);
	}
	
	public static final MSEmailBodyType fromMimeType(String mimeType) {
		Preconditions.checkNotNull(mimeType);
		return mimeTypeMap.get(mimeType);
	}
}
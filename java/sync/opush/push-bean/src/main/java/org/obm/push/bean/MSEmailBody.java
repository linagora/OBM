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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;

public class MSEmailBody implements Serializable {

	private final Map<MSEmailBodyType, String> formatValueMap;
	private String charset;

	public MSEmailBody() {
		formatValueMap = new HashMap<MSEmailBodyType, String>();
	}

	public MSEmailBody(MSEmailBodyType mime, String value) {
		this();
		formatValueMap.put(mime, value);
	}

	public void addConverted(MSEmailBodyType mime, String value) {
		formatValueMap.put(mime, value);
	}

	public Set<MSEmailBodyType> availableFormats() {
		return formatValueMap.keySet();
	}

	public String getValue(MSEmailBodyType format) {
		return formatValueMap.get(format);
	}
	
	public void addMailPart(MSEmailBodyType mime, String part){
		String body = this.formatValueMap.get(mime);
		if(body!=null){
			body += part;
			this.addConverted(mime, body);
		}
	}
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(formatValueMap, charset);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEmailBody) {
			MSEmailBody that = (MSEmailBody) object;
			return Objects.equal(this.formatValueMap, that.formatValueMap)
				&& Objects.equal(this.charset, that.charset);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("formatValueMap", formatValueMap)
			.add("charset", charset)
			.toString();
	}

	
}

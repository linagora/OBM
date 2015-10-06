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

import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class BodyParam {

	private final String key;
	private final String value;
	private final Optional<String> charset;
	private final Optional<Integer> groupIndex;
	
	public BodyParam(String key, String value) {
		this(key, value, Optional.<String>absent(), Optional.<Integer>absent());
	}
	
	public BodyParam(String key, String value, Optional<String> charset, Optional<Integer> groupIndex) {
		this.key = key.toLowerCase().trim();
		this.value = value;
		this.charset = charset;
		this.groupIndex = groupIndex;
	}

	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public Optional<String> getCharset() {
		return charset;
	}

	public Optional<Integer> getGroupIndex() {
		return groupIndex;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(key, value, groupIndex, charset);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof BodyParam) {
			BodyParam that = (BodyParam) object;
			if (this.key != null && that.key != null && 
					this.value != null && that.value != null) {
				return this.key.equalsIgnoreCase(that.key)
					&& this.value.trim().equalsIgnoreCase(that.value.trim())
					&& Objects.equal(this.groupIndex, that.groupIndex)
					&& Objects.equal(this.charset, that.charset);
			}
			return Objects.equal(this.key, that.key)
				&& Objects.equal(this.value, that.value)
				&& Objects.equal(this.groupIndex, that.groupIndex)
				&& Objects.equal(this.charset, that.charset);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("key", key)
			.add("value", value)
			.add("groupIndex", groupIndex)
			.add("charset", charset)
			.toString();
	}
}
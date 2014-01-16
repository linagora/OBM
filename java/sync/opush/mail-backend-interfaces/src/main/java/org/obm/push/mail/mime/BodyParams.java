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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class BodyParams implements Iterable<BodyParam> {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
	
		private Map<String, BodyParam> params;
		
		private Builder() {
			params = Maps.newHashMap();
		}

		public Builder add(BodyParam bodyParam) {
			params.put(bodyParam.getKey().toLowerCase(), bodyParam);
			return this;
		}
		
		public Builder addAll(Iterable<BodyParam> bodyParams) {
			for (BodyParam bodyParam: bodyParams) {
				add(bodyParam);
			}
			return this;
		}

		public Builder bodyParams(Map<String, String> bodyParams) {
			for (Entry<String, String> param: bodyParams.entrySet()) {
				add(new BodyParam(param.getKey(), param.getValue()));
			}
			return this;
		}
		
		public Builder addBodyParam(String contentType) {
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
					value = stripDQuote(value);
					BodyParam bodyParam = new BodyParam(key, value.trim());
					this.add(bodyParam);
				}
			}
			return this;
		}

		private String stripDQuote(String value) {
			if (value.startsWith("\"") && value.endsWith("\"")) {
				return value.substring(1, value.length() - 1);
			}
			return value;
		}

		public BodyParams build() {
			return new BodyParams(ImmutableMap.copyOf(params));
		}

	}
	
	private final ImmutableMap<String, BodyParam> params;
	
	public BodyParams(ImmutableMap<String, BodyParam> params) {
		this.params = params;
	}

	@Override
	public Iterator<BodyParam> iterator() {
		return params.values().iterator();
	}

	public BodyParam get(String bodyParam) {
		return params.get(bodyParam.toLowerCase());
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(params);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof BodyParams) {
			BodyParams that = (BodyParams) object;
			return Objects.equal(this.params, that.params);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("params", params)
			.toString();
	}
	
}

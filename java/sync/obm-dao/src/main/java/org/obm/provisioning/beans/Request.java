/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.beans;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class Request {

	public final static String USERS_ID_KEY = "userId";
	public final static String GROUPS_ID_KEY = "groupId";
	public final static String SUBGROUPS_ID_KEY = "subgroupId";
	public final static String BATCHS_ID_KEY = "batchId";
	public final static String EXPUNGE_KEY = "expunge";

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String resourcePath;
		private HttpVerb verb;
		private ImmutableMap.Builder<String, String> params;
		private String body;

		private Builder() {
			this.params = ImmutableMap.builder();
		}

		public Builder resourcePath(String resourcePath) {
			this.resourcePath = resourcePath;
			return this;
		}

		public Builder params(Map<String, String> params) {
			this.params.putAll(params);
			return this;
		}

		public Builder param(String key, String value) {
			this.params.put(key, value);
			return this;
		}

		public Builder verb(HttpVerb verb) {
			this.verb = verb;
			return this;
		}

		public Builder body(String body) {
			this.body = body;
			return this;
		}

		public Request build() {
			Preconditions.checkState(resourcePath != null, "'resourcePath' should be set");
			Preconditions.checkState(verb != null, "'verb' should be set");

			return new Request(resourcePath, verb, params.build(), body);
		}
	}

	private String resourcePath;
	private HttpVerb verb;
	private Map<String, String> params;
	private String body;

	private Request(String resourcePath, HttpVerb verb, Map<String, String> params, String body) {
		this.resourcePath = resourcePath;
		this.verb = verb;
		this.params = params;
		this.body = body;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public HttpVerb getVerb() {
		return verb;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getBody() {
		return body;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(resourcePath, verb, body, params);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Request) {
			Request other = (Request) obj;

			return Objects.equal(resourcePath, other.resourcePath)
					&& Objects.equal(verb, other.verb)
					&& Objects.equal(body, other.body)
					&& Objects.equal(params, other.params);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("resourcePath", resourcePath)
				.add("verb", verb)
				.add("body", body)
				.add("params", params)
				.toString();
	}

}

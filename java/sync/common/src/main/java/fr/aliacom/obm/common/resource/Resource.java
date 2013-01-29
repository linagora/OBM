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
package fr.aliacom.obm.common.resource;

import com.google.common.base.Objects;

public class Resource {
	
	private final Integer id;
	private final Integer entityId;
	private final String name;
	private final String email;
	
	public static class Builder {
		private Integer id;
		private Integer entityId;
		private String name;
		private String mail;

		public Builder id(Integer id) {
			this.id = id;
			return this;
		}
		
		public Builder entityId(Integer entityId) {
			this.entityId = entityId;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder mail(String mail) {
			this.mail = mail;
			return this;
		}

		public Resource build() {
			return new Resource(id, entityId, name, mail);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
	
	private Resource(Integer id, Integer entityId, String name, String email) {
		this.id = id;
		this.entityId = entityId;
		this.name = name;
		this.email = email;
	}

	public Integer getId() {
		return id;
	}

	public Integer getEntityId() {
		return entityId;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, email, entityId, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Resource)) {
			return false;
		}
		
		Resource other = (Resource) obj;
		
		return Objects.equal(id, other.id)
				&& Objects.equal(email, other.email)
				&& Objects.equal(entityId, other.entityId)
				&& Objects.equal(name, other.name);
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("email", email)
				.toString();
	}
	
}

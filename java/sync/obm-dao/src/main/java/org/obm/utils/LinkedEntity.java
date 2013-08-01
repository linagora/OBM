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
package org.obm.utils;

import org.obm.sync.dao.EntityId;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class LinkedEntity {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Integer linkId;
		private EntityId entityId;

		private Builder() {
		}

		public Builder linkId(int linkId) {
			this.linkId = linkId;
			return this;
		}

		public Builder entityId(EntityId entityId) {
			this.entityId = entityId;
			return this;
		}

		public LinkedEntity build() {
			Preconditions.checkState(linkId != null && linkId > 0, "'linkId' must be set to a positive integer");
			Preconditions.checkState(entityId != null, "'entityId' must be set");

			return new LinkedEntity(linkId, entityId);
		}
	}

	private LinkedEntity(int linkId, EntityId entityId) {
		this.linkId = linkId;
		this.entityId = entityId;
	}

	private final int linkId;
	private final EntityId entityId;

	public int getLinkId() {
		return linkId;
	}

	public EntityId getEntityId() {
		return entityId;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(linkId, entityId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LinkedEntity) {
			LinkedEntity other = (LinkedEntity) obj;

			return Objects.equal(linkId, other.linkId)
					&& Objects.equal(entityId, other.entityId);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(this)
				.add("linkId", linkId)
				.add("entityId", entityId)
				.toString();
	}

}

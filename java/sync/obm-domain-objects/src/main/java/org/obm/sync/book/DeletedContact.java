/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2016  Linagora
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
package org.obm.sync.book;

import com.google.common.base.Objects;

public class DeletedContact {

	public static class Builder {

		private int id;
		private int addressbookId;

		private Builder() {
		}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder addressbookId(int addressbookId) {
			this.addressbookId = addressbookId;
			return this;
		}

		public DeletedContact build() {
			return new DeletedContact(id, addressbookId);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	private final int id;
	private final int addressbookId;

	private DeletedContact(int id, int addressbookId) {
		this.id = id;
		this.addressbookId = addressbookId;
	}

	public int getAddressbookId() {
		return addressbookId;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof DeletedContact) {
			DeletedContact that = (DeletedContact) object;

			return Objects.equal(id, that.id) && Objects.equal(addressbookId, that.addressbookId);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, addressbookId);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id).add("addressbookId", addressbookId).toString();
	}

}

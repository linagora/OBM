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
package org.obm.sync.book;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class AddressBook {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private int uid;
		private String name;
		private boolean readOnly;

		private Builder() {
		}

		public Builder uid(int uid) {
			this.uid = uid;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder readOnly(boolean readOnly) {
			this.readOnly = readOnly;
			return this;
		}

		public AddressBook build() {
			Preconditions.checkState(name != null);

			return new AddressBook(uid, name, readOnly);
		}

	}

	private final int uid;
	private final String name;
	private final boolean readOnly;

	private AddressBook(int uid, String name, boolean readOnly) {
		this.name = name;
		this.uid = uid;
		this.readOnly = readOnly;
	}

	public String getName() {
		return name;
	}

	public int getUid() {
		return uid;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(name, uid, readOnly);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof AddressBook) {
			AddressBook that = (AddressBook) object;

			return Objects.equal(this.name, that.name)
				&& Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.readOnly, that.readOnly);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", name)
			.add("uid", uid)
			.add("readOnly", readOnly)
			.toString();
	}

}

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
package org.obm.sync.book;

import java.util.Date;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class AddressBook {

	public static class Id {

		public static Id valueOf(String idAsString) {
			return builder().id(Integer.parseInt(idAsString)).build();
		}

		public static Id valueOf(int id) {
			return builder().id(id).build();
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private int id;

			private Builder() {
			}

			public Builder id(int id) {
				this.id = id;
				return this;
			}

			public Id build() {
				return new Id(id);
			}
		}

		private final int id;

		public int getId() {
			return id;
		}

		private Id(int id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Id) {
				Id other = (Id) obj;

				return Objects.equal(id, other.id);
			}

			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(id);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Id uid;
		private String name;
		private boolean readOnly;
		private boolean syncable;
		private boolean defaultBook;
		private Date timecreate;
		private Date timeupdate;
		private String origin;

		private Builder() {
		}

		public Builder uid(Id uid) {
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

		public Builder syncable(boolean syncable) {
			this.syncable = syncable;
			return this;
		}

		public Builder defaultBook(boolean defaultBook) {
			this.defaultBook = defaultBook;
			return this;
		}

		public Builder origin(String origin) {
			this.origin = origin;
			return this;
		}

		public Builder timecreate(Date timecreate) {
			this.timecreate = timecreate;
			return this;
		}

		public Builder timeupdate(Date timeupdate) {
			this.timeupdate = timeupdate;
			return this;
		}

		public AddressBook build() {
			Preconditions.checkState(name != null);

			return new AddressBook(uid, name, readOnly, syncable, defaultBook, timecreate, timeupdate, origin);
		}

	}

	private final Id uid;
	private final String name;
	private final boolean readOnly;
	private final boolean syncable;
	private final boolean defaultBook;
	private final Date timecreate;
	private final Date timeupdate;
	private final String origin;

	private AddressBook(Id uid, String name, boolean readOnly, boolean syncable, boolean deaultBook, Date timecreate, Date timeupdate, String origin) {
		this.name = name;
		this.uid = uid;
		this.readOnly = readOnly;
		this.syncable = syncable;
		this.defaultBook = deaultBook;
		this.timecreate = timecreate;
		this.timeupdate = timeupdate;
		this.origin = origin;
	}

	public String getName() {
		return name;
	}

	public Id getUid() {
		return uid;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public boolean isSyncable() {
		return syncable;
	}

	public boolean isDefaultBook() {
		return defaultBook;
	}

	public Date getTimecreate() {
		return timecreate;
	}

	public Date getTimeupdate() {
		return timeupdate;
	}

	public String getOrigin() {
		return origin;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(name, uid, readOnly, syncable, defaultBook, origin);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof AddressBook) {
			AddressBook that = (AddressBook) object;

			return Objects.equal(this.name, that.name)
				&& Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.readOnly, that.readOnly)
				&& Objects.equal(this.syncable, that.syncable)
				&& Objects.equal(this.defaultBook, that.defaultBook)
				&& Objects.equal(this.origin, that.origin);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", name)
			.add("uid", uid)
			.add("readOnly", readOnly)
			.add("syncable", syncable)
			.add("default", defaultBook)
			.add("timecreate", timecreate)
			.add("timeupdate", timeupdate)
			.add("origin", origin)
			.toString();
	}

}

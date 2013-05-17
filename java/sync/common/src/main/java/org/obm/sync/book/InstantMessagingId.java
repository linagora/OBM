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

import java.io.Serializable;

import org.obm.annotations.database.DatabaseField;

import com.google.common.base.Objects;

public class InstantMessagingId implements IMergeable, Serializable {

	public static final String IM_TABLE = "IM";

	private String protocol;
	private String id;

	public InstantMessagingId() {
	}

	public InstantMessagingId(String protocol, String address) {
		super();
		this.protocol = protocol;
		this.id = address;
	}

	public String getId() {
		return id;
	}

	@DatabaseField(table = IM_TABLE, column = "im_protocol")
	public String getProtocol() {
		return protocol;
	}

	@DatabaseField(table = IM_TABLE, column = "im_address")
	public void setId(String id) {
		this.id = id;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public void merge(IMergeable previous) {
		if (previous instanceof InstantMessagingId) {
			InstantMessagingId prev = (InstantMessagingId) previous;
			if (getId() == null && prev.getId() != null) {
				setId(prev.getId());
			}
			if (getProtocol() == null && prev.getProtocol() != null) {
				setProtocol(prev.getProtocol());
			}
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getProtocol(), getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InstantMessagingId)) {
			return false;
		}

		InstantMessagingId other = (InstantMessagingId) obj;

		return Objects.equal(getProtocol(), other.getProtocol())
				&& Objects.equal(getId(), other.getId());
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("protocol", getProtocol())
				.add("id", getId())
				.toString();
	}

}

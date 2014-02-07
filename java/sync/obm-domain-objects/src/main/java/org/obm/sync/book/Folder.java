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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Folder {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private Integer uid;
		private String name;
		private String ownerLoginAtDomain;
		private String ownerDisplayName;

		private Builder() {
			super();
		}
		
		public Builder uid(int uid) {
			this.uid = uid;
			return this;
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder ownerLoginAtDomain(String ownerLoginAtDomain) {
			this.ownerLoginAtDomain = ownerLoginAtDomain;
			return this;
		}
		
		public Builder ownerDisplayName(String ownerDisplayName) {
			this.ownerDisplayName = ownerDisplayName;
			return this;
		}
		
		public Folder build() {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "name can't be null or empty");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(ownerLoginAtDomain), "ownerLoginAtDomain can't be null or empty");
			Preconditions.checkNotNull(uid, "uid can't be null");
			return new Folder(uid, name, ownerLoginAtDomain, ownerDisplayName);
		}
	}
	
	
	private final Integer uid;
	private final String name;
	private final String ownerLoginAtDomain;
	private final String ownerDisplayName;
	
	private Folder(Integer uid, String name, String ownerLoginAtDomain, String ownerDisplayName) {
		this.uid = uid;
		this.name = name;
		this.ownerLoginAtDomain = ownerLoginAtDomain;
		this.ownerDisplayName = ownerDisplayName;
	}

	public Integer getUid() {
		return uid;
	}

	public String getName() {
		return name;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public String getOwnerLoginAtDomain() {
		return ownerLoginAtDomain;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(uid, name, ownerLoginAtDomain, ownerDisplayName);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Folder) {
			Folder that = (Folder) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.ownerLoginAtDomain, that.ownerLoginAtDomain)
				&& Objects.equal(this.ownerDisplayName, that.ownerDisplayName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("name", name)
			.add("ownerLoginAtDomain", ownerLoginAtDomain)
			.add("ownerDisplayName", ownerDisplayName)
			.toString();
	}

}

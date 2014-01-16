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
package org.obm.push.backend;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class OpushCollection {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private CollectionPath collectionPath;
		private String displayName;
		private String ownerLoginAtDomain;

		private Builder() {
			super();
		}
		
		public Builder collectionPath(CollectionPath collectionPath) {
			this.collectionPath = collectionPath;
			return this;
		}

		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder ownerLoginAtDomain(String ownerLoginAtDomain) {
			this.ownerLoginAtDomain = ownerLoginAtDomain;
			return this;
		}
		
		public OpushCollection build() {
			Preconditions.checkState(collectionPath != null);
			Preconditions.checkState(!Strings.isNullOrEmpty(displayName));
			
			return new OpushCollection(collectionPath, ownerLoginAtDomain, displayName);
		}
	}

	private final CollectionPath collectionPath;
	private final String ownerLoginAtDomain;
	private final String displayName;
	
	private OpushCollection(CollectionPath collectionPath, String ownerLoginAtDomain, String displayName) {
		this.collectionPath = collectionPath;
		this.ownerLoginAtDomain = ownerLoginAtDomain;
		this.displayName = displayName;
	}
	
	public String displayName() {
		return displayName;
	}

	public CollectionPath collectionPath() {
		return collectionPath;
	}

	public String getOwnerLoginAtDomain() {
		return ownerLoginAtDomain;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(collectionPath, ownerLoginAtDomain, displayName);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof OpushCollection) {
			OpushCollection that = (OpushCollection) object;
			return Objects.equal(this.collectionPath, that.collectionPath)
				&& Objects.equal(this.ownerLoginAtDomain, that.ownerLoginAtDomain)
				&& Objects.equal(this.displayName, that.displayName);
		}
		return false;
	}

	@Override
	public final String toString() {
		return Objects.toStringHelper(this)
			.add("collectionPath", collectionPath)
			.add("ownerLoginAtDomain", ownerLoginAtDomain)
			.add("displayName", displayName)
			.toString();
	}
}

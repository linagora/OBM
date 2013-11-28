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
package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public final class BodyPreference implements Serializable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Integer truncationSize;
		private MSEmailBodyType type;
		private boolean allOrNone;
		
		private Builder() {
			allOrNone = false;
		}
		
		public Builder truncationSize(int size) {
			this.truncationSize = size;
			return this;
		}
		
		public Builder bodyType(MSEmailBodyType msEmailBodyType) {
			this.type = msEmailBodyType;
			return this;
		}
		
		public Builder allOrNone(boolean allOrNone) {
			this.allOrNone = allOrNone;
			return this;
		}
		
		public BodyPreference build() {
			if (truncationSize != null && truncationSize == 0) {
				truncationSize = null;
			}
			return new BodyPreference(
					this.truncationSize, this.type, allOrNone);
		}
	}
	
	private static final long serialVersionUID = 6190091634662451745L;
	
	private final Integer truncationSize;
	private final MSEmailBodyType type;
	private final boolean allOrNone;
	
	private BodyPreference(Integer truncationSize, MSEmailBodyType msEmailBodyType, Boolean allOrNone) {
		this.truncationSize = truncationSize;
		this.type = msEmailBodyType;
		this.allOrNone = allOrNone;
	}
	
	public Integer getTruncationSize() {
		return this.truncationSize;
	}

	public MSEmailBodyType getType() {
		return type;
	}

	public boolean isAllOrNone() {
		return allOrNone;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(truncationSize, type, allOrNone);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof BodyPreference) {
			BodyPreference that = (BodyPreference) object;
			return Objects.equal(this.truncationSize, that.truncationSize)
				&& Objects.equal(this.type, that.type)
				&& Objects.equal(this.allOrNone, that.allOrNone);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("truncationSize", truncationSize)
			.add("type", type)
			.add("allOrNone", allOrNone)
			.toString();
	}
}
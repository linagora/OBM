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

package org.obm.push.mail.bean;

import com.google.common.base.Objects;

public final class ListInfo {

	private String name;

	private boolean selectable;
	private final boolean createSubfolder;

	public ListInfo(String name, boolean selectable, boolean noInferiors) {
		super();
		this.name = name;
		this.selectable = selectable;
		this.createSubfolder = noInferiors;
	}

	public String getName() {
		return name;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public boolean canCreateSubfolder() {
		return createSubfolder;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass())
				.add("name", name)
				.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(name, selectable, createSubfolder);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ListInfo) {
			ListInfo that = (ListInfo) object;
			return Objects.equal(this.name, that.name)
				&& Objects.equal(this.selectable, that.selectable)
				&& Objects.equal(this.createSubfolder, that.createSubfolder);
		}
		return false;
	}
	
}

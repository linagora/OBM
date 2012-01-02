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
package org.obm.sync.push.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.google.common.base.Objects;

public final class FolderHierarchy implements Map<FolderType, Folder> {

	private Map<FolderType, Folder> folders;

	public FolderHierarchy(Map<FolderType, Folder> folders) {
		this.folders = new HashMap<FolderType, Folder>(folders.size()+1);
		this.folders.putAll(folders);
	}

	@Override
	public void clear() {
		folders.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return folders.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return folders.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<FolderType, Folder>> entrySet() {
		return folders.entrySet();
	}

	@Override
	public Folder get(Object key) {
		return folders.get(key);
	}

	@Override
	public boolean isEmpty() {
		return folders.isEmpty();
	}

	@Override
	public Set<FolderType> keySet() {
		return folders.keySet();
	}

	@Override
	public Folder put(FolderType key, Folder value) {
		return folders.put(key, value);
	}

	@Override
	public void putAll(Map<? extends FolderType, ? extends Folder> m) {
		folders.putAll(m);
	}

	@Override
	public Folder remove(Object key) {
		return folders.remove(key);
	}

	@Override
	public int size() {
		return folders.size();
	}

	@Override
	public Collection<Folder> values() {
		return folders.values();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(folders);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof FolderHierarchy) {
			FolderHierarchy that = (FolderHierarchy) object;
			return Objects.equal(this.folders, that.folders);
		}
		return false;
	}

}

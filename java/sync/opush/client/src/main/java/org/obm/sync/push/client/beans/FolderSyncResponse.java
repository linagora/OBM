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
package org.obm.sync.push.client.beans;

import java.util.HashMap;
import java.util.Map;

import org.obm.push.bean.SyncKey;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.IEasReponse;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Objects;

public final class FolderSyncResponse implements IEasReponse {

	public static class XmlParser {

		public FolderSyncResponse parse(Element root) {
			String key = DOMUtils.getElementText(root, "SyncKey");
			int status = Integer.valueOf(DOMUtils.getElementText(root, "Status"));
			int count = Integer.valueOf(DOMUtils.getElementText(root, "Count"));
			Map<FolderType, Folder> ret = new HashMap<FolderType, Folder>(count + 1);

			getFolders(ret, root, FolderStatus.ADD);
			getFolders(ret, root, FolderStatus.UPDATE);
			getFolders(ret, root, FolderStatus.DELETE);
			
			return new FolderSyncResponse(new SyncKey(key), ret, status, count);
		}

		private void getFolders(Map<FolderType, Folder> ret, Element root, FolderStatus statusNodeName) {
			NodeList nl = root.getElementsByTagName(statusNodeName.getValue());
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				Folder f = new Folder();
				f.setServerId(DOMUtils.getElementText(e, "ServerId"));
				f.setParentId(DOMUtils.getElementText(e, "ParentId"));
				f.setName(DOMUtils.getElementText(e, "DisplayName"));
				f.setType(recognizeType(e));
				f.setStatus(statusNodeName);
				ret.put(f.getType(), f);
			}
		}

		private FolderType recognizeType(Element e) {
			String type = DOMUtils.getElementText(e, "Type");
			if (type != null) {
				return FolderType.getValue(Integer.parseInt(type));
			} else {
				return null;
			}
		}
	}
	
	private final FolderHierarchy fl;
	private final SyncKey key;
	private final int status;
	private final int count;

	public FolderSyncResponse(SyncKey key, Map<FolderType, Folder> fl, int status, int count) {
		this.status = status;
		this.count = count;
		this.fl = new FolderHierarchy(fl);
		this.key = key;
	}
	
	@Override
	public SyncKey getReturnedSyncKey() {
		return key;
	}

	public FolderHierarchy getFolders() {
		return fl;
	}

	public int getStatus() {
		return status;
	}

	public String getStatusAsString() {
		return String.valueOf(getStatus());
	}
	
	public int getCount() {
		return count;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(fl, key, status, count);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof FolderSyncResponse) {
			FolderSyncResponse that = (FolderSyncResponse) object;
			return Objects.equal(this.fl, that.fl)
				&& Objects.equal(this.key, that.key)
				&& Objects.equal(this.status, that.status)
				&& Objects.equal(this.count, that.count);
		}
		return false;
	}
	
}

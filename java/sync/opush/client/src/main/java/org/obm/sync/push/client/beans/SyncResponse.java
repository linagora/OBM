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
import org.obm.push.bean.SyncStatus;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.Change;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Objects;

public final class SyncResponse {

	public static class XmlParser {
		
		public SyncResponse parse(Element root) {
			Map<String, Collection> ret = new HashMap<String, Collection>();

			String status = DOMUtils.getElementText(root, "Status");
			NodeList nl = root.getElementsByTagName("Collection");
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				Collection col = new Collection();
				col.setSyncKey(new SyncKey(DOMUtils.getElementText(e, "SyncKey")));
				col.setCollectionId(DOMUtils.getElementText(e, "CollectionId"));
				col.setStatus(DOMUtils.getElementText(e, "Status"));
				NodeList ap = e.getElementsByTagName("Add");
				for (int j = 0; j < ap.getLength(); j++) {
					Element appData = (Element) ap.item(j);
					String serverId = DOMUtils.getElementText(appData, "ServerId");
					Add add = new Add();
					add.setServerId(serverId);
					col.addAdd(add);
				}
				NodeList changes = e.getElementsByTagName("Change");
				for (int j = 0; j < changes.getLength(); j++) {
					Element appData = (Element) changes.item(j);
					String serverId = DOMUtils.getElementText(appData, "ServerId");
					Change change = new Change();
					change.setServerId(serverId);
					col.addChange(change);
				}
				NodeList deleteNodes = e.getElementsByTagName("Delete");
				for (int j = 0; j < deleteNodes.getLength(); j++) {
					Element appData = (Element) deleteNodes.item(j);
					String serverId = DOMUtils.getElementText(appData, "ServerId");
					Delete delete = new Delete(serverId);
					col.addDelete(delete);
				}
				ret.put(col.getCollectionId(), col);
			}

			return new SyncResponse(ret, SyncStatus.fromSpecificationValue(status));
		}
		
	}
	
	private Map<String, Collection> cl;
	private SyncStatus syncStatus;

	public SyncResponse(Map<String, Collection> cl, SyncStatus syncStatus) {
		this.cl = new HashMap<String, Collection>(cl);
		this.syncStatus = syncStatus;
	}

	public Map<String, Collection> getCollections() {
		return cl;
	}
	
	public Collection getCollection(String collectionId) {
		return cl.get(collectionId);
	}
	
	public Collection getCollection(int key) {
		return cl.get(String.valueOf(key));
	}

	public SyncStatus getSyncStatus() {
		return syncStatus;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(cl, syncStatus);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof SyncResponse) {
			SyncResponse that = (SyncResponse) object;
			return Objects.equal(this.cl, that.cl) && 
				Objects.equal(this.syncStatus, that.syncStatus);
		}
		return false;
	}
	
}

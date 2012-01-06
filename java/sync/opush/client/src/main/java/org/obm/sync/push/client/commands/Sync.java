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
package org.obm.sync.push.client.commands;

import java.util.HashMap;
import java.util.Map;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.Add;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.SyncResponse;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Performs a Sync AS command for the given folders with 0 as syncKey
 */
public class Sync extends TemplateBasedCommand<SyncResponse> {

	private Folder[] folders;

	public Sync(Folder... folders) {
		this("SyncRequest.xml");
		this.folders = folders;
	}

	public Sync(Document doc) {
		super(NS.AirSync, "Sync", doc);
	}

	public Sync(String template) {
		super(NS.AirSync, "Sync", template);
	}

	@Override
	protected void customizeTemplate(AccountInfos ai, OPClient opc) {
		Element cols = DOMUtils.getUniqueElement(tpl.getDocumentElement(),
				"Collections");
		for (Folder folder : folders) {
			Element col = DOMUtils.createElement(cols, "Collection");
			DOMUtils.createElementAndText(col, "SyncKey", "0");
			DOMUtils.createElementAndText(col, "CollectionId",
					folder.getServerId());
		}
	}

	@Override
	protected SyncResponse parseResponse(Element root) {
		Map<String, Collection> ret = new HashMap<String, Collection>();

		NodeList nl = root.getElementsByTagName("Collection");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			Collection col = new Collection();
			col.setSyncKey(DOMUtils.getElementText(e, "SyncKey"));
			col.setCollectionId(DOMUtils.getElementText(e, "CollectionId"));
			col.setStatus(Integer.valueOf(
					DOMUtils.getElementText(e, "Status"), 0));
			NodeList ap = e.getElementsByTagName("Add");
			for (int j = 0; j < ap.getLength(); j++) {
				Element appData = (Element) ap.item(j);
				String serverId = DOMUtils.getElementText(appData, "ServerId");
				Add add = new Add();
				add.setServerId(serverId);
				col.addAdd(add);
			}
			ret.put(col.getCollectionId(), col);
		}

		return new SyncResponse(ret);
	}

}

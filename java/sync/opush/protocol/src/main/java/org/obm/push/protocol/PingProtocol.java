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
package org.obm.push.protocol;

import java.util.LinkedHashSet;

import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncCollection;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.annotations.VisibleForTesting;

public class PingProtocol implements ActiveSyncProtocol<PingRequest, PingResponse> {

	@Override
	public PingRequest decodeRequest(Document doc) {
		if (doc == null) {
			return new PingRequest.Builder().build();
		}
		Element pr = doc.getDocumentElement();
		Element hb = DOMUtils.getUniqueElement(pr, "HeartbeatInterval");
		Long heartbeatInterval = null;
		if (hb != null) {
			heartbeatInterval = Long.valueOf(hb.getTextContent());
		}
		
		LinkedHashSet<SyncCollection> syncCollections = new LinkedHashSet<SyncCollection>();
		NodeList folders = pr.getElementsByTagName("Folder");
		for (int i = 0; i < folders.getLength(); i++) {
			SyncCollection syncCollection = new SyncCollection();
			Element f = (Element) folders.item(i);
			syncCollection.setDataClass(DOMUtils.getElementText(f, "Class"));
			int id = Integer.valueOf(DOMUtils.getElementText(f, "Id"));
			syncCollection.setCollectionId(id);
			syncCollections.add(syncCollection);
		}
		
		return new PingRequest.Builder()
					.heartbeatInterval(heartbeatInterval)
					.syncCollections(syncCollections)
					.build();
	}

	@Override
	public PingResponse decodeResponse(Document doc) {
		if (doc == null) {
			return new PingResponse.Builder().build();
		}
		
		Element pr = doc.getDocumentElement();
		Element status = DOMUtils.getUniqueElement(pr, "Status");
		PingStatus pingStatus = PingStatus.fromSpecificationValue(status.getTextContent());
		
		LinkedHashSet<SyncCollection> syncCollections = new LinkedHashSet<SyncCollection>();
		NodeList folders = pr.getElementsByTagName("Folder");
		for (int i = 0; i < folders.getLength(); i++) {
			SyncCollection syncCollection = new SyncCollection();
			Element folder = (Element) folders.item(i);
			syncCollection.setCollectionId(Integer.valueOf(folder.getTextContent()));
			syncCollections.add(syncCollection);
		}
		
		return new PingResponse.Builder()
					.pingStatus(pingStatus)
					.syncCollections(syncCollections)
					.build();
	}

	@Override
	public Document encodeResponse(PingResponse pingResponse) {
		Document document = DOMUtils.createDoc(null, "Ping");
		Element root = document.getDocumentElement();
		
		DOMUtils.createElementAndText(root, "Status", pingResponse.getPingStatus().asSpecificationValue());
		if (responseHasFolders(pingResponse)) {
			encodeFolders(pingResponse, root);
		}
		return document;
	}

	@VisibleForTesting boolean responseHasFolders(PingResponse pingResponse) {
		return pingResponse.getSyncCollections() != null && !pingResponse.getSyncCollections().isEmpty();
	}

	private void encodeFolders(PingResponse pingResponse, Element root) {
		Element folders = DOMUtils.createElement(root, "Folders");
		for (SyncCollection sc : pingResponse.getSyncCollections()) {
			DOMUtils.createElementAndText(folders, "Folder", sc.getCollectionId().toString());
		}
	}

	@Override
	public Document encodeRequest(PingRequest pingRequest) {
		Document document = DOMUtils.createDoc(null, "Ping");
		Element root = document.getDocumentElement();
		
		DOMUtils.createElementAndText(root, "HeartbeatInterval", String.valueOf(pingRequest.getHeartbeatInterval()));
		Element folders = DOMUtils.createElement(root, "Folders");
		for (SyncCollection syncCollection : pingRequest.getSyncCollections()) {
			Element folder = DOMUtils.createElement(folders, "Folder");
			DOMUtils.createElementAndText(folder, "Id", String.valueOf(syncCollection.getCollectionId()));
			DOMUtils.createElementAndText(folder, "Class", syncCollection.getDataClass());
		}
		return document;
	}
	
	public Document buildError(String errorStatus) {
		Document document = DOMUtils.createDoc(null, "Ping");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", errorStatus);
		return document;
	}
	
}

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
 * 
 * @author tom
 * 
 */
public class Sync extends TemplateBasedCommand<SyncResponse> {

	private Folder[] folders;

	public Sync(Folder... folders) {
		super(NS.AirSync, "Sync", "SyncRequest.xml");
		this.folders = folders;
	}

	public Sync(Document doc) {
		super(NS.AirSync, "Sync", doc);
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
			col.setStatus(Integer.getInteger(
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

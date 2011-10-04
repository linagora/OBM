package org.obm.sync.push.client.commands;

import java.util.HashMap;
import java.util.Map;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Performs a FolderSync AS command with the given sync key
 * 
 * @author tom
 * 
 */
public class FolderSync extends TemplateBasedCommand<FolderSyncResponse> {

	private String syncKey;

	public FolderSync(String syncKey) {
		super(NS.FolderHierarchy, "FolderSync", "FolderSyncRequest.xml");
		this.syncKey = syncKey;
	}

	@Override
	protected void customizeTemplate(AccountInfos ai, OPClient opc) {
		Element sk = DOMUtils.getUniqueElement(tpl.getDocumentElement(),
				"SyncKey");
		sk.setTextContent(syncKey);
	}

	@Override
	protected FolderSyncResponse parseResponse(Element root) {
		String key = DOMUtils.getElementText(root, "SyncKey");
		int count = Integer.parseInt(DOMUtils.getElementText(root, "Count"));
		Map<FolderType, Folder> ret = new HashMap<FolderType, Folder>(count + 1);

		// TODO process updates / deletions
		NodeList nl = root.getElementsByTagName("Add");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			Folder f = new Folder();
			f.setServerId(DOMUtils.getElementText(e, "ServerId"));
			f.setParentId(DOMUtils.getElementText(e, "ParentId"));
			f.setName(DOMUtils.getElementText(e, "DisplayName"));
			f.setType(FolderType.getValue(Integer.parseInt(DOMUtils.getElementText(e, "Type"))));
			ret.put(f.getType(), f);
		}

		return new FolderSyncResponse(key, ret);
	}

}

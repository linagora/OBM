package org.obm.sync.setting;

import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SettingItemsWriter extends AbstractItemsWriter {

	public void appendSetting(Element root, String key, String value) {
		Element s = root;
		if (!"setting".equals(root.getNodeName())) {
			s = DOMUtils.createElement(root, "setting");
		}
		createIfNotNull(s, "key", key);
		createIfNotNull(s, "value", value);
	}

	public Document getVacationDOM(VacationSettings vs) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/vacation.xsd", "vacation");
			Element root = doc.getDocumentElement();
			root.setAttribute("enabled", vs.isEnabled() + "");
			if (vs.getStart() != null) {
				root.setAttribute("start", vs.getStart().getTime() + "");
			}
			if (vs.getEnd() != null) {
				root.setAttribute("end", vs.getEnd().getTime() + "");
			}
			root.setTextContent(vs.getText());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return doc;
	}

	public Document getForwardingDOM(ForwardingSettings fs) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/forwarding.xsd", "forwarding");
			Element root = doc.getDocumentElement();
			root.setAttribute("enabled", fs.isEnabled() + "");
			root.setAttribute("localCopy", fs.isLocalCopy() + "");
			if (fs.getEmail() != null) {
				root.setTextContent(fs.getEmail());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return doc;
	}

}

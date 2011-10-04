package org.obm.sync.setting;

import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.obm.sync.items.AbstractItemsParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SettingItemsParser extends AbstractItemsParser {

	public VacationSettings parseVacation(Document doc) {
		VacationSettings ret = new VacationSettings();

		Element root = doc.getDocumentElement();
		ret.setEnabled("true".equals(root.getAttribute("enabled")));
		if (root.hasAttribute("start")) {
			ret.setStart(new Date(Long.parseLong(root.getAttribute("start"))));
		}
		if (root.hasAttribute("end")) {
			ret.setEnd(new Date(Long.parseLong(root.getAttribute("end"))));
		}
		ret.setText(root.getTextContent());
		return ret;
	}

	public ForwardingSettings parseForwarding(Document doc) {
		ForwardingSettings ret = new ForwardingSettings();

		Element root = doc.getDocumentElement();
		ret.setEnabled("true".equals(root.getAttribute("enabled")));
		ret.setLocalCopy("true".equals(root.getAttribute("localCopy")));
		ret.setEmail(root.getTextContent());
		return ret;
	}

	public Map<String, String> parseListSettings(Document doc) {
		Map<String, String> settings = new HashMap<String, String>();
		Element root = doc.getDocumentElement();

		NodeList nlSetting = root.getElementsByTagName("setting");
		for (int i = 0; i < nlSetting.getLength(); i++) {
			Element set = (Element) nlSetting.item(i);
			Entry<String, String> e = parseSetting(set);
			settings.put(e.getKey(), e.getValue());
		}

		return settings;
	}

	public Entry<String, String> parseSetting(Element e) {
		return new AbstractMap.SimpleEntry<String, String>(s(e, "key"), s(e,
				"value"));

	}

}

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

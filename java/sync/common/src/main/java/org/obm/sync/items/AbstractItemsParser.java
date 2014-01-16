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
package org.obm.sync.items;

import java.util.Arrays;
import java.util.Date;

import org.obm.sync.base.KeyList;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractItemsParser {

	protected String s(Element e, String name) {
		String ret = DOMUtils.getElementTextInChildren(e, name);
		if (ret == null) {
			return "";
		}
		return ret;
	}

	protected Date d(Element e, String name) {
		String ret = DOMUtils.getElementTextInChildren(e, name);
		if (ret != null) {
			return DateHelper.asDate(ret);
		}
		return null;
	}

	protected Integer i(Element e, String name, Integer defaultValue) {
		String txt = DOMUtils.getElementTextInChildren(e, name);
		if (txt != null) {
			return Integer.parseInt(txt);
		}
		return defaultValue;
	}

	protected Integer i(Element e, String name) {
		return i(e, name, null);
	}
	
	protected boolean b(Element e, String name) {
		String txt = DOMUtils.getElementTextInChildren(e, name);
		if (txt != null) {
			return Boolean.parseBoolean(txt);
		}
		return false;
	}

	public String[] parseArrayOfString(Document doc) {
		return DOMUtils.getTexts(doc.getDocumentElement(), "value");
	}

	public KeyList parseKeyList(Document doc) {
		String[] keys = DOMUtils.getTexts(doc.getDocumentElement(), "key");
		return new KeyList(Arrays.asList(keys));
	}
}

package org.obm.sync.items;

import java.util.Arrays;
import java.util.Date;

import org.obm.sync.base.KeyList;
import org.obm.sync.utils.DOMUtils;
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

	protected Integer i(Element e, String name) {
		String txt = DOMUtils.getElementTextInChildren(e, name);
		if (txt != null) {
			return Integer.parseInt(txt);
		}
		return 0;
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

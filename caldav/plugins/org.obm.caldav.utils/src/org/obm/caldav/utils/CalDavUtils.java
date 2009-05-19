package org.obm.caldav.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CalDavUtils {
	public static Set<String> getDhrefNode(Document doc){
		Set<String> nodeList = new HashSet<String>();
		Element root = doc.getDocumentElement();
		String[] values = DOMUtils.getTexts(root, "D:href");
		nodeList.addAll(Arrays.asList(values));
		return nodeList;
	}
}

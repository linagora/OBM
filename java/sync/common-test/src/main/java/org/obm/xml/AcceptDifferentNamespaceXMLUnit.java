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
package org.obm.xml;

import org.custommonkey.xmlunit.Difference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class AcceptDifferentNamespaceXMLUnit {

	public static ElementQualifier newElementQualifier() {
		return new ElementQualifier();
	}

	public static DifferenceListener newDifferenceListener() {
		return new DifferenceListener();
	}
	
	public static class ElementQualifier implements org.custommonkey.xmlunit.ElementQualifier {

		private ElementQualifier() {}
		
		@Override
		public boolean qualifyForComparison(Element first, Element second) {
			String firstNodeName = getNodeNameWithoutNamespace(first);
			String secondNodeName = getNodeNameWithoutNamespace(second);
			return firstNodeName.equals(secondNodeName);
		}
		
	}

	public static class DifferenceListener implements org.custommonkey.xmlunit.DifferenceListener {

		private DifferenceListener() {}
		
		@Override
		public int differenceFound(Difference diff) {
			Node controlNode = diff.getControlNodeDetail().getNode();
			Node testNode = diff.getTestNodeDetail().getNode();
			if (hasValidNodes(controlNode, testNode)) {
				return differenceByNodeName(controlNode, testNode);
			}
			return RETURN_ACCEPT_DIFFERENCE;
		}

		private boolean hasValidNodes(Node controlNode, Node testNode) {
			return controlNode != null && testNode != null;
		}

		private int differenceByNodeName(Node controlNode, Node testNode) {
			String controlValue = getNodeNameWithoutNamespace(controlNode);
			String testValue = getNodeNameWithoutNamespace(testNode);
			if (controlValue.equals(testValue)) {
				return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
			}
			return RETURN_ACCEPT_DIFFERENCE;
		}


		@Override
		public void skippedComparison(Node arg0, Node arg1) {
			// continue skipping
		}
	}
	

	@VisibleForTesting static String getNodeNameWithoutNamespace(Node node) {
		String nodeName = node.getNodeName();
		if (nodeHasNamespace(nodeName)) {
			return getNodeNameWithoutNamespace(nodeName);
		}
		return nodeName;
	}

	private static String getNodeNameWithoutNamespace(String nodeName) {
		Iterable<String> splitedName = Splitter.on(':').omitEmptyStrings().split(nodeName);
		if (Iterables.size(splitedName) == 2) {
			nodeName = Iterables.get(splitedName, 1);
		} else {
			throw new IllegalArgumentException("The node name is illegal : " + nodeName);
		}
		return nodeName;
	}

	private static boolean nodeHasNamespace(String nodeName) {
		return nodeName.contains(":");
	}
}

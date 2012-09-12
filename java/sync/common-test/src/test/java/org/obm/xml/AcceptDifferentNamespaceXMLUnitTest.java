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
package org.obm.xml;

import static org.custommonkey.xmlunit.DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
import static org.custommonkey.xmlunit.DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.xml.AcceptDifferentNamespaceXMLUnit.DifferenceListener;
import org.obm.xml.AcceptDifferentNamespaceXMLUnit.ElementQualifier;
import org.w3c.dom.Element;

@RunWith(SlowFilterRunner.class)
public class AcceptDifferentNamespaceXMLUnitTest {

	@Test
	public void qualifierSameElement() {
		Element firstWithoutNamespace = mockElementWithName("AName");
		Element secondWithoutNamespace = mockElementWithName("AName");
		
		replay(firstWithoutNamespace, secondWithoutNamespace);
		
		ElementQualifier qualifier = AcceptDifferentNamespaceXMLUnit.newElementQualifier();
		
		assertThat(qualifier.qualifyForComparison(firstWithoutNamespace, secondWithoutNamespace)).isTrue();
	}
	
	@Test
	public void qualifierDifferentElement() {
		Element firstWithoutNamespace = mockElementWithName("AName");
		Element secondWithoutNamespace = mockElementWithName("OtherName");
		
		replay(firstWithoutNamespace, secondWithoutNamespace);
		
		ElementQualifier qualifier = AcceptDifferentNamespaceXMLUnit.newElementQualifier();
		
		assertThat(qualifier.qualifyForComparison(firstWithoutNamespace, secondWithoutNamespace)).isFalse();
	}
	
	@Test
	public void qualifierSameNameInSameNamespace() {
		Element firstWithoutNamespace = mockElementWithName("Namespace:AName");
		Element secondWithoutNamespace = mockElementWithName("Namespace:AName");
		
		replay(firstWithoutNamespace, secondWithoutNamespace);
		
		ElementQualifier qualifier = AcceptDifferentNamespaceXMLUnit.newElementQualifier();
		
		assertThat(qualifier.qualifyForComparison(firstWithoutNamespace, secondWithoutNamespace)).isTrue();
	}
	
	@Test
	public void qualifierDifferentNameInSameNamespace() {
		Element firstWithoutNamespace = mockElementWithName("Namespace:AName");
		Element secondWithoutNamespace = mockElementWithName("Namespace:OtherName");
		
		replay(firstWithoutNamespace, secondWithoutNamespace);
		
		ElementQualifier qualifier = AcceptDifferentNamespaceXMLUnit.newElementQualifier();
		
		assertThat(qualifier.qualifyForComparison(firstWithoutNamespace, secondWithoutNamespace)).isFalse();
	}
	
	@Test
	public void qualifierSameNameWithOneWithoutNamespace() {
		Element firstWithoutNamespace = mockElementWithName("Namespace:AName");
		Element secondWithoutNamespace = mockElementWithName("AName");
		
		replay(firstWithoutNamespace, secondWithoutNamespace);
		
		ElementQualifier qualifier = AcceptDifferentNamespaceXMLUnit.newElementQualifier();
		
		assertThat(qualifier.qualifyForComparison(firstWithoutNamespace, secondWithoutNamespace)).isTrue();
	}
	
	@Test
	public void qualifierSameNameWithDifferentNamespace() {
		Element firstWithoutNamespace = mockElementWithName("Namespace:AName");
		Element secondWithoutNamespace = mockElementWithName("OtherNamespace:AName");
		
		replay(firstWithoutNamespace, secondWithoutNamespace);
		
		ElementQualifier qualifier = AcceptDifferentNamespaceXMLUnit.newElementQualifier();
		
		assertThat(qualifier.qualifyForComparison(firstWithoutNamespace, secondWithoutNamespace)).isTrue();
	}

	@Test
	public void differenceSameElement() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = mockElementWithName("AName");
		Element testNode = mockElementWithName("AName");
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, controlNode, testNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR);
	}
	
	@Test
	public void differenceDifferentElement() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = mockElementWithName("AName");
		Element testNode = mockElementWithName("OtherName");
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, controlNode, testNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_ACCEPT_DIFFERENCE);
	}
	
	@Test
	public void differenceSameNameInSameNamespace() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = mockElementWithName("Namespace:AName");
		Element testNode = mockElementWithName("Namespace:AName");
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, controlNode, testNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR);
	}
	
	@Test
	public void differenceDifferentNameInSameNamespace() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = mockElementWithName("Namespace:AName");
		Element testNode = mockElementWithName("Namespace:OtherName");
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, controlNode, testNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_ACCEPT_DIFFERENCE);
	}
	
	@Test
	public void differenceSameNameWithOneWithoutNamespace() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = mockElementWithName("Namespace:AName");
		Element testNode = mockElementWithName("AName");
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, controlNode, testNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR);
	}
	
	@Test
	public void differenceSameNameWithDifferentNamespace() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = mockElementWithName("Namespace:AName");
		Element testNode = mockElementWithName("OtherNamespace:AName");
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, controlNode, testNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR);
	}
	
	@Test
	public void differenceIfControlNodeNull() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = null;
		Element testNode = mockElementWithName("Namespace:AName");
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, testNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_ACCEPT_DIFFERENCE);
	}
	
	@Test
	public void differenceIfTestNodeNull() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = mockElementWithName("Namespace:AName");
		Element testNode = null;
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail, controlNode);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_ACCEPT_DIFFERENCE);
	}
	
	@Test
	public void differenceIfBothNodeAreNull() {
		Difference diff = createMock(Difference.class);
		NodeDetail controlNodeDetail = createMock(NodeDetail.class);
		NodeDetail testNodeDetail = createMock(NodeDetail.class);
		Element controlNode = null;
		Element testNode = null;
		
		expect(diff.getControlNodeDetail()).andReturn(controlNodeDetail);
		expect(diff.getTestNodeDetail()).andReturn(testNodeDetail);
		expect(controlNodeDetail.getNode()).andReturn(controlNode);
		expect(testNodeDetail.getNode()).andReturn(testNode);
		
		replay(diff, controlNodeDetail, testNodeDetail);
		
		DifferenceListener listener = AcceptDifferentNamespaceXMLUnit.newDifferenceListener();
		
		assertThat(listener.differenceFound(diff)).isEqualTo(RETURN_ACCEPT_DIFFERENCE);
	}

	@Test
	public void removeNSWhenSimpleNode() {
		Element controlNode = mockElementWithName("AName");

		replay(controlNode);
		
		String foundName = AcceptDifferentNamespaceXMLUnit.getNodeNameWithoutNamespace(controlNode);
		
		assertThat(foundName).isEqualTo("AName");
	}
	
	@Test
	public void removeNSWhenNodeWithNS() {
		Element controlNode = mockElementWithName("Namespace:AName");

		replay(controlNode);
		
		String foundName = AcceptDifferentNamespaceXMLUnit.getNodeNameWithoutNamespace(controlNode);
		
		assertThat(foundName).isEqualTo("AName");
	}

	@Test(expected=IllegalArgumentException.class)
	public void removeNSWhenNodeWithTwoNS() {
		Element controlNode = mockElementWithName("Namespace:OtherNamespace:AName");

		replay(controlNode);
		
		AcceptDifferentNamespaceXMLUnit.getNodeNameWithoutNamespace(controlNode);
	}

	@Test(expected=IllegalArgumentException.class)
	public void removeNSWhenNodeWithEmptyNS() {
		Element controlNode = mockElementWithName(":AName");

		replay(controlNode);
		
		AcceptDifferentNamespaceXMLUnit.getNodeNameWithoutNamespace(controlNode);
	}

	@Test(expected=IllegalArgumentException.class)
	public void removeNSWhenNodeWithEmptyName() {
		Element controlNode = mockElementWithName("Namespace:");

		replay(controlNode);
		
		AcceptDifferentNamespaceXMLUnit.getNodeNameWithoutNamespace(controlNode);
	}
	
	private Element mockElementWithName(String name) {
		Element element = createMock(Element.class);
		expect(element.getNodeName()).andReturn(name);
		return element;
	}
}

/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.client;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.obm.sync.Parameter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventType;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.common.collect.Multimap;

import fr.aliacom.obm.common.domain.ObmDomain;

public abstract class AbstractClientTest {

	protected Logger logger;
	protected Responder responder;
	protected IMocksControl control;

	@Before
	public final void setUp() {
		control = createControl();
		responder = control.createMock(Responder.class);
		logger = control.createMock(Logger.class);
	}

	@After
	public final void tearDown() {
		control.verify();
	}

	protected Document mockAccessTokenDocument(String email, String displayName, String sid, MavenVersion version, ObmDomain domain) {
		Document doc = control.createMock(Document.class);
		Element root = control.createMock(Element.class);
		Element domainElement = control.createMock(Element.class);
		Element versionElement = control.createMock(Element.class);

		expect(doc.getDocumentElement()).andReturn(root).anyTimes();
		expect(root.getNodeName()).andReturn("token").anyTimes();
		expect(domainElement.getNodeName()).andReturn("domain").anyTimes();
		expect(domainElement.getAttribute("uuid")).andReturn(domain.getUuid().toString()).anyTimes();
		expect(versionElement.getAttribute("major")).andReturn(version.getMajor()).anyTimes();
		expect(versionElement.getAttribute("minor")).andReturn(version.getMinor()).anyTimes();
		expect(versionElement.getAttribute("release")).andReturn(version.getRelease()).anyTimes();

		mockTextElement(root, "email", email);
		mockTextElement(root, "displayname", displayName);
		mockTextElement(root, "sid", sid);
		mockComplexElement(root, "version", versionElement);
		mockComplexElement(root, "domain", domainElement);
		mockTextContent(domainElement, domain.getName());

		return doc;
	}

	protected Document mockErrorDocument(Class<? extends Exception> exceptionClass, String message) {
		Document doc = control.createMock(Document.class);
		Element root = control.createMock(Element.class);

		expect(doc.getDocumentElement()).andReturn(root).anyTimes();
		expect(root.getNodeName()).andReturn("error").anyTimes();

		mockTextElement(root, "message", message);
		if (exceptionClass != null) {
			mockTextElement(root, "type", exceptionClass.getName());
		} else {
			mockEmptyElement(root, "type");
		}

		return doc;
	}

	protected Document mockEmptyResourceInfosDocument() {
		Document doc = control.createMock(Document.class);
		Element root = control.createMock(Element.class);
		NodeList list = control.createMock(NodeList.class);

		expect(doc.getDocumentElement()).andReturn(root).anyTimes();
		expect(root.getNodeName()).andReturn("resourceInfoGroup").anyTimes();
		expect(doc.getElementsByTagName(eq("resourceInfo"))).andReturn(list).anyTimes();
		expect(list.getLength()).andReturn(0).anyTimes();

		return doc;
	}

	protected Document mockEmptyCalendarInfosDocument() {
		Document doc = control.createMock(Document.class);
		Element root = control.createMock(Element.class);
		NodeList list = control.createMock(NodeList.class);

		expect(doc.getDocumentElement()).andReturn(root).anyTimes();
		expect(root.getNodeName()).andReturn("calendar-infos").anyTimes();
		expect(doc.getElementsByTagName(eq("info"))).andReturn(list).anyTimes();
		expect(list.getLength()).andReturn(0).anyTimes();

		return doc;
	}

	protected void mockTextElement(Element root, String elementName, String text) {
		Element element = control.createMock(Element.class);

		mockComplexElement(root, elementName, element);
		mockTextContent(element, text);
	}

	protected void mockTextContent(Element element, String text) {
		Text textNode = control.createMock(Text.class);

		expect(element.getFirstChild()).andReturn(textNode).anyTimes();
		expect(textNode.getData()).andReturn(text).anyTimes();
	}

	protected void mockComplexElement(Element root, String elementName, Element subElement) {
		NodeList list = control.createMock(NodeList.class);

		expect(root.getElementsByTagName(eq(elementName))).andReturn(list).anyTimes();
		expect(list.getLength()).andReturn(1).anyTimes();
		expect(list.item(eq(0))).andReturn(subElement).anyTimes();
	}

	protected void mockEmptyElement(Element root, String elementName) {
		NodeList list = control.createMock(NodeList.class);

		expect(root.getElementsByTagName(eq(elementName))).andReturn(list).anyTimes();
		expect(list.getLength()).andReturn(0).anyTimes();
	}

	protected Event createEvent() {
		Event event = new Event();

		event.setType(EventType.VEVENT);
		event.setExtId(new EventExtId("abc"));

		return event;
	}

	protected static interface Responder {
		Document execute(AccessToken token, String action, Multimap<String, Parameter> parameters);
	}

}

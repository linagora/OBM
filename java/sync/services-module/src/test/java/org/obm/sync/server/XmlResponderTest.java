/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.ServerCapability;
import org.obm.sync.auth.AccessToken;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.UserSettings;

public class XmlResponderTest {

	private AccessToken at;
	private XmlResponder responder;

	@Before
	public void setUp() {
		at = ToolBox.mockAccessToken();
		responder = new XmlResponder(null);
	}

	@Test
	public void testToXMLNoUserSettingsNoCapabilities() throws Exception {
		expect(at.getUserSettings()).andReturn(null).once();
		expect(at.getServerCapabilities()).andReturn(null).once();
		expect(at.getUserDisplayName()).andReturn(null).once();
		replay(at);

		Document doc = responder.toXML(at);

		XMLAssert.assertXMLEqual(loadXMLFile("tokenWithEmptyUserSettings.xml"), DOMUtils.serialize(doc));
		verify(at);
	}

	@Test
	public void testToXMLFiveUserSettingsOneCapability() throws Exception {
		ImmutableMap<String, String> settings = ImmutableMap.of("setting1", "value1", "setting2", "value2", "setting3", "value3", "setting4", "value4", "setting5", "value5");
		ImmutableMap<ServerCapability, String> caps =
				ImmutableMap.of(
						ServerCapability.CALENDAR_HANDLER_SUPPORTS_NOTALLOWEDEXCEPTION, "true");

		expect(at.getUserSettings()).andReturn(new UserSettings(settings)).once();
		expect(at.getServerCapabilities()).andReturn(caps).once();
		expect(at.getUserDisplayName()).andReturn(null).once();
		replay(at);

		Document doc = responder.toXML(at);

		XMLAssert.assertXMLEqual(loadXMLFile("tokenWithFiveUserSettings.xml"), DOMUtils.serialize(doc));
		verify(at);
	}

	@Test
	public void testToXMLAllCapabilities() throws Exception {
		ImmutableMap.Builder<ServerCapability, String> builder = ImmutableMap.builder();

		for (ServerCapability capability : ServerCapability.values()) {
			builder.put(capability, "true");
		}

		expect(at.getUserSettings()).andReturn(null).once();
		expect(at.getServerCapabilities()).andReturn(builder.build()).once();
		expect(at.getUserDisplayName()).andReturn(null).once();
		replay(at);

		Document doc = responder.toXML(at);

		XMLAssert.assertXMLEqual(loadXMLFile("tokenWithAllCapabilities.xml"), DOMUtils.serialize(doc));
		verify(at);
	}

	@Test
	public void testToXMLDisplayName() throws Exception {
		String displayName = "user user";
		expect(at.getUserSettings()).andReturn(null).once();
		expect(at.getServerCapabilities()).andReturn(null).once();
		expect(at.getUserDisplayName()).andReturn(displayName).once();
		replay(at);

		Document doc = responder.toXML(at);

		String loadXMLFile = loadXMLFile("tokenWithDisplayName.xml");
		System.out.println(loadXMLFile);
		String serialize = DOMUtils.serialize(doc);
		System.out.println(serialize);
		XMLAssert.assertXMLEqual(loadXMLFile, serialize);
		verify(at);
	}

	private String loadXMLFile(String file) throws Exception {
		String content = IOUtils.toString(getClass().getResourceAsStream(file));

		return content.replaceAll("\n|\t", "");
	}

}

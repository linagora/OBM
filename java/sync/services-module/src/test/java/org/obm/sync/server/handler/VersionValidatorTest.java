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
package org.obm.sync.server.handler;


import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ClientInformations;
import org.obm.sync.auth.ClientInformations.Parser;
import org.obm.sync.auth.LightningVersion;
import org.obm.sync.auth.OBMConnectorVersionException;
import org.obm.sync.auth.Version;


public class VersionValidatorTest {
	
	private Parser createMockParser(Version connectorVersion, LightningVersion lightningVersion) {
		Parser parser = EasyMock.createMock(Parser.class);
		ClientInformations infos = EasyMock.createMock(ClientInformations.class);
		EasyMock.expect(parser.parse(EasyMock.anyObject(String.class))).andReturn(infos);
		EasyMock.expect(infos.getObmConnectorVersion()).andReturn(connectorVersion);
		EasyMock.expect(infos.getLightningVersion()).andReturn(lightningVersion);
		EasyMock.replay(parser, infos);
		return parser;
	}

	private AccessToken createFakeAccessToken() {
		return new AccessToken(10, "unsused");
	}
	
	@Test
	public void testCheckObmConnectorVersionEqual() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 4, 1, 8, "-rc11"), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckObmConnectorVersionEqualWithOutRC() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 4, 1, 8, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionOlderMajor() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(1, 4, 1, 8, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionOlderMinor() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 1, 1, 8, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionOlderRelease() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 4, 0, 8, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckObmConnectorVersionNewerMajorAndOlderMinor() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(3, 1, 1, 8, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckObmConnectorVersionNewerMinorAndOlderRelease() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 5, 0, 8, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckObmConnectorVersionWithoutReleaseAndNewerRelease() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 5, null, null, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionWithoutReleaseAndOlderRelease() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 2, null, null, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-02"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckLightningVersionDoesntContainsLinagora02() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(3, 0, null, null, null), new LightningVersion(1, 0, null, null, "b2"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckLightningVersionContainsLinagora01() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(3, 0, null, null, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-01"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckLightningVersionContainsLinagora03() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(3, 0, null, null, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-03"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckLightningVersionContainsLinagora12() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(3, 0, null, null, null), new LightningVersion(1, 0, null, null, "b2-LINAGORA-12"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckLightningVersionContainsLinagoraDot3() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(3, 0, null, null, null), new LightningVersion(1, 0, null, null, "b2.03obm"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckLightningReleaseContainsLinagoraDot3() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(3, 0, null, null, null), new LightningVersion(1, 0, null, null, ".03obm"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckLightning09ReleaseContainsLinagoraDot3() throws OBMConnectorVersionException {
		Parser parser = createMockParser(new Version(2, 4, 1, 12, null), new LightningVersion(0, 9, null, null, ".03obm"));
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckObmConnectorVersionEmpty() throws OBMConnectorVersionException {
		Parser parser = EasyMock.createMock(Parser.class);
		EasyMock.expect(parser.parse(EasyMock.anyObject(String.class))).andReturn(null);
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
	@Test
	public void testCheckNullLightningSuffix() {
		Parser parser = createMockParser(new Version(2, 4, 0, 1, null), new LightningVersion(0, 8, null, null, null));
		VersionValidator versionValidator = new VersionValidator(parser);
		Assert.assertNotNull(versionValidator);
	}
	
}

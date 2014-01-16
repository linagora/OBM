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
package org.obm.sync.auth;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;



import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class ConnectorVersionParserTest {

	private void testVersion(Version obmConnectorVersion, int major, int minor, Integer release, Integer subRelease, String suffix) {
		Assert.assertEquals(major, obmConnectorVersion.getMajor());
		Assert.assertEquals(minor, obmConnectorVersion.getMinor());
		Assert.assertEquals(release, obmConnectorVersion.getRelease());
		Assert.assertEquals(subRelease, obmConnectorVersion.getSubRelease());
		Assert.assertEquals(suffix, obmConnectorVersion.getSuffix());
	}

	private void testLightningVersion(LightningVersion lightningVersion, int major, int minor, Integer release, Integer subRelease, String suffix, Integer linagoraVersion) {
		testVersion(lightningVersion, major, minor, release, subRelease, suffix);
		Assert.assertEquals(linagoraVersion, lightningVersion.getLinagoraVersion());
	}

	
	@Test
	public void parseValidString() {
		String valid = "thunderbird[ext: 2.4.1.8-rc11, light: 1.0b2]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 8, "-rc11");
		testLightningVersion(infos.getLightningVersion(), 1, 0, null, null, "b2", null);
	}

	@Test
	public void parseInvalidString() {
		String valid = "thunder[ext: 2.4.1.8-rc11, light: 1.0b2]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		Assert.assertNull(infos);
	}
	
	@Test
	public void parseInvalidString2() {
		String valid = "thunderbird[ext: 2.4.1.8-rc11,]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		Assert.assertNull(infos);
	}
	
	@Test
	public void parseLinagoraPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.8, light: 1.0b2-LINAGORA-02]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 8, "");
		testLightningVersion(infos.getLightningVersion(), 1, 0, null, null, "b2-LINAGORA-02", 2);
	}
	
	@Test
	public void parseObmPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.8, light: 1.0b2.03obm]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 8, "");
		testLightningVersion(infos.getLightningVersion(), 1, 0, null, null, "b2.03obm", 3);
	}

	@Test
	public void parse10ReleaseObmPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.8, light: 1.0.03obm]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 8, "");
		testLightningVersion(infos.getLightningVersion(), 1, 0, null, null, "3obm", 3);
	}
	
	@Test
	public void parse101ReleaseObmPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.8, light: 1.0.1.03obm]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 8, "");
		testLightningVersion(infos.getLightningVersion(), 1, 0, 1, null, "3obm", 3);
	}
	
	@Test
	public void parse1013ReleaseObmPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.8, light: 1.0.1.3.03obm]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 8, "");
		testLightningVersion(infos.getLightningVersion(), 1, 0, 1, 3, ".03obm", 3);
	}
	
	@Test
	public void parse101Release11ObmPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.8, light: 1.0.1.11obm]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 8, "");
		testLightningVersion(infos.getLightningVersion(), 1, 0, 1, null, "11obm", 11);
	}
	
	@Test
	public void parse09ObmPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.12, light: 0.9.03obm]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		testVersion(infos.getObmConnectorVersion(), 2, 4, 1, 12, "");
		testLightningVersion(infos.getLightningVersion(), 0, 9, null, null, "3obm", 3);
	}
	
	@Test
	public void parseBadObmPatchedString() {
		String valid = "thunderbird[ext: 2.4.1.8, light: 1.03obm]";
		ClientInformations.Parser parser = new ClientInformations.Parser();
		ClientInformations infos = parser.parse(valid);
		Assert.assertNull(infos.getLightningVersion().getLinagoraVersion());
	}
	
}

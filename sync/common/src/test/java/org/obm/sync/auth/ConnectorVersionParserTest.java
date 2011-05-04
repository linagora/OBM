package org.obm.sync.auth;

import org.junit.Assert;
import org.junit.Test;


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

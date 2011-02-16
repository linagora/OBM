package org.obm.sync.server.handler;


import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ClientInformations.Parser;
import org.obm.sync.auth.ClientInformations;
import org.obm.sync.auth.LightningVersion;
import org.obm.sync.auth.OBMConnectorVersionException;
import org.obm.sync.auth.Version;

public class VersionValidatorTest{
	
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
		return new AccessToken(10, 2, "unsused");
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
	public void testCheckObmConnectorVersionEmpty() throws OBMConnectorVersionException {
		Parser parser = EasyMock.createMock(Parser.class);
		EasyMock.expect(parser.parse(EasyMock.anyObject(String.class))).andReturn(null);
		VersionValidator validator = new VersionValidator(parser);
		validator.checkObmConnectorVersion(createFakeAccessToken());
	}
	
}

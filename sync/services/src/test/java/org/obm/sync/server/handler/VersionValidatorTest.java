package org.obm.sync.server.handler;


import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.OBMConnectorVersionException;

public class VersionValidatorTest{
	
	@Test
	public void testCheckObmConnectorVersionEqual() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 2.4.1.8-rc11, light: 1.0b2]"));
	}
	
	@Test
	public void testCheckObmConnectorVersionEqualWithOutRC() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 2.4.1.8, light: 1.0b2]"));
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionOlderMajor() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 1.4.1.8, light: 1.0b2]"));
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionOlderMinor() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 2.1.1.8, light: 1.0b2]"));
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionOlderRelease() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 2.4.0.8, light: 1.0b2]"));
	}
	
	@Test
	public void testCheckObmConnectorVersionNewerMajorAndOlderMinor() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 3.1.1.8, light: 1.0b2]"));
	}
	
	@Test
	public void testCheckObmConnectorVersionNewerMinorAndOlderRelease() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 2.5.0.8, light: 1.0b2]"));
	}
	
	@Test
	public void testCheckObmConnectorVersionWithoutReleaseAndNewerRelease() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 2.5, light: 1.0b2]"));
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionWithoutReleaseAndOlderRelease() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext: 2.2, light: 1.0b2]"));
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionEmpty() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[]"));
	}
	
	@Test(expected=OBMConnectorVersionException.class)
	public void testCheckObmConnectorVersionEmptyExt() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "thunderbird[ext:]"));
	}
	
	
	@Test
	public void testCheckNotObmConnectorVersion() throws OBMConnectorVersionException {
		VersionValidator validator = new VersionValidator();
		validator.checkObmConnectorVersion(new AccessToken(10, 2, "opush"));
	}
}

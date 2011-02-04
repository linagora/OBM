package org.obm.sync.auth;

import org.junit.Assert;
import org.junit.Test;

public class ConnectorVersionTest {

	@Test
	public void compareEquals() {
		ConnectorVersion  c1 = new ConnectorVersion(1, 2, 3, 4);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(0, c1.compareTo(c2));
	}
	
	@Test
	public void compareMajorHigher() {
		ConnectorVersion  c1 = new ConnectorVersion(2, 2, 3, 4);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareMinorHigher() {
		ConnectorVersion  c1 = new ConnectorVersion(1, 3, 3, 4);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareReleaseHigher() {
		ConnectorVersion  c1 = new ConnectorVersion(1, 2, 4, 4);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareSubReleaseHigher() {
		ConnectorVersion  c1 = new ConnectorVersion(1, 2, 3, 5);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareMajorLower() {
		ConnectorVersion  c1 = new ConnectorVersion(0, 2, 3, 4);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
	
	@Test
	public void compareMinorLower() {
		ConnectorVersion  c1 = new ConnectorVersion(1, 1, 3, 4);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
	
	@Test
	public void compareReleaseLower() {
		ConnectorVersion  c1 = new ConnectorVersion(1, 2, 2, 4);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
	
	@Test
	public void compareSubReleaseLower() {
		ConnectorVersion  c1 = new ConnectorVersion(1, 2, 3, 3);
		ConnectorVersion  c2 = new ConnectorVersion(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
}

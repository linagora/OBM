package org.obm.sync.auth;

import org.junit.Assert;
import org.junit.Test;

public class ConnectorVersionTest {

	@Test
	public void compareEquals() {
		Version  c1 = new Version(1, 2, 3, 4);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(0, c1.compareTo(c2));
	}
	
	@Test
	public void compareMajorHigher() {
		Version  c1 = new Version(2, 2, 3, 4);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareMinorHigher() {
		Version  c1 = new Version(1, 3, 3, 4);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareReleaseHigher() {
		Version  c1 = new Version(1, 2, 4, 4);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareSubReleaseHigher() {
		Version  c1 = new Version(1, 2, 3, 5);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(10, c1.compareTo(c2));
	}
	
	@Test
	public void compareMajorLower() {
		Version  c1 = new Version(0, 2, 3, 4);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
	
	@Test
	public void compareMinorLower() {
		Version  c1 = new Version(1, 1, 3, 4);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
	
	@Test
	public void compareReleaseLower() {
		Version  c1 = new Version(1, 2, 2, 4);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
	
	@Test
	public void compareSubReleaseLower() {
		Version  c1 = new Version(1, 2, 3, 3);
		Version  c2 = new Version(1, 2, 3, 4);
		Assert.assertEquals(-10, c1.compareTo(c2));
	}
}

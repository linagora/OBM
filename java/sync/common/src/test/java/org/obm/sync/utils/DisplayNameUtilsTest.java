package org.obm.sync.utils;


import org.junit.Assert;
import org.junit.Test;
import org.obm.sync.utils.DisplayNameUtils;

public class DisplayNameUtilsTest {
	
	@Test
	public void getDisplayNameNotNullParam() {
		String disp = DisplayNameUtils.getDisplayName("common", "first", "last");
		Assert.assertEquals("common", disp);
	}
	
	@Test
	public void getDisplayNameNullFirstAndEmptyName() {
		String disp = DisplayNameUtils.getDisplayName("common", null, "");
		Assert.assertEquals("common", disp);
	}
	
	@Test
	public void getDisplayNameEmptyFirstAndNullName() {
		String disp = DisplayNameUtils.getDisplayName("common", "", null);
		Assert.assertEquals("common", disp);
	}

	@Test
	public void getDisplayNameNullCommon() {
		String disp = DisplayNameUtils.getDisplayName(null, "first", "last");
		Assert.assertEquals("first last", disp);
	}
	
	@Test
	public void getDisplayNameEmptyCommon() {
		String disp = DisplayNameUtils.getDisplayName("", "first", "last");
		Assert.assertEquals("first last", disp);
	}
	
	@Test
	public void getDisplayNameNullCommonAndFirst() {
		String disp = DisplayNameUtils.getDisplayName(null, null, "last");
		Assert.assertEquals("last", disp);
	}
	
	@Test
	public void getDisplayNameEmptyCommonAndName() {
		String disp = DisplayNameUtils.getDisplayName("", "", "last");
		Assert.assertEquals("last", disp);
	}
	
	@Test
	public void getDisplayNameNullCommonAndLast() {
		String disp = DisplayNameUtils.getDisplayName(null, "first", "");
		Assert.assertEquals("first", disp);
	}
	
	@Test
	public void getDisplayNameEmptyCommonAndLast() {
		String disp = DisplayNameUtils.getDisplayName("", "first", "");
		Assert.assertEquals("first", disp);
	}
	
	@Test
	public void getDisplayNameAllEmpty() {
		String disp = DisplayNameUtils.getDisplayName("", "", "");
		Assert.assertEquals("", disp);
	}
	
	@Test
	public void getDisplayNameAllNull() {
		String disp = DisplayNameUtils.getDisplayName(null, null, null);
		Assert.assertEquals("", disp);
	}
	
}

/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.utils;

import junit.framework.Assert;

import org.junit.Test;

public class HelperTest {
	
	@Test
	public void testGetLoginFromEmailFromNull() {
		Helper helper = new Helper(null);
		String username = helper.getLoginFromEmail(null);
		Assert.assertEquals("", username);
	}
	
	@Test
	public void testGetLoginFromEmailFromEmpty() {
		Helper helper = new Helper(null);
		String username = helper.getLoginFromEmail("");
		Assert.assertEquals("", username);
	}
	
	@Test
	public void testGetLoginFromEmailFromWithoutDomain() {
		Helper helper = new Helper(null);
		String username = helper.getLoginFromEmail("john");
		Assert.assertEquals("john", username);
	}
	
	@Test
	public void testGetLoginFromEmailFromLoginAtDomain() {
		Helper helper = new Helper(null);
		String username = helper.getLoginFromEmail("john@test.tlse.lng");
		Assert.assertEquals("john", username);
	}
	
}

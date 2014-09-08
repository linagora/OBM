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
package org.obm.push.utils;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;


public class LdapUtilsTest {

	private IMocksControl control;
	private DirContext ctx;
	private LdapUtils ldapUtils;
	private NamingEnumeration<SearchResult> namingEnumeration;
	private NamingEnumeration<Attribute> ae;
	private SearchResult searchResult;
	private Attributes attributes;

	@Before
	public void setUp() {
		control = createControl();
		ctx = control.createMock(DirContext.class);
		namingEnumeration = control.createMock(NamingEnumeration.class);
		ae = control.createMock(NamingEnumeration.class);
		searchResult = control.createMock(SearchResult.class);
		attributes = control.createMock(Attributes.class);
		ldapUtils = new LdapUtils(ctx, "baseDN");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNamingEnumerationAreClosed() throws NamingException {
		expect(ctx.search(eq("baseDN"), eq("aFilter"), anyObject(SearchControls.class))).andReturn(namingEnumeration);
		expect(namingEnumeration.hasMore()).andReturn(true);
		expect(namingEnumeration.next()).andReturn(searchResult);
		expect(searchResult.getAttributes()).andReturn(attributes);
		expect((NamingEnumeration<Attribute>)attributes.getAll()).andReturn(ae);
		expect(ae.hasMoreElements()).andReturn(false);
		expect(namingEnumeration.hasMore()).andReturn(false);

		namingEnumeration.close();
		expectLastCall().once();
		ae.close();
		expectLastCall().once();

		control.replay();
		ldapUtils.getAttributes("aFilter", "aQuery", null);
		control.verify();
	}
}

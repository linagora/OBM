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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.assertj.core.data.MapEntry;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;


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

	@Test
	public void getAttributesShouldReturnEmptyWhenNone() throws NamingException {
		expect(ctx.search(eq("baseDN"), eq("aFilter"), anyObject(SearchControls.class))).andReturn(namingEnumeration);
		expect(namingEnumeration.hasMore()).andReturn(false);

		namingEnumeration.close();
		expectLastCall().once();

		control.replay();
		List<Map<String, List<String>>> results = ldapUtils.getAttributes("aFilter", "aQuery", null);
		control.verify();
		
		assertThat(results).isEmpty();
	}

	@Test
	public void getAttributesShouldReturnEmptyEntryWhenEmtpyAtttributes() throws NamingException {
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
		List<Map<String, List<String>>> results = ldapUtils.getAttributes("aFilter", "aQuery", null);
		control.verify();
		
		assertThat(results).hasSize(1);
		assertThat(results.get(0)).isEmpty();
	}

	@Test
	public void getAttributesShouldReturnEntryWhenOne() throws NamingException {
		Attribute attribute = new BasicAttribute("key", "value");
		
		expect(ctx.search(eq("baseDN"), eq("aFilter"), anyObject(SearchControls.class))).andReturn(namingEnumeration);
		expect(namingEnumeration.hasMore()).andReturn(true);
		expect(namingEnumeration.next()).andReturn(searchResult);
		expect(searchResult.getAttributes()).andReturn(attributes);
		expect((NamingEnumeration<Attribute>)attributes.getAll()).andReturn(ae);
		expect(ae.hasMoreElements()).andReturn(true);
		expect(ae.next()).andReturn(attribute);
		expect(ae.hasMoreElements()).andReturn(false);
		expect(namingEnumeration.hasMore()).andReturn(false);
		
		namingEnumeration.close();
		expectLastCall().once();
		ae.close();
		expectLastCall().once();

		control.replay();
		List<Map<String, List<String>>> results = ldapUtils.getAttributes("aFilter", "aQuery", null);
		control.verify();
		
		LinkedList<Object> expectedValues = Lists.newLinkedList();
		expectedValues.add("value");
		assertThat(results).hasSize(1);
		assertThat(results.get(0)).contains(MapEntry.entry("key", expectedValues));
	}
	
	@Test
	public void getAttributesShouldReturnSilentlyWhenSizeLimitExceededException() throws NamingException {
		expect(ctx.search(eq("baseDN"), eq("aFilter"), anyObject(SearchControls.class))).andReturn(namingEnumeration);
		expect(namingEnumeration.hasMore()).andReturn(true);
		expect(namingEnumeration.next()).andReturn(searchResult);
		expect(searchResult.getAttributes()).andReturn(attributes);
		expect((NamingEnumeration<Attribute>)attributes.getAll()).andReturn(ae);
		expect(ae.hasMoreElements()).andReturn(false);
		expect(namingEnumeration.hasMore()).andThrow(new SizeLimitExceededException());

		namingEnumeration.close();
		expectLastCall().once();
		ae.close();
		expectLastCall().once();
		
		control.replay();
		List<Map<String, List<String>>> results = ldapUtils.getAttributes("aFilter", "aQuery", null);
		control.verify();
		
		assertThat(results).hasSize(1);
		assertThat(results.get(0)).isEmpty();
	}
	
	@Test
	public void getAttributesShouldRespectLimit() throws NamingException {
		int limit = 1;
		Attribute attribute = new BasicAttribute("key", "value");
		
		expect(ctx.search(eq("baseDN"), eq("aFilter"), anyObject(SearchControls.class))).andReturn(namingEnumeration);
		expect(namingEnumeration.hasMore()).andReturn(true);
		expect(namingEnumeration.next()).andReturn(searchResult);
		expect(searchResult.getAttributes()).andReturn(attributes);
		expect((NamingEnumeration<Attribute>)attributes.getAll()).andReturn(ae);
		expect(ae.hasMoreElements()).andReturn(true);
		expect(ae.next()).andReturn(attribute);
		expect(ae.hasMoreElements()).andReturn(false);

		namingEnumeration.close();
		expectLastCall().once();
		ae.close();
		expectLastCall().once();
		
		control.replay();
		List<Map<String, List<String>>> results = ldapUtils.getAttributes("aFilter", "aQuery", limit, null);
		control.verify();

		LinkedList<Object> expectedValues = Lists.newLinkedList();
		expectedValues.add("value");
		assertThat(results).hasSize(1);
		assertThat(results.get(0)).contains(MapEntry.entry("key", expectedValues));
	}
}

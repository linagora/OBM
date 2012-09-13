/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.solr;

import static org.easymock.EasyMock.*;

import java.net.MalformedURLException;

import junit.framework.Assert;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.junit.Before;
import org.junit.Test;
import org.obm.dbcp.ConfigurationServiceFixturePostgreSQL;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;

import fr.aliacom.obm.ToolBox;

public class SolrHelperFactoryTest {

	private Contact contact;
	private AccessToken accessToken;
	private LocatorService locatorClient;
	private ContactIndexer.Factory contactIndexerFactory;
	private ContactIndexer contactIndexer;
	private SolrHelper.Factory factory;
	private SolrManager manager;

	@Before
	public void setUp() {
		contact = new Contact();
		accessToken = ToolBox.mockAccessToken();
		locatorClient = createMock(LocatorService.class);
		contactIndexerFactory = createMockBuilder(ContactIndexer.Factory.class).addMockedMethod("createIndexer").createMock();
		contactIndexer = createMockBuilder(ContactIndexer.class).createMock();
		manager = new SolrManager(new ConfigurationServiceFixturePostgreSQL());
		factory = new SolrHelper.Factory(contactIndexerFactory, null, locatorClient, manager);

		contact.setUid(1);

		expect(locatorClient.getServiceLocation(isA(String.class), isA(String.class))).andReturn(null).anyTimes();
		expect(contactIndexerFactory.createIndexer(isA(CommonsHttpSolrServer.class), eq(contact))).andReturn(contactIndexer).once();

		replay(accessToken, locatorClient, contactIndexerFactory);
	}

	@Test(expected = IllegalStateException.class)
	public void create_contact_solr_remains_unavailable() throws MalformedURLException, LocatorClientException {
		index(false);
	}

	@Test(expected = IllegalStateException.class)
	public void delete_contact_solr_remains_unavailable() throws MalformedURLException, LocatorClientException {
		delete(false);
	}

	@Test
	public void create_contact_solr_available() throws MalformedURLException, LocatorClientException {
		try {
			index(true);
		}
		catch (IllegalStateException e) {
			fail();
		}
	}

	@Test
	public void delete_contact_solr_available() throws MalformedURLException, LocatorClientException {
		try {
			delete(true);
		}
		catch (IllegalStateException e) {
			fail();
		}
	}

	@Test
	public void create_contact_solr_becomes_available() throws MalformedURLException, LocatorClientException {
		try {
			index(false);
		}
		catch (IllegalStateException e) {
		}
		create_contact_solr_available();
	}

	@Test
	public void delete_contact_solr_becomes_available() throws MalformedURLException, LocatorClientException {
		try {
			delete(false);
		}
		catch (IllegalStateException e) {
		}
		delete_contact_solr_available();
	}

	@Test(expected = IllegalStateException.class)
	public void create_contact_solr_becomes_unavailable() throws MalformedURLException, LocatorClientException {
		create_contact_solr_available();
		index(false);
	}

	@Test(expected = IllegalStateException.class)
	public void delete_contact_solr_becomes_unavailable() throws MalformedURLException, LocatorClientException {
		delete_contact_solr_available();
		delete(false);
	}

	private void index(boolean solrUp) throws MalformedURLException {
		manager.setSolrAvailable(solrUp);
		factory.createClient(accessToken).createOrUpdate(contact);
	}

	private void delete(boolean solrUp) throws MalformedURLException {
		manager.setSolrAvailable(solrUp);
		factory.createClient(accessToken).delete(contact);
	}

	private void fail() {
		Assert.fail("creation should silently pass when the Indexer is available");
	}
}

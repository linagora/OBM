/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.opush.windowing;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.obm.push.bean.SyncKey;
import org.obm.push.mail.EmailChanges;
import org.obm.push.mail.WindowingService;
import org.obm.push.mail.bean.Email;
import org.obm.push.store.ehcache.ObjectStoreManager;

import bitronix.tm.TransactionManagerServices;

import com.google.common.base.Function;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ranges;
import com.google.inject.Inject;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class WindowingStepdefs {

	private final WindowingService windowingService;
	private final TransactionManager tm;
	private final ObjectStoreManager storeManager;

	private SyncKey syncKey;
	private EmailChanges inbox;
	private EmailChanges retrievedElements;
	private int elements;
	private int retreivingChangesIteration;
	private int retreivingChangesSum;
	
	@Inject
	public WindowingStepdefs(WindowingService windowingService, ObjectStoreManager storeManager) {
		this.windowingService = windowingService;
		this.storeManager = storeManager;
		this.tm = TransactionManagerServices.getTransactionManager();
	}
	
	@Before
	public void setup() throws NotSupportedException, SystemException {
		tm.begin();
		syncKey = new SyncKey(UUID.randomUUID().toString());		
	}
	
	@After
	public void shutdown() throws IllegalStateException, SecurityException, SystemException {
		tm.rollback();
		storeManager.shutdown();
	}
	
	@Given("user has (\\d+) elements in INBOX")
	public void elementsInInbox(int elements) {
		this.elements = elements;
		inbox = generateEmails(elements);
	}
	
	@When("user ask for the first (\\d+) elements")
	public void retrieveFirstElements(int elements) {
		startToRetrieveElements();
		windowingService.pushPendingElements(syncKey, inbox, elements);
		retrieveElements(elements);
	}
	
	@When("user ask for the next (\\d+) elements")
	public void retrieveNextElements(int elements) {
		startToRetrieveElements();
		retrieveElements(elements);
	}
	
	@When("user ask repeatedly for (\\d+) elements")
	public void retreiveUntilPendingElementIsEmpty(int elements) {
		startToRetrieveElements();
		while (this.elements > 0) {
			retrieveElements(elements);
			retreivingChangesSum += retrievedElements.sumOfChanges();
			retreivingChangesIteration++;
		}
	}

	private void retrieveElements(int elements) {
		retrievedElements = windowingService.popNextPendingElements(syncKey, elements);
		this.elements -= retrievedElements.sumOfChanges();
	}
	
	@Then("user get (\\d+) elements")
	public void assertRetrievedElement(int elements) {
		assertThat(retrievedElements).isEqualTo(generateEmails(this.elements, elements));
	}
	
	@Then("there is (\\d+) elements left in store")
	public void assertElementsInStore(int elements) {
		assertThat(windowingService.hasPendingElements(syncKey)).isEqualTo(elements > 0);
		EmailChanges pendingChanges = windowingService.popNextPendingElements(syncKey, Integer.MAX_VALUE);
		assertThat(pendingChanges).isEqualTo(generateEmails(elements));
	}
	
	@When("user get (\\d+) elements in (\\d+) iterations")
	public void retreiveUntilPendingElementAssertion(int elements, int iterations) {
		assertThat(retreivingChangesSum).isEqualTo(elements);
		assertThat(retreivingChangesIteration).isEqualTo(iterations);
	}

	private void startToRetrieveElements() {
		retrievedElements = null;
		retreivingChangesIteration = 0;
		retreivingChangesSum = 0;
	}
	
	private EmailChanges generateEmails(long number) {
		return generateEmails(0, number);
	}
	
	private EmailChanges generateEmails(long start, long number) {
		return EmailChanges.builder()
				.additions(
					FluentIterable.from(Ranges.closedOpen(start, start + number).asSet(DiscreteDomains.longs()))
						.transform(new Function<Long, Email>() {
							@Override
							public Email apply(Long uid) {
								return Email.builder().uid(uid).build();
							}
						}).toImmutableSet())
				.build();
	}
	
}

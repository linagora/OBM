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
package org.obm.push.mail.imap.testsuite;

import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.ImapTimeoutException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.imap.SlowGuiceRunner;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

@RunWith(SlowGuiceRunner.class) @Slow
public abstract class MailboxTimeoutTest {

	@Inject MailboxService mailboxService;
	@Inject GreenMail greenMail;
	@Inject CollectionPathHelper collectionPathHelper;
	
	private UserDataRequest udr;
	private String mailbox;
	private String password;
	
	@Before
	public void setup() {
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
					.createUser(mailbox, mailbox, null), password), null, null);
	}
	
	@After
	public void teardown() {
		greenMail.stop();
	}
	
	@Test(expected=ImapTimeoutException.class)
	public void fetchTooSlow() throws InterruptedException {
		Stopwatch stopwatch = new Stopwatch();
		String inboxPath = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, IMAP_INBOX_NAME);
		//This one is for warming the stack
		mailboxService.fetchUIDNext(udr, inboxPath);
		lockGreenmailAndReleaseAfter(20);
		stopwatch.start();
		try {
			mailboxService.fetchUIDNext(udr, inboxPath);
		} finally {
			assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(5000).isLessThan(6000);
		}
	}

	private void lockGreenmailAndReleaseAfter(int timeoutInSeconds) throws InterruptedException {
		lock();
		releaseLockAfter(timeoutInSeconds);
	}

	private void lock() throws InterruptedException {
		greenMail.getLock().acquire();
	}
	
	private void releaseLockAfter(int timeoutInSecond) {
		new Timer().schedule(
				new TimerTask() {
			
			@Override
			public void run() {
				greenMail.getLock().release();
			}
		}, timeoutInSecond * 1000);
	}
	
}

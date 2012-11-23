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
package org.obm.push.mail;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.bean.Email;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@RunWith(SlowFilterRunner.class) @Slow
public class EmailChangesComputerImplTest {

	@Test(expected=NullPointerException.class)
	public void testComputeChangesBeforeNull() {
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		emailChangesComputerImpl.computeChanges(null, ImmutableSet.<Email> of());
	}

	@Test(expected=NullPointerException.class)
	public void testComputeChangesActualNull() {
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		emailChangesComputerImpl.computeChanges(ImmutableSet.<Email> of(), null);
	}

	@Test
	public void testComputeChangesEmptySets() {
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		EmailChanges emailChanges = emailChangesComputerImpl.computeChanges(ImmutableSet.<Email> of(), ImmutableSet.<Email> of());
		
		assertThat(emailChanges.getDeletions()).isEmpty();
		assertThat(emailChanges.getChanges()).isEmpty();
		assertThat(emailChanges.getAdditions()).isEmpty();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testComputeChangesDuplicateKeys() {
		Date currentDate = DateUtils.getCurrentDate();
		Email email = new Email(1, true, currentDate);
		Email email2 = new Email(1, false, currentDate);
		
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		emailChangesComputerImpl.computeChanges(ImmutableSet.<Email> of(email, email2), ImmutableSet.<Email> of());
	}
	
	@Test
	public void testComputeChangesOnlyDeletions() {
		Date currentDate = DateUtils.getCurrentDate();
		Email email = new Email(1, true, currentDate);
		Email email2 = new Email(2, true, currentDate);
		
		ImmutableSet<Email> before = ImmutableSet.<Email> of(email, email2);
		ImmutableSet<Email> actual = ImmutableSet.<Email> of();
		
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		EmailChanges emailChanges = emailChangesComputerImpl.computeChanges(before, actual);
		
		assertThat(emailChanges.getDeletions()).containsOnly(email, email2);
		assertThat(emailChanges.getChanges()).isEmpty();
		assertThat(emailChanges.getAdditions()).isEmpty();
	}
	
	@Test
	public void testComputeChangesOnlyChanges() {
		Date currentDate = DateUtils.getCurrentDate();
		Email email = new Email(1, false, currentDate);
		Email email2 = new Email(2, false, currentDate);
		
		ImmutableSet<Email> before = ImmutableSet.<Email> of(email, email2);
		
		Email email3 = new Email(1, true, currentDate);
		Email email4 = new Email(2, true, currentDate);
		ImmutableSet<Email> actual = ImmutableSet.<Email> of(email3, email4);
		
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		EmailChanges emailChanges = emailChangesComputerImpl.computeChanges(before, actual);
		
		assertThat(emailChanges.getDeletions()).isEmpty();
		assertThat(emailChanges.getChanges()).containsOnly(email3, email4);
		assertThat(emailChanges.getAdditions()).isEmpty();
	}
	
	@Test
	public void testComputeChangesOnlyAdditions() {
		Date currentDate = DateUtils.getCurrentDate();
		Email email = new Email(1, true, currentDate);
		Email email2 = new Email(2, true, currentDate);
		
		ImmutableSet<Email> before = ImmutableSet.<Email> of();
		ImmutableSet<Email> actual = ImmutableSet.<Email> of(email, email2);
		
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		EmailChanges emailChanges = emailChangesComputerImpl.computeChanges(before, actual);
		
		assertThat(emailChanges.getDeletions()).isEmpty();
		assertThat(emailChanges.getChanges()).isEmpty();
		assertThat(emailChanges.getAdditions()).containsOnly(email, email2);
	}
	
	@Test
	public void testComputeChangesMixed() {
		Date currentDate = DateUtils.getCurrentDate();
		Email deleted = new Email(1, true, currentDate);
		Email changed = new Email(2, false, currentDate);
		Email same = new Email(4, false, currentDate);
		ImmutableSet<Email> before = ImmutableSet.<Email> of(deleted, changed, same);
		
		Email changed2 = new Email(2, true, currentDate);
		Email added = new Email(3, false, currentDate);
		Email same2 = new Email(4, false, currentDate);
		ImmutableSet<Email> actual = ImmutableSet.<Email> of(changed2, added, same2);
		
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		EmailChanges emailChanges = emailChangesComputerImpl.computeChanges(before, actual);
		
		assertThat(emailChanges.getDeletions()).containsOnly(deleted);
		assertThat(emailChanges.getChanges()).containsOnly(changed2);
		assertThat(emailChanges.getAdditions()).containsOnly(added);
	}
	
	@Test
	public void testComputeChangesPerformances() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Date currentDate = DateUtils.getCurrentDate();
		Set<Email> before = Sets.newHashSet();
		Set<Email> actual = Sets.newHashSet();
		Boolean threeValues = null;
		int numberOfEmails = 20000 * 3;
		for (int i = 0; i < numberOfEmails; i++) {
			if (threeValues == null) {
				Email deleted = new Email(i, true, currentDate);
				before.add(deleted);
				threeValues = true;
				continue;
			} else if (threeValues){
				Email changed = new Email(i, false, currentDate);
				before.add(changed);
				Email changed2 = new Email(i, true, currentDate);
				actual.add(changed2);
				threeValues = false;
				continue;
			} else {
				Email added = new Email(i, true, currentDate);
				actual.add(added);
				threeValues = null;
			}
		}
		
		EmailChangesComputerImpl emailChangesComputerImpl = new EmailChangesComputerImpl();
		EmailChanges emailChanges = emailChangesComputerImpl.computeChanges(before, actual);
		
		stopWatch.stop();
		System.out.println("Execution time : " + stopWatch.getTime());
		
		assertThat(emailChanges.getDeletions()).hasSize(numberOfEmails / 3);
		assertThat(emailChanges.getChanges()).hasSize(numberOfEmails / 3);
		assertThat(emailChanges.getAdditions()).hasSize(numberOfEmails / 3);
	}
}

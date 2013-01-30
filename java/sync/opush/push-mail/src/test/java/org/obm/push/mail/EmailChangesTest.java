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
import static org.obm.DateUtils.date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.EmailChanges.SplittedEmailChanges;
import org.obm.push.mail.bean.Email;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@RunWith(SlowFilterRunner.class)
public class EmailChangesTest {

	@Test
	public void buildWithNullDeletions() {
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of())
			.additions(ImmutableSet.<Email>of())
			.deletions(null)
			.build();
		
		assertThat(emailChanges.deletions()).isEmpty();
	}

	@Test
	public void buildWithNullChanges() {
		EmailChanges emailChanges = EmailChanges.builder()
			.deletions(ImmutableSet.<Email>of())
			.additions(ImmutableSet.<Email>of())
			.changes(null)
			.build();
		
		assertThat(emailChanges.changes()).isEmpty();
	}
	
	@Test
	public void buildWithNullAdditions() {
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of())
			.deletions(ImmutableSet.<Email>of())
			.additions(null)
			.build();
		
		assertThat(emailChanges.additions()).isEmpty();
	}

	@Test
	public void buildOneEmailInEachCollection() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();

		assertThat(emailChanges.changes()).containsOnly(change);
		assertThat(emailChanges.deletions()).containsOnly(deletion);
		assertThat(emailChanges.additions()).containsOnly(addition);
	}

	@Test
	public void testHasChangesWhenNothing() {
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of())
			.deletions(ImmutableSet.<Email>of())
			.additions(ImmutableSet.<Email>of())
			.build();
		
		assertThat(emailChanges.hasChanges()).isFalse();
	}

	@Test
	public void testHasChangesWhenOnlyAdd() {
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of())
			.deletions(ImmutableSet.<Email>of())
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		assertThat(emailChanges.hasChanges()).isTrue();
	}

	@Test
	public void testHasChangesWhenOnlyChange() {
		Email change = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of())
			.additions(ImmutableSet.<Email>of())
			.build();
		
		assertThat(emailChanges.hasChanges()).isTrue();
	}

	@Test
	public void testHasChangesWhenOnlyDeletion() {
		Email deletion = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of())
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of())
			.build();
		
		assertThat(emailChanges.hasChanges()).isTrue();
	}

	@Test
	public void testHasChangesWhenAll() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		assertThat(emailChanges.hasChanges()).isTrue();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSplittingZero() {
		EmailChanges emailChanges = EmailChanges.builder().build();
		
		emailChanges.splitToFit(0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSplittingNegative() {
		EmailChanges emailChanges = EmailChanges.builder().build();
		
		emailChanges.splitToFit(-1);
	}

	@Test
	public void testSplittingEmpty() {
		EmailChanges emailChanges = EmailChanges.builder().build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(10);
		
		assertThat(splittedEmailChanges.getFittingEmailChanges()).isEqualTo(EmailChanges.builder().build());
		assertThat(splittedEmailChanges.getRemainingEmailChanges()).isEqualTo(EmailChanges.builder().build());
	}

	@Test
	public void testSplittingAdditionsSizeLowerThanEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.additions(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(3);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingChangesSizeLowerThanEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(0);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(3);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingDeletionsSizeLowerThanEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.deletions(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(0);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(3);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingAdditionsSizeEqualsEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.additions(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(3);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingChangesSizeEqualsEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(0);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(3);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingDeletionsSizeEqualsEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.deletions(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(0);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(3);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingAdditionsSizeHigherThanEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.additions(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(2);
		assertThat(remainingEmailChanges.additions()).hasSize(1);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
		
		assertThat(fittingEmailChanges.additions())
			.doesNotContain(Iterables.toArray(remainingEmailChanges.additions(), Email.class));
	}

	@Test
	public void testSplittingChangesSizeHigherThanEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(0);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(2);
		assertThat(remainingEmailChanges.changes()).hasSize(1);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
		
		assertThat(fittingEmailChanges.changes())
			.doesNotContain(Iterables.toArray(remainingEmailChanges.changes(), Email.class));
	}

	@Test
	public void testSplittingDeletionsSizeHigherThanEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.deletions(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(0);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(2);
		assertThat(remainingEmailChanges.deletions()).hasSize(1);
		
		assertThat(fittingEmailChanges.deletions())
			.doesNotContain(Iterables.toArray(remainingEmailChanges.deletions(), Email.class));
	}

	@Test
	public void testSplittingSizeHigherThanAllEmailChanges() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(1).containsOnly(addition);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(1).containsOnly(change);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(1).containsOnly(deletion);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingSizeHigherEqualsAllEmailChanges() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(1).containsOnly(addition);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(1).containsOnly(change);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(1).containsOnly(deletion);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingSizeLowerThanAllEmailChangesFitsAdditionsFirst() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email addition2 = Email.builder().uid(4).read(true).date(date("2007-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition, addition2))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(2).containsOnly(addition, addition2);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(1).containsOnly(change);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(1).containsOnly(deletion);
	}

	@Test
	public void testSplittingSizeLowerThanAllEmailChangesFitsChangeSecondly() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email change2 = Email.builder().uid(5).read(true).date(date("2008-08-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change, change2))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(1).containsOnly(addition);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(2).containsOnly(change, change2);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(1).containsOnly(deletion);
	}

	@Test
	public void testSplittingSizeLowerThanAllEmailChangesFitsDeletionsThirdly() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email deletion2 = Email.builder().uid(5).read(true).date(date("2008-08-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion, deletion2))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(4);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(1).containsOnly(addition);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(1).containsOnly(change);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(2).containsOnly(deletion, deletion2);
		assertThat(remainingEmailChanges.deletions()).hasSize(0);
	}

	@Test
	public void testSplittingSizeSplitsAdditions() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email addition2 = Email.builder().uid(4).read(true).date(date("2007-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition, addition2))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(1);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(1);
		assertThat(remainingEmailChanges.additions()).hasSize(1);
		assertThat(fittingEmailChanges.changes()).hasSize(0);
		assertThat(remainingEmailChanges.changes()).hasSize(1).containsOnly(change);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(1).containsOnly(deletion);
		
		assertThat(fittingEmailChanges.additions())
			.doesNotContain(Iterables.toArray(remainingEmailChanges.additions(), Email.class));
	}

	@Test
	public void testSplittingSizeSplitsChanges() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email change2 = Email.builder().uid(4).read(true).date(date("2007-08-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change, change2))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(1).containsOnly(addition);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(1);
		assertThat(remainingEmailChanges.changes()).hasSize(1);
		assertThat(fittingEmailChanges.deletions()).hasSize(0);
		assertThat(remainingEmailChanges.deletions()).hasSize(1).containsOnly(deletion);
		
		assertThat(fittingEmailChanges.changes())
			.doesNotContain(Iterables.toArray(remainingEmailChanges.changes(), Email.class));
	}

	@Test
	public void testSplittingSizeSplitsDeletions() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email deletion2 = Email.builder().uid(4).read(true).date(date("2007-08-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion, deletion2))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		SplittedEmailChanges splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFittingEmailChanges();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getRemainingEmailChanges();

		assertThat(fittingEmailChanges.additions()).hasSize(1).containsOnly(addition);
		assertThat(remainingEmailChanges.additions()).hasSize(0);
		assertThat(fittingEmailChanges.changes()).hasSize(1).containsOnly(change);
		assertThat(remainingEmailChanges.changes()).hasSize(0);
		assertThat(fittingEmailChanges.deletions()).hasSize(1);
		assertThat(remainingEmailChanges.deletions()).hasSize(1);
		
		assertThat(fittingEmailChanges.deletions())
			.doesNotContain(Iterables.toArray(remainingEmailChanges.deletions(), Email.class));
	}
}

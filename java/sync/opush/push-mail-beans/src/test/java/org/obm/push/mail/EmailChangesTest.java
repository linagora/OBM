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
package org.obm.push.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.obm.push.mail.EmailChanges.Builder;
import org.obm.push.mail.EmailChanges.Splitter;
import org.obm.push.mail.bean.Email;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;


public class EmailChangesTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void buildWithNullDeletions() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("must not be null");
		
		EmailChanges.builder()
			.changes(ImmutableSet.<Email>of())
			.additions(ImmutableSet.<Email>of())
			.deletions(null)
			.build();
		
	}

	@Test
	public void buildWithNullChanges() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("must not be null");
		
		EmailChanges.builder()
			.deletions(ImmutableSet.<Email>of())
			.additions(ImmutableSet.<Email>of())
			.changes(null)
			.build();
	}
	
	@Test
	public void buildWithNullAdditions() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("must not be null");
		
		EmailChanges.builder()
			.changes(ImmutableSet.<Email>of())
			.deletions(ImmutableSet.<Email>of())
			.additions(null)
			.build();
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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(10);
		
		assertThat(splittedEmailChanges.getFit()).isEqualTo(EmailChanges.builder().build());
		assertThat(splittedEmailChanges.getLeft()).isEqualTo(EmailChanges.builder().build());
	}

	@Test
	public void testSplittingAdditionsSizeLowerThanEmailChanges() {
		Email email1 = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email email2 = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Email email3 = Email.builder().uid(4).read(true).date(date("2007-10-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.additions(ImmutableSet.<Email>of(email1, email2, email3))
			.build();
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(5);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

		assertThat(fittingEmailChanges).isEqualTo(
				EmailChanges.builder().change(change2).addition(addition).deletion(deletion).build());
		assertThat(remainingEmailChanges).isEqualTo(
				EmailChanges.builder().change(change).build());

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(4);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(1);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(2);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

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
		
		Splitter splittedEmailChanges = emailChanges.splitToFit(3);
		EmailChanges fittingEmailChanges = splittedEmailChanges.getFit();
		EmailChanges remainingEmailChanges = splittedEmailChanges.getLeft();

		assertThat(fittingEmailChanges).isEqualTo(
				EmailChanges.builder().addition(addition).deletion(deletion, deletion2).build());
		assertThat(remainingEmailChanges).isEqualTo(
				EmailChanges.builder().change(change).build());
	}
	
	@Test
	public void mergeWithEmpty() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email change2 = Email.builder().uid(4).read(true).date(date("2007-08-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Builder emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change, change2))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition));
		
		assertThat(emailChanges.merge(EmailChanges.empty()).build()).isEqualTo(emailChanges.build());
	}

	@Test
	public void mergeFromEmpty() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email change2 = Email.builder().uid(4).read(true).date(date("2007-08-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change, change2))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		Builder empty = EmailChanges.builder();
		
		assertThat(empty.merge(emailChanges).build()).isEqualTo(emailChanges);
	}
	
	@Test
	public void merge() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Builder emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition));
		
		Email change2 = Email.builder().uid(4).read(true).date(date("2007-12-13T21:39:45Z")).build();
		Email deletion2 = Email.builder().uid(5).read(true).date(date("2008-10-13T21:39:45Z")).build();
		Email addition2 = Email.builder().uid(6).read(true).date(date("2009-08-13T21:39:45Z")).build();
		EmailChanges emailChanges2 = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change2))
			.deletions(ImmutableSet.<Email>of(deletion2))
			.additions(ImmutableSet.<Email>of(addition2))
			.build();
		
		EmailChanges merged = emailChanges.merge(emailChanges2).build();
		
		assertThat(merged.changes()).containsOnly(change, change2);
		assertThat(merged.deletions()).containsOnly(deletion, deletion2);
		assertThat(merged.additions()).containsOnly(addition, addition2);
	}

	@Test
	public void mergeWithDuplicates() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Builder emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition));
		
		Email change2 = Email.builder().uid(4).read(true).date(date("2007-12-13T21:39:45Z")).build();
		EmailChanges emailChanges2 = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change2))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();
		
		EmailChanges merged = emailChanges.merge(emailChanges2).build();
		
		assertThat(merged.sumOfChanges()).isEqualTo(4);
		assertThat(merged.changes()).containsOnly(change, change2);
		assertThat(merged.deletions()).containsOnly(deletion);
		assertThat(merged.additions()).containsOnly(addition);
	}
	
	@Test
	public void sumOfChangesOnEmpty() {
		assertThat(EmailChanges.builder().sumOfChanges()).isEqualTo(0);
	}
	
	@Test
	public void sumOfChanges() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Builder emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition));
		
		assertThat(emailChanges.sumOfChanges()).isEqualTo(3);
	}
	
	@Test
	public void sumOfChangesWithDuplicates() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Builder emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.additions(ImmutableSet.<Email>of(addition));
		
		assertThat(emailChanges.sumOfChanges()).isEqualTo(3);
	}
	
	@Test
	public void sumOfChangesWithDuplicatesInMerge() {
		Email change = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		Email deletion = Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build();
		Email addition = Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build();
		Builder emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.merge(EmailChanges.builder()
				.changes(ImmutableSet.<Email>of(change))
				.deletions(ImmutableSet.<Email>of(deletion))
				.additions(ImmutableSet.<Email>of(addition))
				.build());
			
		assertThat(emailChanges.sumOfChanges()).isEqualTo(3);
	}

	@Test(expected=IllegalArgumentException.class)
	public void partitionNegative() {
		EmailChanges.empty().partition(-1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void partitionZero() {
		EmailChanges.empty().partition(0);
	}
	
	@Test
	public void partitionEmpty() {
		Iterable<EmailChanges> changes = EmailChanges.empty().partition(5);
			
		assertThat(changes).isEmpty();
	}
	
	@Test
	public void partitionLowerSizeThanWindowSizeAdditions() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build()
				.partition(5);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionLowerSizeThanWindowSizeChanges() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.change(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build()
				.partition(5);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.change(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionLowerSizeThanWindowSizeDeletions() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.deletion(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build()
				.partition(5);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.deletion(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionLowerSizeThanWindowSize() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.deletion(Email.builder().uid(1).build())
				.addition(Email.builder().uid(2).build())
				.change(Email.builder().uid(3).build())
				.build()
				.partition(5);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.deletion(Email.builder().uid(1).build())
				.addition(Email.builder().uid(2).build())
				.change(Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionSameSizeThanWindowSizeAdditions() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionSameSizeThanWindowSizeChanges() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.change(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.change(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionSameSizeThanWindowSizeDeletions() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.deletion(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.deletion(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionSameSizeThanWindowSize() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.deletion(Email.builder().uid(1).build())
				.addition(Email.builder().uid(2).build())
				.change(Email.builder().uid(3).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(EmailChanges.builder()
				.deletion(Email.builder().uid(1).build())
				.addition(Email.builder().uid(2).build())
				.change(Email.builder().uid(3).build())
				.build());
	}
	
	@Test
	public void partitionBiggerSizeThanWindowSizeAdditions() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build(),
					Email.builder().uid(4).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(
				EmailChanges.builder()
				.addition(
					Email.builder().uid(4).build(),
					Email.builder().uid(3).build(),
					Email.builder().uid(2).build())
				.build(),
				EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build())
				.build());
	}
	
	@Test
	public void partitionBiggerSizeThanWindowSizeChanges() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.change(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build(),
					Email.builder().uid(4).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(
				EmailChanges.builder()
				.change(
					Email.builder().uid(4).build(),
					Email.builder().uid(3).build(),
					Email.builder().uid(2).build())
				.build(),
				EmailChanges.builder()
				.change(
					Email.builder().uid(1).build())
				.build());
	}
	
	@Test
	public void partitionBiggerSizeThanWindowSizeDeletions() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.deletion(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(3).build(),
					Email.builder().uid(4).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(
				EmailChanges.builder()
				.deletion(
					Email.builder().uid(4).build(),
					Email.builder().uid(3).build(),
					Email.builder().uid(2).build())
				.build(),
				EmailChanges.builder()
				.deletion(
					Email.builder().uid(1).build())
				.build());
	}

	@Test
	public void partitionBiggerSizeThanWindowSize() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build())
				.change(Email.builder().uid(3).build())
				.deletion(Email.builder().uid(4).build())
				.build()
				.partition(3);
			
		assertThat(changes).containsOnly(
				EmailChanges.builder()
					.deletion(Email.builder().uid(4).build())
					.addition(Email.builder().uid(2).build())
					.change(Email.builder().uid(3).build())
					.build(),
				EmailChanges.builder()
					.addition(Email.builder().uid(1).build())
					.build());
	}

	@Test
	public void partitionReallyBiggerSizeThanWindowSize() {
		Iterable<EmailChanges> changes = EmailChanges.builder()
				.addition(
					Email.builder().uid(1).build(),
					Email.builder().uid(2).build(),
					Email.builder().uid(5).build(),
					Email.builder().uid(6).build(),
					Email.builder().uid(9).build())
				.change(
					Email.builder().uid(4).build(),
					Email.builder().uid(8).build(),
					Email.builder().uid(11).build(),
					Email.builder().uid(12).build(),
					Email.builder().uid(15).build())
				.deletion(
					Email.builder().uid(3).build(),
					Email.builder().uid(10).build(),
					Email.builder().uid(14).build(),
					Email.builder().uid(20).build())
				.build()
				.partition(2);
			
		assertThat(changes).containsOnly(
				EmailChanges.builder()
					.deletion(Email.builder().uid(20).build())
					.change(Email.builder().uid(15).build()).build(),
				EmailChanges.builder()
					.deletion(Email.builder().uid(14).build())
					.change(Email.builder().uid(12).build()).build(),
				EmailChanges.builder()
					.change(Email.builder().uid(11).build())
					.deletion(Email.builder().uid(10).build()).build(),
				EmailChanges.builder()
					.addition(Email.builder().uid(9).build())
					.change(Email.builder().uid(8).build()).build(),
				EmailChanges.builder()
					.addition(Email.builder().uid(6).build(), Email.builder().uid(5).build())
					.build(),
				EmailChanges.builder()
					.change(Email.builder().uid(4).build())
					.deletion(Email.builder().uid(3).build()).build(),
				EmailChanges.builder()
					.addition(Email.builder().uid(2).build(), Email.builder().uid(1).build())
					.build());
	}
}

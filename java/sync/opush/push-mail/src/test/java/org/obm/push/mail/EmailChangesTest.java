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
import org.obm.push.mail.bean.Email;

import com.google.common.collect.ImmutableSet;

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
		Email change = new Email(1, true, date("2004-12-13T21:39:45Z"));
		Email deletion = new Email(2, true, date("2005-10-13T21:39:45Z"));
		Email addition = new Email(3, true, date("2006-08-13T21:39:45Z"));
		EmailChanges emailChanges = EmailChanges.builder()
			.changes(ImmutableSet.<Email>of(change))
			.deletions(ImmutableSet.<Email>of(deletion))
			.additions(ImmutableSet.<Email>of(addition))
			.build();

		assertThat(emailChanges.changes()).containsOnly(change);
		assertThat(emailChanges.deletions()).containsOnly(deletion);
		assertThat(emailChanges.additions()).containsOnly(addition);
	}
}

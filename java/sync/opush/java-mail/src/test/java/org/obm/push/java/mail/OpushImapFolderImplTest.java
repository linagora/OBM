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
package org.obm.push.java.mail;

import java.util.Date;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.push.java.mail.OpushImapFolderImpl;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.utils.DateUtils;


import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class OpushImapFolderImplTest {

	private void assertNotDeletedTerm(SearchTerm searchTerm) {
		Assertions.assertThat(searchTerm).isInstanceOf(NotTerm.class);
		NotTerm not = (NotTerm) searchTerm;
		Assertions.assertThat(not.getTerm()).isInstanceOf(FlagTerm.class);
		FlagTerm flagTerm = (FlagTerm) not.getTerm();
		Assertions.assertThat(flagTerm.getFlags()).isEqualTo(new Flags(Flag.DELETED));
	}
	
	private void assertNotBeforeTerm(Date date, SearchTerm searchTerm) {
		Assertions.assertThat(searchTerm).isInstanceOf(NotTerm.class);
		NotTerm not = (NotTerm) searchTerm;
		SearchTerm before = not.getTerm();
		assertBeforeTerm(date, before);
	}
	
	private void assertBeforeTerm(Date date, SearchTerm searchTerm) {
		Assertions.assertThat(searchTerm).isInstanceOf(ReceivedDateTerm.class);
		ReceivedDateTerm receivedDateTerm = (ReceivedDateTerm) searchTerm;
		Assertions.assertThat(receivedDateTerm.getComparison()).isEqualTo(ComparisonTerm.LT);
		Assertions.assertThat(receivedDateTerm.getDate()).isEqualTo(date);
	}
	
	private SearchTerm[] assertAndTerm(SearchTerm searchTerm) {
		Assertions.assertThat(searchTerm).isInstanceOf(AndTerm.class);
		AndTerm and = (AndTerm) searchTerm;
		Assertions.assertThat(and.getTerms()).hasSize(2);
		return and.getTerms();
	}
	
	@Test
	public void matchAllToSearchTerm() {
		OpushImapFolderImpl folder = new OpushImapFolderImpl(null, null, null);
		SearchTerm searchTerm = folder.toSearchTerm(SearchQuery.MATCH_ALL);
		assertNotDeletedTerm(searchTerm);
	}

	@Test
	public void afterToSearchTerm() {
		OpushImapFolderImpl folder = new OpushImapFolderImpl(null, null, null);
		Date date = new Date();
		SearchQuery query = SearchQuery.builder().after(date).build();
		
		SearchTerm searchTerm = folder.toSearchTerm(query);
		
		SearchTerm[] andTerms = assertAndTerm(searchTerm);
		assertNotDeletedTerm(andTerms[0]);
		assertNotBeforeTerm(date, andTerms[1]);
	}

	@Test
	public void beforeToSearchTerm() {
		OpushImapFolderImpl folder = new OpushImapFolderImpl(null, null, null);
		Date date = new Date();
		SearchQuery query = SearchQuery.builder().before(date).build();
		
		SearchTerm searchTerm = folder.toSearchTerm(query);
		SearchTerm[] andTerms = assertAndTerm(searchTerm);
		assertNotDeletedTerm(andTerms[0]);
		assertBeforeTerm(date, andTerms[1]);
	}


	
	@Test
	public void beforeAndAfterToSearchTerm() {
		OpushImapFolderImpl folder = new OpushImapFolderImpl(null, null, null);
		Date now = DateUtils.getCurrentDate();
		Date tommorow = DateUtils.getOneDayLater(now);
		
		SearchQuery query = SearchQuery.builder().after(now).before(tommorow).build();
		
		SearchTerm searchTerm = folder.toSearchTerm(query);
		
		SearchTerm[] andTerms = assertAndTerm(searchTerm);
		assertNotDeletedTerm(andTerms[0]);
		SearchTerm[] dateTerms = assertAndTerm(andTerms[1]);
		assertBeforeTerm(tommorow, dateTerms[0]);
		assertNotBeforeTerm(now, dateTerms[1]);
		
	}

}

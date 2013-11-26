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

import java.util.Collection;
import java.util.Map;

import org.obm.push.mail.bean.Email;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EmailChangesComputerImpl implements EmailChangesComputer {
	
	private static class EmailKeyFunction implements Function<Email, Long> {
		@Override
		public Long apply(Email input) {
			return Long.valueOf(input.getUid());
		}
	}

	private Map<Long, Email> iterableToMap(Iterable<Email> before) {
		return Maps.<Long, Email> uniqueIndex(before, new EmailKeyFunction());
	}

	private static class EmailEquivalence extends Equivalence<Email> {
		@Override
		protected boolean doEquivalent(Email a, Email b) {
			return Objects.equal(a, b);
		}

		@Override
		protected int doHash(Email t) {
			return t.hashCode();
		}
	}
	
	private static class RightValueDifferenceFunction implements Function<ValueDifference<Email>, Email> {
		@Override
		public Email apply(ValueDifference<Email> input) {
			return input.rightValue();
		}
	}

	private Collection<Email> rightValueDifferences(Collection<ValueDifference<Email>> valueDifferences) {
		return Collections2.transform(valueDifferences, new RightValueDifferenceFunction());
	}
	
	@Override
	public EmailChanges computeChanges(Iterable<Email> before, Iterable<Email> actual) {
		MapDifference<Long, Email> difference = Maps.difference(iterableToMap(before), 
				iterableToMap(actual), 
				new EmailEquivalence());
		
		return EmailChanges.builder()
			.deletions(
					Sets.newHashSet(difference.entriesOnlyOnLeft().values()))
			.changes(
					Sets.newHashSet(
							rightValueDifferences(difference.entriesDiffering().values())))
			.additions(
					Sets.newHashSet(difference.entriesOnlyOnRight().values()))
			.build();
	}
}

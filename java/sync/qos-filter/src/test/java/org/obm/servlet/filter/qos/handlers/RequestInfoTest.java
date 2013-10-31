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
package org.obm.servlet.filter.qos.handlers;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.servlet.filter.qos.handlers.ContinuationIdStore.ContinuationId;

@RunWith(SlowFilterRunner.class)
public class RequestInfoTest
{
	@Test
	public void defaultValues() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		assertThat(info.getNumberOfRunningRequests()).isEqualTo(0);
		assertThat(info.getKey()).isEqualTo(2);
		assertThat(info.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
	}

	@Test
	public void copyHaveSameKey() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		RequestInfo<Integer> copy = info.oneMoreRequest();
		assertThat(copy.getKey()).isEqualTo(2);
	}
	
	@Test
	public void testOneMoreRequest() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		RequestInfo<Integer> copy = info.oneMoreRequest();
		assertThat(copy.getNumberOfRunningRequests()).isEqualTo(1);
		assertThat(info.getNumberOfRunningRequests()).isEqualTo(0);
	}

	@Test
	public void testRemoveOneRequest() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		RequestInfo<Integer> oneRequest = info.oneMoreRequest();
		RequestInfo<Integer> twoRequests = oneRequest.oneMoreRequest();
		RequestInfo<Integer> actual = twoRequests.removeOneRequest();
		assertThat(actual.getNumberOfRunningRequests()).isEqualTo(1);
		assertThat(info.getNumberOfRunningRequests()).isEqualTo(0);
	}
	
	@Test
	public void testAppendOneContinuation() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		ContinuationId continuationId = new ContinuationId(1l);
		RequestInfo<Integer> actual = info.appendContinuationId(continuationId);
		assertThat(actual.getContinuationIds()).containsOnly(continuationId);
		assertThat(info.getContinuationIds()).isEmpty();
	}
	
	@Test
	public void testAppendTwoContinuation() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		RequestInfo<Integer> actual = info
				.appendContinuationId(new ContinuationId(1l))
				.appendContinuationId(new ContinuationId(2l));
		assertThat(actual.getContinuationIds()).containsOnly(new ContinuationId(1l), new ContinuationId(2l));
		assertThat(info.getContinuationIds()).isEmpty();
	}
	
	@Test
	public void testPopOneRequest() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		RequestInfo<Integer> actual = info
				.appendContinuationId(new ContinuationId(1l))
				.appendContinuationId(new ContinuationId(2l))
				.popContinuation();
		assertThat(actual.getContinuationIds()).containsOnly(new ContinuationId(2l));
	}
	
	@Test
	public void testNextContinuation() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		ContinuationId actual = info
				.appendContinuationId(new ContinuationId(1l))
				.appendContinuationId(new ContinuationId(2l))
				.nextContinuation();
		assertThat(actual).isEqualTo(new ContinuationId(1l));
	}
	
	@Test
	public void testNextContinuationIsSameAsPop() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		RequestInfo<Integer> twoContinuations = info
				.appendContinuationId(new ContinuationId(1l))
				.appendContinuationId(new ContinuationId(2l));
		assertThat(twoContinuations.nextContinuation()).isEqualTo(new ContinuationId(1l));
		assertThat(twoContinuations.popContinuation().getContinuationIds()).containsOnly(new ContinuationId(2l));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTooManyRequests() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		info.appendContinuationId(new ContinuationId(1l))
			.popContinuation()
			.popContinuation();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testNegativeRequest() {
		RequestInfo<Integer> info = RequestInfo.create(2);
		info.removeOneRequest();
	}
	
}

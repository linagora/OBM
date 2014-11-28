/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.continuation.Continuation;
import org.junit.Test;
import org.obm.servlet.filter.qos.QoSContinuationSupport;
import org.obm.servlet.filter.qos.QoSContinuationSupport.QoSContinuation;

import com.google.common.base.Objects;


public class KeyRequestsInfoTest {
	
	public static class TestContinuation implements QoSContinuationSupport.QoSContinuation {
		
		private long id;

		public TestContinuation(long id) {
			this.id = id;
		}
		
		@Override
		public Continuation getContinuation() {
			throw new IllegalStateException();
		}
		
		@Override
		public long id() {
			return id;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}
		
		@Override
		public boolean equals(Object obj) {
			return id == ((TestContinuation)obj).id;
		}
	}
	
	@Test
	public void defaultValues() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		assertThat(info.getNumberOfRunningRequests()).isEqualTo(0);
		assertThat(info.getKey()).isEqualTo(2);
		assertThat(info.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
	}

	@Test
	public void copyHaveSameKey() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		KeyRequestsInfo<Integer> copy = info.oneMoreRequest();
		assertThat(copy.getKey()).isEqualTo(2);
	}
	
	@Test
	public void testOneMoreRequest() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		KeyRequestsInfo<Integer> copy = info.oneMoreRequest();
		assertThat(copy.getNumberOfRunningRequests()).isEqualTo(1);
		assertThat(info.getNumberOfRunningRequests()).isEqualTo(0);
	}

	@Test
	public void testRemoveOneRequest() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		KeyRequestsInfo<Integer> oneRequest = info.oneMoreRequest();
		KeyRequestsInfo<Integer> twoRequests = oneRequest.oneMoreRequest();
		KeyRequestsInfo<Integer> actual = twoRequests.removeOneRequest();
		assertThat(actual.getNumberOfRunningRequests()).isEqualTo(1);
		assertThat(info.getNumberOfRunningRequests()).isEqualTo(0);
	}
	
	@Test
	public void testAppendOneContinuation() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		TestContinuation continuationId = new TestContinuation(1l);
		KeyRequestsInfo<Integer> actual = info.appendContinuation(continuationId);
		assertThat(actual.getContinuationIds()).containsOnly(continuationId);
		assertThat(info.getContinuationIds()).isEmpty();
	}
	
	@Test
	public void testAppendTwoContinuation() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		KeyRequestsInfo<Integer> actual = info
				.appendContinuation(new TestContinuation(1l))
				.appendContinuation(new TestContinuation(2l));
		assertThat(actual.getContinuationIds()).containsOnly(new TestContinuation(1l), new TestContinuation(2l));
		assertThat(info.getContinuationIds()).isEmpty();
	}
	
	@Test
	public void testPopOneRequest() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		KeyRequestsInfo<Integer> actual = info
				.appendContinuation(new TestContinuation(1l))
				.appendContinuation(new TestContinuation(2l))
				.popContinuation();
		assertThat(actual.getContinuationIds()).containsOnly(new TestContinuation(2l));
	}
	
	@Test
	public void testNextContinuation() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		QoSContinuation actual = info
				.appendContinuation(new TestContinuation(1l))
				.appendContinuation(new TestContinuation(2l))
				.nextContinuation();
		assertThat(actual).isEqualTo(new TestContinuation(1l));
	}
	
	@Test
	public void testNextContinuationIsSameAsPop() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		KeyRequestsInfo<Integer> twoContinuations = info
				.appendContinuation(new TestContinuation(1l))
				.appendContinuation(new TestContinuation(2l));
		assertThat(twoContinuations.nextContinuation()).isEqualTo(new TestContinuation(1l));
		assertThat(twoContinuations.popContinuation().getContinuationIds()).containsOnly(new TestContinuation(2l));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testTooManyRequests() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		info.appendContinuation(new TestContinuation(1l))
			.popContinuation()
			.popContinuation();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testNegativeRequest() {
		KeyRequestsInfo<Integer> info = KeyRequestsInfo.create(2);
		info.removeOneRequest();
	}
	
	@Test
	public void testGetRequestCountWhenNone() {
		KeyRequestsInfo<String> info = KeyRequestsInfo.create("key");
		assertThat(info.getPendingRequestCount()).isEqualTo(0);
	}
	
	@Test
	public void testGetRequestCountOnlyRunning() {
		KeyRequestsInfo<String> info = KeyRequestsInfo.create("key");
		KeyRequestsInfo<String> twoRunnings = info
				.oneMoreRequest()
				.oneMoreRequest();
		assertThat(twoRunnings.getPendingRequestCount()).isEqualTo(2);
	}
	
	@Test
	public void testGetRequestCountOnlyContinuation() {
		KeyRequestsInfo<String> info = KeyRequestsInfo.create("key");
		KeyRequestsInfo<String> twoContinuations = info
				.appendContinuation(new TestContinuation(1l))
				.appendContinuation(new TestContinuation(2l));
		assertThat(twoContinuations.getPendingRequestCount()).isEqualTo(2);
	}
	
	@Test
	public void testGetRequestCountBoth() {
		KeyRequestsInfo<String> info = KeyRequestsInfo.create("key");
		KeyRequestsInfo<String> twoContinuations = info
				.oneMoreRequest()
				.appendContinuation(new TestContinuation(1l))
				.appendContinuation(new TestContinuation(2l))
				.oneMoreRequest();
		assertThat(twoContinuations.getPendingRequestCount()).isEqualTo(4);
	}
}

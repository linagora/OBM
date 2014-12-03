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

package org.obm.push.mail.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;


public class MessageSetTest {

	@Test
	public void emptyRangeBuilder() {
		MessageSet actual = MessageSet.builder().build();
		assertThat(actual.asDiscreteValues()).isEmpty();
	}
	
	@Test
	public void singletonRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(1l).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l);
	}
	
	@Test
	public void duplicateSingletonRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(1l).add(1l).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l);
	}
	
	@Test
	public void discreteValuesRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(1l).add(3l).add(5l).build();
		assertThat(actual.rangeNumber()).isEqualTo(3);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 3l, 5l);
	}
	
	@Test
	public void discreteContiguousValuesRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(1l).add(2l).add(3l).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l);
	}
	
	@Test
	public void simpleRangeRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(Range.closed(1l, 5l)).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l);
	}
	
	@Test
	public void duplicateRangeRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(Range.closed(1l, 5l)).add(Range.closed(1l, 5l)).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l);
	}
	
	@Test
	public void spanningRangeRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(Range.closed(1l, 5l)).add(Range.closed(4l, 7l)).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l, 6l, 7l);
	}
	
	@Test
	public void spanningRangeRangeBuilder2() {
		MessageSet actual = MessageSet.builder().add(Range.closed(2l, 5l)).add(Range.closed(1l, 4l)).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l);
	}
	
	@Test
	public void notIntersectingRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(Range.closed(1l, 5l)).add(Range.closed(10l, 12l)).build();
		assertThat(actual.rangeNumber()).isEqualTo(2);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l, 10l, 11l, 12l);
	}
	
	@Test
	public void connectingRangeRangeBuilder() {
		MessageSet actual = MessageSet.builder().add(Range.closed(1l, 5l)).add(Range.closed(7l, 8l)).add(Range.closed(6l, 7l)).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l);
	}
	
	@Test
	public void messageSetMergingRangeBuilder() {
		MessageSet toMerge = MessageSet.builder().add(Range.closed(6l, 7l)).add(Range.closed(9l, 10l)).build();
		MessageSet actual = MessageSet.builder().add(Range.closed(1l, 5l)).add(Range.closed(7l, 8l)).add(toMerge).build();
		assertThat(actual.rangeNumber()).isEqualTo(1);
		assertThat(actual.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l, 10l);
	}
	
	@Test
	public void messageSetBuilderFromExistingMessageSet() {
		MessageSet firstSet = MessageSet.builder().add(Range.closed(1l, 5l)).add(Range.closed(7l, 8l)).build();
		MessageSet secondSet = MessageSet.from(firstSet).add(6l).build();
		assertThat(firstSet.rangeNumber()).isEqualTo(2);
		assertThat(secondSet.rangeNumber()).isEqualTo(1);
		assertThat(firstSet.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l, 7l, 8l);
		assertThat(secondSet.asDiscreteValues()).containsExactly(1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l);
	}
	
	@Test
	public void messageSetIsEmptyBuiltWithNothing() {
		MessageSet set = MessageSet.builder().build();
		assertThat(set.isEmpty()).isTrue();
	}
	
	@Test
	public void messageSetIsEmptyBuiltWithEmpty() {
		MessageSet set = MessageSet.empty();
		assertThat(set.isEmpty()).isTrue();
	}
	
	@Test
	public void messageSetIsEmptyBuiltWithSingleton() {
		MessageSet set = MessageSet.singleton(1l);
		assertThat(set.isEmpty()).isFalse();
	}
	
	@Test
	public void messageSetIsEmptyBuiltWithAdd() {
		MessageSet set = MessageSet.builder().add(1l).build();
		assertThat(set.isEmpty()).isFalse();
	}
	
	@Test
	public void emptyMessageSetContainsNothing() {
		MessageSet empty = MessageSet.empty();
		assertThat(empty.contains(123)).isFalse();
	}
	
	@Test
	public void singletonMessageSetContainsItsOnlyElement() {
		MessageSet singleton = MessageSet.singleton(1);
		assertThat(singleton.contains(1)).isTrue();
		assertThat(singleton.contains(2)).isFalse();
	}
	
	@Test
	public void complexSetContainsMessageSetContainsMatchingElements() {
		MessageSet complex = MessageSet.builder().add(Range.closed(1l, 3l)).add(Range.closed(10l, 20l)).build();
		assertThat(complex.contains(1)).isTrue();
		assertThat(complex.contains(2)).isTrue();
		assertThat(complex.contains(3)).isTrue();
		assertThat(complex.contains(4)).isFalse();
		assertThat(complex.contains(7)).isFalse();
		assertThat(complex.contains(10)).isTrue();
		assertThat(complex.contains(11)).isTrue();
		assertThat(complex.contains(20)).isTrue();
		assertThat(complex.contains(21)).isFalse();
		assertThat(complex.contains(Integer.MAX_VALUE + 2)).isFalse();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void partitionShouldProvideExpectedSizedLists() {
		MessageSet messageSet = MessageSet.builder().add(1l).add(2l).add(3l).add(5l).build();
		List<Long> part1 = ImmutableList.of(1l, 2l);
		List<Long> part2 = ImmutableList.of(3l, 5l);
		
		Iterable<List<Long>> partition = messageSet.partition(2);
		assertThat(partition).hasSize(2);
		assertThat(partition).containsExactly(part1, part2);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void partitionShouldProvideRemainingInLastList() {
		MessageSet messageSet = MessageSet.builder().add(1l).add(2l).add(3l).add(5l).add(6l).build();
		List<Long> part1 = ImmutableList.of(1l, 2l, 3l);
		List<Long> part2 = ImmutableList.of(5l, 6l);
		
		Iterable<List<Long>> partition = messageSet.partition(3);
		assertThat(partition).hasSize(2);
		assertThat(partition).containsExactly(part1, part2);
	}
	
	@Test
	public void partitionShouldProvideEmptyWhenNoValues() {
		MessageSet messageSet = MessageSet.builder().build();
		
		Iterable<List<Long>> partition = messageSet.partition(2);
		assertThat(partition).isEmpty();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void partitionShouldThrowWhenBadSize() {
		MessageSet messageSet = MessageSet.builder().add(1l).add(2l).add(3l).add(5l).build();
		
		messageSet.partition(-2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void partitionShouldThrowWhenZeroSize() {
		MessageSet messageSet = MessageSet.builder().add(1l).add(2l).add(3l).add(5l).build();
		
		messageSet.partition(0);
	}
	
	@Test
	public void maxShouldReturnTheMax() {
		MessageSet messageSet = MessageSet.builder().add(1l).add(2l).add(3l).add(5l).build();
		long max = messageSet.max();
		assertThat(max).isEqualTo(5);
	}
	
	@Test(expected=NoSuchElementException.class)
	public void maxShouldThrowWhenEmpty() {
		MessageSet messageSet = MessageSet.builder().build();
		long max = messageSet.max();
		assertThat(max).isEqualTo(0);
	}
	
	@Test
	public void firstShouldReturnAbsentWhenNone() {
		MessageSet messageSet = MessageSet.builder().build();
		Optional<Long> first = messageSet.first();
		assertThat(first).isAbsent();
	}
	
	@Test
	public void firstShouldReturnFirst() {
		MessageSet messageSet = MessageSet.builder().add(1l).add(2l).add(3l).add(5l).build();
		Optional<Long> first = messageSet.first();
		assertThat(first).isPresent();
		assertThat(first.get()).isEqualTo(1);
	}
	
	@Test
	public void firstShouldReturnFirstOrdered() {
		MessageSet messageSet = MessageSet.builder().add(2l).add(3l).add(1l).add(5l).build();
		Optional<Long> first = messageSet.first();
		assertThat(first).isPresent();
		assertThat(first.get()).isEqualTo(1);
	}
	
	@Test
	public void removeEmpty() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.empty());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 12l, 13l, 14l, 15l, 16l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeASingleton() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(15l).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 12l, 13l, 14l, 16l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeASingletonLower() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(1l).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 12l, 13l, 14l, 15l, 16l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeASingletonHigher() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(30l).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 12l, 13l, 14l, 15l, 16l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeARange() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.closed(14l, 16l)).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 12l, 13l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeARangeWithSameUpperEndpoint() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.closed(14l, 20l)).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 12l, 13l);
	}
	
	@Test
	public void removeARangeWithLowerEndpointLower() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.closed(8l, 12l)).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(13l, 14l, 15l, 16l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeARangeWithUpperEndpointUpper() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.closed(18l, 24l)).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 12l, 13l, 14l, 15l, 16l, 17l);
	}
	
	@Test
	public void removeTwoSingletons() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.singleton(11l)).add(Range.singleton(13l)).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 12l, 14l, 15l, 16l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeMixed() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.singleton(11l)).add(Range.closed(13l, 15l)).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 12l, 16l, 17l, 18l, 19l, 20l);
	}
	
	@Test
	public void removeTwoRanges() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.closed(12l, 14l)).add(Range.closed(16l, 18l)).build());
		assertThat(messageSet.asDiscreteValues()).containsExactly(10l, 11l, 15l, 19l, 20l);
	}
	
	@Test
	public void removeAll() {
		MessageSet origin = MessageSet.builder()
				.add(Range.closed(10l, 20l))
				.build();
		
		MessageSet messageSet = origin.remove(MessageSet.builder().add(Range.closed(8l, 30l)).build());
		assertThat(messageSet.asDiscreteValues()).isEmpty();
	}
}

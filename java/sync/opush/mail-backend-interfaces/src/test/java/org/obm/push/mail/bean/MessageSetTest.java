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

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

import com.google.common.collect.Range;

@RunWith(SlowFilterRunner.class)
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
}

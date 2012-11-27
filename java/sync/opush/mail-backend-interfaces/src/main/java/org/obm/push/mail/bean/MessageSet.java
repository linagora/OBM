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

package org.obm.push.mail.bean;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

public class MessageSet {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder from(MessageSet set) {
		return new Builder(set);
	}

	public static MessageSet parseMessageSet(String set) {
		String[] parts = set.split(",");
		Builder builder = MessageSet.builder();
		for (String s : parts) {
			if (!s.contains(":")) {
				builder.add(Long.valueOf(s));
			} else {
				String[] p = s.split(":");
				long start = Long.valueOf(p[0]);
				long end = Long.valueOf(p[1]);
				builder.add(Ranges.closed(start, end));
			}
		}
		return builder.build();
	}

	public static class Builder implements org.obm.push.bean.Builder<MessageSet> {

		private final class LowerEndpointComparator implements Comparator<Range<Long>> {
			@Override
			public int compare(Range<Long> o1, Range<Long> o2) {
				long distance = o1.lowerEndpoint() - o2.lowerEndpoint();
				if (distance == 0) {
					return 0;
				} else {
					return distance > 0 ? 1 : -1;
				}
			}
		}

		private SortedSet<Range<Long>> ranges;

		private Builder() {
			ranges = Sets.newTreeSet(new LowerEndpointComparator());
		}
		
		public Builder(MessageSet set) {
			this();
			ranges.addAll(set.ranges);
		}

		public Builder add(long value) {
			return add(Ranges.singleton(value));
		}
		
		public Builder addAll(Collection<Long> values) {
			for (Long value : values) {
				add(value);
			}
			return this;
		}
		
		public Builder add(Range<Long> value) {
			Optional<Range<Long>> connectedRange = findRangeConnecterOrContiguousTo(value);
			if (connectedRange.isPresent()) {
				replaceWithSpanningRange(connectedRange.get(), value);
			} else {
				ranges.add(value);
			}
			return this;
		}
		
		public Builder add(MessageSet set) {
			for (Range<Long> range: set.ranges) {
				add(range);
			}
			return this;
		}
		
		private void replaceWithSpanningRange(Range<Long> connectedRange, Range<Long> other) {
			ranges.remove(connectedRange);
			add(connectedRange.span(other));
		}

		private Optional<Range<Long>> findRangeConnecterOrContiguousTo(Range<Long> other) {
			for (Range<Long> range: ranges) {
				if (range.isConnected(other) ||isContiguous(other, range)) {
					return Optional.of(range);
				}
			}
			return Optional.absent();
		}

		private boolean isContiguous(Range<Long> other, Range<Long> range) {
			return Math.abs(range.upperEndpoint() - other.lowerEndpoint()) == 1
					|| Math.abs(range.lowerEndpoint() - other.upperEndpoint()) == 1;
		}
		
		@Override
		public MessageSet build() {
			return new MessageSet(ranges);
		}
	}

	private final Set<Range<Long>> ranges;
	
	private MessageSet(Set<Range<Long>> ranges) {
		this.ranges = ranges;
	}
	
	public Set<Range<Long>> getRanges() {
		return ranges;
	}
	
	public Iterable<Long> asDiscreteValues() {
		return Iterables.concat(Iterables.transform(ranges, new Function<Range<Long>, Set<Long>>() {
			@Override
			public Set<Long> apply(Range<Long> input) {
				return input.asSet(DiscreteDomains.longs());
			}
		}));
	}
	
	public int rangeNumber() {
		return ranges.size();
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(ranges);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MessageSet) {
			MessageSet that = (MessageSet) object;
			return Objects.equal(this.ranges, that.ranges);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("ranges", ranges)
			.toString();
	}
}

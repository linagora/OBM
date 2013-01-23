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

package org.obm.push.minig.imap.impl;

import java.util.Collection;
import java.util.List;

import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.MessageSet.Builder;

import com.google.common.base.Joiner;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

public class ImapMessageSet {
	
	private final MessageSet set;

	public static ImapMessageSet parseMessageSet(String set) {
		String[] parts = set.split(",");
		Builder builder = MessageSet.builder();
		for (String s : parts) {
			if (!s.contains(":")) {
				builder.add(Long.valueOf(s));
			} else {
				String[] p = s.split(":");
				long start = Long.valueOf(p[0]);
				long end = Long.valueOf(p[1]);
				builder.add(Range.closed(start, end));
			}
		}
		return wrap(builder.build());
	}
	
	public static ImapMessageSet wrap(MessageSet set) {
		return new ImapMessageSet(set);
	}

	private ImapMessageSet(MessageSet set) {
		this.set = set;
	}
	
	public MessageSet getMessageSet() {
		return set;
	}
	
	private String singleValueRangeAsString(ContiguousSet<Long> rangeAsSet) {
		return String.valueOf(rangeAsSet.first());
	}
	
	private String intervalRangeAsString(ContiguousSet<Long> rangeAsSet) {
		return String.format("%d:%d", rangeAsSet.first(), rangeAsSet.last());
	}
	
	private String rangeAsString(Range<Long> range) {
		ContiguousSet<Long> rangeAsSet = ContiguousSet.create(range, DiscreteDomain.longs());
		if (rangeAsSet.size() == 1) {
			return singleValueRangeAsString(rangeAsSet);
		} else {
			return intervalRangeAsString(rangeAsSet);
		}
	}
	
	public String asString() {
		List<String> rangesAsStrings = Lists.newArrayList();
		for (Range<Long> range: set.getRanges()) {
			if (!range.isEmpty()) {
				rangesAsStrings.add(rangeAsString(range));
			}
		}
		return Joiner.on(',').join(rangesAsStrings);
	}
	
	public Collection<Long> asLongCollection() {
		return ImmutableList.copyOf(set.asDiscreteValues());
	}

	public int size() {
		return Iterators.size(set.asDiscreteValues().iterator());
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean contains(long value) {
		return Iterators.contains(set.asDiscreteValues().iterator(), value); 
	}
}

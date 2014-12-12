/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.beans;

import java.util.Iterator;
import java.util.Map;

import org.obm.push.mail.bean.MessageSet;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class MappedMessageSets {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private MessageSet origin;
		private MessageSet destination;
		
		private Builder() {
		}
		
		public Builder origin(MessageSet origin) {
			Preconditions.checkNotNull(origin);
			this.origin = origin;
			return this;
		}
		
		public Builder destination(MessageSet destination) {
			Preconditions.checkNotNull(destination);
			this.destination = destination;
			return this;
		}
		
		public MappedMessageSets build() {
			Preconditions.checkState(origin != null);
			Preconditions.checkState(destination != null);
			return new MappedMessageSets(origin, destination, map(destination, origin));
		}

		private Map<Long, Long> map(MessageSet destination, MessageSet origin) {
			Preconditions.checkState(origin.size() == destination.size());
			Iterable<Long> originDiscreteValues = origin.asDiscreteValues();
			Iterator<Long> destinationValuesIterator = destination.iterator();
			
			ImmutableMap.Builder<Long, Long> originUidsToDestinationUids = ImmutableMap.builder();
			for (Long originValue : originDiscreteValues) {
				originUidsToDestinationUids.put(originValue, destinationValuesIterator.next());
			}
			
			return originUidsToDestinationUids.build();
		}
	}

	private final MessageSet origin;
	private final MessageSet destination;
	private final Map<Long, Long> originUidsToDestinationUids;

	public MappedMessageSets(MessageSet origin, MessageSet destination, Map<Long, Long> originUidsToDestinationUids) {
		this.origin = origin;
		this.destination = destination;
		this.originUidsToDestinationUids = originUidsToDestinationUids;
	}
	
	public MessageSet getOrigin() {
		return origin;
	}
	
	public MessageSet getDestination() {
		return destination;
	}
	
	public long getDestinationUidFor(long originUid) {
		return originUidsToDestinationUids.get(originUid);
	}
	
	public MessageSet getDestinationUidFor(MessageSet originUids) {
		MessageSet.Builder builder = MessageSet.builder();
		for (long originUid : originUids.asDiscreteValues()) {
			builder.add(getDestinationUidFor(originUid));
		}
		return builder.build();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(origin, destination);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof MappedMessageSets) {
			MappedMessageSets that = (MappedMessageSets) object;
			return Objects.equal(this.origin, that.origin)
				&& Objects.equal(this.destination, that.destination);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("origin", origin)
			.add("destination", destination)
			.toString();
	}
}

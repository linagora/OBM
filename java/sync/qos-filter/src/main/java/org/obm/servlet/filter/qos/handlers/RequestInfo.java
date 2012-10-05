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

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Holds information about a request.<br />
 * The information stored is:
 * <ul>
 * <li>The timestamp of the object creation.</li>
 * <li>The number of requests since.</li>
 * </ul>
 */
public class RequestInfo<K extends Serializable> implements Serializable {
	
	public static <K extends Serializable> RequestInfo<K> create(K key) {
		return new RequestInfo<K>(key);
	}
	
	private long timestamp;
	private int numberOfRunningRequests;
	private final K key;

	/**
	 * Builds a new {@link RequestInfo}.<br />
	 * Use the factory method {@link #create()} to instantiate.
	 */
	private RequestInfo(K key, int numberOfRunningRequests) {
		this.key = key;
		this.timestamp = System.currentTimeMillis();
		this.numberOfRunningRequests = numberOfRunningRequests;
	}
	
	private RequestInfo(K key) {
		this(key, 0);
	}

	/**
	 * @return a copy of this {@link RequestInfo} adding one to the pending requests number  
	 */
	public RequestInfo<K> oneMoreRequest() {
		return new RequestInfo<K>(key, numberOfRunningRequests + 1);
	}

	/**
	 * @return a copy of this {@link RequestInfo} removing one to the pending requests number  
	 */
	public RequestInfo<K> removeOneRequest() {
		Preconditions.checkState(numberOfRunningRequests > 0);
		return new RequestInfo<K>(key, numberOfRunningRequests - 1);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getNumberOfRunningRequests() {
		return numberOfRunningRequests;
	}

	public K getKey() {
		return key;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(numberOfRunningRequests, key);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof RequestInfo) {
			@SuppressWarnings("unchecked")
			RequestInfo<K> that = (RequestInfo<K>) object;
			return Objects.equal(this.numberOfRunningRequests, that.numberOfRunningRequests)
				&& Objects.equal(this.key, that.key);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("key", key)
			.add("numberOfRunningRequests", numberOfRunningRequests)
			.toString();
	}
	

}

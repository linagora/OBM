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
package org.obm.push.backend;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.UserDataRequest;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class CollectionChangeListener implements
		ICollectionChangeListener {

	private final UserDataRequest udr;
	private final Set<AnalysedSyncCollection> monitoredCollections;
	private final IContinuation continuation;

	public CollectionChangeListener(UserDataRequest udr,
			IContinuation c, 
			Set<AnalysedSyncCollection> monitoredCollections) {
		this.udr = udr;
		this.monitoredCollections = monitoredCollections;
		this.continuation = c;
	}

	@Override
	public IContinuation getContinuation() {
		return continuation;
	}

	@Override
	public Set<AnalysedSyncCollection> getMonitoredCollections() {
		return monitoredCollections;
	}

	@Override
	public UserDataRequest getSession() {
		return udr;
	}
	
	public void changesDetected() {
		continuation.resume();
	}

	@Override
	public boolean monitorOneOf(ChangedCollections changedCollections) {
		SortedSet<String> collectionPathSet = convertSetToComparePath(changedCollections);
		return !Sets.intersection(
				Sets.newHashSet(Iterables.transform(getMonitoredCollections(), new Function<AnalysedSyncCollection, String>() {
					@Override
					public String apply(AnalysedSyncCollection input) {
						return input.getCollectionPath();
					}
				}))
				, collectionPathSet).isEmpty();
	}

	private SortedSet<String> convertSetToComparePath(ChangedCollections changedCollections) {
		
		SortedSet<String> collectionPathSet = Sets.newTreeSet(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return ComparisonChain.start()
						.compare(o1, o2)
						.result();
			}
		});
		collectionPathSet.addAll(changedCollections.getChanges());
		return collectionPathSet;
	}
	
}

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
package org.obm.push.backend;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.SyncCollection;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;

public class CollectionChangeListener implements
		ICollectionChangeListener {

	private BackendSession bs;
	private Set<SyncCollection> monitoredCollections;
	private IContinuation continuation;

	public CollectionChangeListener(BackendSession bs,
			IContinuation c, Set<SyncCollection> monitoredCollections) {
		this.bs = bs;
		this.monitoredCollections = monitoredCollections;
		this.continuation = c;
	}

	@Override
	public IContinuation getContinuation() {
		return continuation;
	}

	@Override
	public Set<SyncCollection> getMonitoredCollections() {
		return monitoredCollections;
	}

	@Override
	public BackendSession getSession() {
		return bs;
	}
	
	public void changesDetected() {
		continuation.resume();
	}

	@Override
	public boolean monitorOneOf(ChangedCollections changedCollections) {
		TreeSet<SyncCollection> collectionPathSet = convertSetToComparePath(changedCollections);
		return !Sets.intersection(getMonitoredCollections(), collectionPathSet).isEmpty();
	}

	private TreeSet<SyncCollection> convertSetToComparePath(ChangedCollections changedCollections) {
		
		TreeSet<SyncCollection> collectionPathSet = Sets.newTreeSet(new Comparator<SyncCollection>() {

			@Override
			public int compare(SyncCollection o1, SyncCollection o2) {
				return ComparisonChain.start()
						.compare(o1.getCollectionPath(), o2.getCollectionPath())
						.result();
			}
		});
		collectionPathSet.addAll(changedCollections.getChanges());
		return collectionPathSet;
	}
	
}

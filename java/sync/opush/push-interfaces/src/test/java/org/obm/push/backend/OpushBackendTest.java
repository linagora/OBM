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

import static org.easymock.EasyMock.createControl;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.backend.CollectionPath.Builder;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.service.impl.MappingService;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;


public class OpushBackendTest {

	private IMocksControl mocks;
	private MappingService mappingService;
	private Provider<Builder> collectionPathBuilderProvider;
	private OpushBackend opushBackend;

	@Before
	public void setUp() {
		mocks = createControl();
		mappingService = mocks.createMock(MappingService.class); 
		collectionPathBuilderProvider = mocks.createMock(Provider.class);
		opushBackend = new OpushBackend(mappingService, collectionPathBuilderProvider) {

			@Override
			protected CollectionChange createCollectionChange(UserDataRequest udr, OpushCollection collection)
					throws DaoException, CollectionNotFoundException {
				throw new NotImplementedException();
			}

			@Override
			protected CollectionDeletion createCollectionDeletion(UserDataRequest udr, CollectionPath collectionPath)
					throws DaoException, CollectionNotFoundException {
				throw new NotImplementedException();
			}
			
		};
	}
	
	@Test
	public void testOneAddedCollectionWhenNoLastKnown() {
		Set<CollectionPath> lastKnownCollections = ImmutableSet.of();
		
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.EMAIL, "name"))
				.displayName("display name")
				.build();
		PathsToCollections changedCollections = PathsToCollections.builder()
				.put(collection.collectionPath(), collection)
				.build();

		mocks.replay();
		Iterable<OpushCollection> addedCollections = opushBackend.addedCollections(lastKnownCollections, changedCollections);
		mocks.verify();
		
		assertThat(addedCollections).containsOnly(collection);
	}
	
	@Test
	public void testTwoAddedCollectionWhenNoLastKnown() {
		Set<CollectionPath> lastKnownCollections = ImmutableSet.of();
		
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.EMAIL, "name"))
				.displayName("display name")
				.build();
		OpushCollection collection2 = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.EMAIL, "name 2"))
				.displayName("display name 2")
				.build();
		PathsToCollections changedCollections = PathsToCollections.builder()
				.put(collection.collectionPath(), collection)
				.put(collection2.collectionPath(), collection2)
				.build();

		mocks.replay();
		Iterable<OpushCollection> addedCollections = opushBackend.addedCollections(lastKnownCollections, changedCollections);
		mocks.verify();
		
		assertThat(addedCollections).containsOnly(collection, collection2);
	}
	
	@Test
	public void testTwoDifferentTypeAddedWithSameNameWhenNoLastKnown() {
		Set<CollectionPath> lastKnownCollections = ImmutableSet.of();
		
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.EMAIL, "name"))
				.displayName("display name")
				.build();
		OpushCollection collection2 = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.CONTACTS, "name"))
				.displayName("display name")
				.build();
		PathsToCollections changedCollections = PathsToCollections.builder()
				.put(collection.collectionPath(), collection)
				.put(collection2.collectionPath(), collection2)
				.build();

		mocks.replay();
		Iterable<OpushCollection> addedCollections = opushBackend.addedCollections(lastKnownCollections, changedCollections);
		mocks.verify();
		
		assertThat(addedCollections).containsOnly(collection, collection2);
	}
	
	@Test
	public void testOneAddedCollectionWhenSameInLastKnown() {
		Set<CollectionPath> lastKnownCollections = ImmutableSet.<CollectionPath>of(
				new CollectionPathTest(PIMDataType.EMAIL, "name"));
		
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.EMAIL, "name"))
				.displayName("display name")
				.build();
		PathsToCollections changedCollections = PathsToCollections.builder()
				.put(collection.collectionPath(), collection)
				.build();

		mocks.replay();
		Iterable<OpushCollection> addedCollections = opushBackend.addedCollections(lastKnownCollections, changedCollections);
		mocks.verify();
		
		assertThat(addedCollections).isEmpty();
	}
	
	@Test
	public void testTwoAddedCollectionWhenOneSameInLastKnown() {
		Set<CollectionPath> lastKnownCollections = ImmutableSet.<CollectionPath>of(
				new CollectionPathTest(PIMDataType.EMAIL, "name"));
		
		OpushCollection collection = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.EMAIL, "name"))
				.displayName("display name")
				.build();
		OpushCollection collection2 = OpushCollection.builder()
				.collectionPath(new CollectionPathTest(PIMDataType.CONTACTS, "name"))
				.displayName("display name")
				.build();
		PathsToCollections changedCollections = PathsToCollections.builder()
				.put(collection.collectionPath(), collection)
				.put(collection2.collectionPath(), collection2)
				.build();

		mocks.replay();
		Iterable<OpushCollection> addedCollections = opushBackend.addedCollections(lastKnownCollections, changedCollections);
		mocks.verify();
		
		assertThat(addedCollections).containsOnly(collection2);
	}
	
	@Test
	public void testNoAddedCollectionWhenOneInLastKnown() {
		Set<CollectionPath> lastKnownCollections = ImmutableSet.<CollectionPath>of(
				new CollectionPathTest(PIMDataType.EMAIL, "name"));
		
		PathsToCollections changedCollections = PathsToCollections.builder()
				.build();

		mocks.replay();
		Iterable<OpushCollection> addedCollections = opushBackend.addedCollections(lastKnownCollections, changedCollections);
		mocks.verify();
		
		assertThat(addedCollections).isEmpty();
	}
	
	private static class CollectionPathTest extends CollectionPath {

		public CollectionPathTest(PIMDataType pimType, String displayName) {
			super("obm:\\\\test@test\\" + pimType.asCollectionPathValue() + "\\" + displayName, pimType, displayName);
		}
	}
}

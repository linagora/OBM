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
package org.obm.push.bean;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class SyncTest {

	@Test
	public void testValidCollectionsWhenEmpty() {
		Sync sync = new Sync();
		
		assertThat(sync.getCollections()).isEmpty();
		assertThat(sync.getCollectionsValidToProcess()).isEmpty();
	}
	
	@Test
	public void testValidCollections() {
		SyncCollection validCollection = new SyncCollection(5, "path 1");
		validCollection.setStatus(null);
		SyncCollection validCollection2 = new SyncCollection(15, "path 2");
		validCollection2.setStatus(SyncStatus.OK);
		SyncCollection invalidCollection = new SyncCollection(20, "path 3");
		invalidCollection.setStatus(SyncStatus.CONFLICT);
		SyncCollection invalidCollection2 = new SyncCollection(25, "path 4");
		invalidCollection2.setStatus(SyncStatus.OBJECT_NOT_FOUND);

		Sync sync = new Sync();
		sync.addCollection(validCollection);
		sync.addCollection(validCollection2);
		sync.addCollection(invalidCollection);
		sync.addCollection(invalidCollection2);
		
		assertThat(sync.getCollections()).containsOnly(
				validCollection, validCollection2, invalidCollection, invalidCollection2);
		assertThat(sync.getCollectionsValidToProcess()).containsOnly(
				validCollection, validCollection2);
	}
	
}

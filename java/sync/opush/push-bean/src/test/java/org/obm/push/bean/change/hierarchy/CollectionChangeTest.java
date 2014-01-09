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
package org.obm.push.bean.change.hierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.bean.FolderType;


public class CollectionChangeTest {

	@Test(expected=IllegalArgumentException.class)
	public void builderNeedsCollectionId() {
		CollectionChange.builder()
			.parentCollectionId("parent")
			.displayName("name")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.isNew(true)
			.collectionId(null)
			.build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderNeedsCollectionIdEmpty() {
		CollectionChange.builder()
			.parentCollectionId("parent")
			.displayName("name")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.isNew(true)
			.collectionId("")
			.build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderNeedsParentCollectionId() {
		CollectionChange.builder()
			.displayName("name")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.isNew(true)
			.collectionId("id")
			.parentCollectionId(null)
			.build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderNeedsParentCollectionIdEmpty() {
		CollectionChange.builder()
			.displayName("name")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.isNew(true)
			.collectionId("id")
			.parentCollectionId("")
			.build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderNeedsDisplayName() {
		CollectionChange.builder()
			.collectionId("id")
			.parentCollectionId("parent")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.isNew(true)
			.displayName(null)
			.build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderNeedsDisplayNameEmpty() {
		CollectionChange.builder()
			.collectionId("id")
			.parentCollectionId("parent")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.isNew(true)
			.displayName("")
			.build();
	}

	@Test(expected=NullPointerException.class)
	public void builderNeedsFolderType() {
		CollectionChange.builder()
			.collectionId("id")
			.parentCollectionId("parent")
			.isNew(true)
			.displayName("name")
			.folderType(null)
			.build();
	}

	@Test(expected=NullPointerException.class)
	public void builderNeedsIsNew() {
		CollectionChange.builder()
			.collectionId("id")
			.parentCollectionId("parent")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.displayName("name")
			.build();
	}

	@Test
	public void builderNeeds() {
		CollectionChange collectionChange = CollectionChange.builder()
			.collectionId("id")
			.parentCollectionId("parent")
			.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
			.isNew(true)
			.displayName("name")
			.build();
		
		assertThat(collectionChange.getCollectionId()).isEqualTo("id");
		assertThat(collectionChange.getParentCollectionId()).isEqualTo("parent");
		assertThat(collectionChange.getDisplayName()).isEqualTo("name");
		assertThat(collectionChange.getFolderType()).isEqualTo(FolderType.DEFAULT_CALENDAR_FOLDER);
		assertThat(collectionChange.isNew()).isEqualTo(true);
	}
	
}

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
package org.obm.push.contacts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class ContactCollectionPathTest {

	@Test(expected=IllegalArgumentException.class)
	public void builderBackendNameWhenNamePartIsNull() {
		ContactCollectionPath.backendNameFromParts(3, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderBackendNameWhenNamePartIsEmpty() {
		ContactCollectionPath.backendNameFromParts(3, "");
	}

	@Test
	public void builderBackendName() {
		assertThat(ContactCollectionPath.backendNameFromParts(5, "a name")).isEqualTo("5:a name");
	}

	@Test
	public void builderBackendNameWhenNegativeUid() {
		assertThat(ContactCollectionPath.backendNameFromParts(-1, "name")).isEqualTo("-1:name");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void folderNameNull() {
		ContactCollectionPath.parseFolderName(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void folderNameEmpty() {
		ContactCollectionPath.parseFolderName("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void folderNameOnlyUid() {
		ContactCollectionPath.parseFolderName("5");
	}

	@Test(expected=IllegalArgumentException.class)
	public void folderNameOnlyUidAndSeparator() {
		ContactCollectionPath.parseFolderName("5:");
	}

	@Test(expected=IllegalArgumentException.class)
	public void folderNameOnlyName() {
		ContactCollectionPath.parseFolderName("a name");
	}

	@Test(expected=IllegalArgumentException.class)
	public void folderNameOnlyNameAndSeparator() {
		ContactCollectionPath.parseFolderName(":a name");
	}

	@Test(expected=IllegalArgumentException.class)
	public void folderNameTwoSeparator() {
		ContactCollectionPath.parseFolderName("::");
	}

	@Test
	public void folderNameNegativeUid() {
		assertThat(ContactCollectionPath.parseFolderName("-1:a name")).isEqualTo("a name");
	}

	@Test
	public void folderNameLongNegativeUid() {
		assertThat(ContactCollectionPath.parseFolderName("-999999:a name")).isEqualTo("a name");
	}

	@Test
	public void folderNamePositiveUid() {
		assertThat(ContactCollectionPath.parseFolderName("1:a name")).isEqualTo("a name");
	}

	@Test
	public void folderNameLongPositiveUid() {
		assertThat(ContactCollectionPath.parseFolderName("999999:a name")).isEqualTo("a name");
	}

	@Test
	public void folderNameWhenFolderHasSeparatorIntoThisName() {
		assertThat(ContactCollectionPath.parseFolderName("1:a:dangerous:name")).isEqualTo("a:dangerous:name");
	}
}

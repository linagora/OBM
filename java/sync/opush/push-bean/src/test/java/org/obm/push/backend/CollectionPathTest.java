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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;


public class CollectionPathTest {

	@Test(expected=IllegalStateException.class)
	public void nullUserDataRequest() throws CollectionPathException {
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		replay(collectionPathHelper);
		new CollectionPath.Builder(collectionPathHelper).displayName("displayName").pimType(PIMDataType.CALENDAR).build();
	}

	@Test(expected=IllegalStateException.class)
	public void nullPimType() throws CollectionPathException {
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		UserDataRequest udr = createMock(UserDataRequest.class);
		replay(collectionPathHelper, udr);
		new CollectionPath.Builder(collectionPathHelper).displayName("displayName").userDataRequest(udr).build();
	}

	@Test(expected=IllegalStateException.class)
	public void nullDisplayName() throws CollectionPathException {
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		UserDataRequest udr = createMock(UserDataRequest.class);
		replay(collectionPathHelper, udr);
		new CollectionPath.Builder(collectionPathHelper).pimType(PIMDataType.CALENDAR).userDataRequest(udr).build();
	}
	
	@Test
	public void validCollectionPath() {
		PIMDataType collectionType = PIMDataType.CALENDAR;
		String displayName = "displayName";
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		UserDataRequest udr = createMock(UserDataRequest.class);
		expect(collectionPathHelper.buildCollectionPath(udr, collectionType, displayName)).andReturn("collectionPath");
		replay(collectionPathHelper, udr);
		
		CollectionPath collectionPath = new CollectionPath.Builder(collectionPathHelper)
			.pimType(collectionType)
			.displayName(displayName)
			.userDataRequest(udr)
			.build();
		
		verify(collectionPathHelper, udr);
		assertThat(collectionPath.collectionPath()).isEqualTo("collectionPath");
		assertThat(collectionPath.displayName()).isEqualTo(displayName);
		assertThat(collectionPath.pimType()).isEqualTo(collectionType);
	}

	@Test(expected=IllegalStateException.class)
	public void testBuildByQualifiedCollectionPathDenyDisplayName() throws CollectionPathException {
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		UserDataRequest udr = createMock(UserDataRequest.class);
		replay(collectionPathHelper, udr);
		
		new CollectionPath.Builder(collectionPathHelper)
			.userDataRequest(udr)	
			.fullyQualifiedCollectionPath("obm:\\\\login@domain\\email\\INBOX")
			.displayName("displayName")
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testBuildByQualifiedCollectionPathDenyPIMDataType() throws CollectionPathException {
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		UserDataRequest udr = createMock(UserDataRequest.class);
		replay(collectionPathHelper, udr);
		
		new CollectionPath.Builder(collectionPathHelper)
			.userDataRequest(udr)	
			.fullyQualifiedCollectionPath("obm:\\\\login@domain\\email\\INBOX")
			.pimType(PIMDataType.EMAIL)
			.build();
	}

	@Test
	public void testValidBuildByQualifiedCollectionPath() throws CollectionPathException {
		String qualifiedCollectionPath = "obm:\\\\login@domain\\email\\INBOX";

		UserDataRequest udr = createMock(UserDataRequest.class);
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(qualifiedCollectionPath)).andReturn(PIMDataType.EMAIL);
		expect(collectionPathHelper.extractFolder(udr, qualifiedCollectionPath, PIMDataType.EMAIL)).andReturn("INBOX");
		replay(collectionPathHelper, udr); 
		
		CollectionPath collectionPath = new CollectionPath.Builder(collectionPathHelper)
			.userDataRequest(udr)	
			.fullyQualifiedCollectionPath(qualifiedCollectionPath)
			.build();

		verify(collectionPathHelper, udr);
		assertThat(collectionPath.collectionPath()).isEqualTo(qualifiedCollectionPath);
		assertThat(collectionPath.displayName()).isEqualTo("INBOX");
		assertThat(collectionPath.pimType()).isEqualTo(PIMDataType.EMAIL);
	}

	@Test
	public void testRootUserCollectionPathIsUnknownDataType() throws CollectionPathException {
		String qualifiedCollectionPath = "obm:\\\\login@domain.org";

		UserDataRequest udr = createMock(UserDataRequest.class);
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(qualifiedCollectionPath)).andReturn(PIMDataType.UNKNOWN);
		replay(collectionPathHelper, udr); 
		
		CollectionPath collectionPath = new CollectionPath.Builder(collectionPathHelper)
			.userDataRequest(udr)	
			.fullyQualifiedCollectionPath(qualifiedCollectionPath)
			.build();

		verify(collectionPathHelper, udr);
		assertThat(collectionPath.collectionPath()).isEqualTo(qualifiedCollectionPath);
		assertThat(collectionPath.displayName()).isNull();
		assertThat(collectionPath.pimType()).isEqualTo(PIMDataType.UNKNOWN);
	}

	@Test
	public void testUnexpectedCollectionPathIsUnknownDataType() throws CollectionPathException {
		String qualifiedCollectionPath = "obm:\\\\login@domain.org\\unexpected\\contacts";

		UserDataRequest udr = createMock(UserDataRequest.class);
		CollectionPathHelper collectionPathHelper = createMock(CollectionPathHelper.class);
		expect(collectionPathHelper.recognizePIMDataType(qualifiedCollectionPath)).andReturn(PIMDataType.UNKNOWN);
		replay(collectionPathHelper, udr); 
		
		CollectionPath collectionPath = new CollectionPath.Builder(collectionPathHelper)
			.userDataRequest(udr)	
			.fullyQualifiedCollectionPath(qualifiedCollectionPath)
			.build();

		verify(collectionPathHelper, udr);
		assertThat(collectionPath.collectionPath()).isEqualTo(qualifiedCollectionPath);
		assertThat(collectionPath.displayName()).isNull();
		assertThat(collectionPath.pimType()).isEqualTo(PIMDataType.UNKNOWN);
	}
}

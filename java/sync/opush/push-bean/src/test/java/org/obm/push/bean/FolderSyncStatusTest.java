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
package org.obm.push.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class FolderSyncStatusTest {
	
	@Test
	public void specValueOK() {
		assertThat(FolderSyncStatus.OK.asXmlValue()).isEqualTo("1");
	}

	@Test
	public void specValueServerError() {
		assertThat(FolderSyncStatus.SERVER_ERROR.asXmlValue()).isEqualTo("6");
	}
	
	@Test
	public void specValueAccessDenied() {
		assertThat(FolderSyncStatus.ACCESS_DENIED.asXmlValue()).isEqualTo("7");
	}

	@Test
	public void specValueTimeOut() {
		assertThat(FolderSyncStatus.TIMED_OUT.asXmlValue()).isEqualTo("8");
	}

	@Test
	public void specValueInvalidSyncKey() {
		assertThat(FolderSyncStatus.INVALID_SYNC_KEY.asXmlValue()).isEqualTo("9");
	}
	
	@Test
	public void specValueInvalidRequest() {
		assertThat(FolderSyncStatus.INVALID_REQUEST.asXmlValue()).isEqualTo("10");
	}
	
	@Test
	public void specValueUnknownError() {
		assertThat(FolderSyncStatus.UNKNOW_ERROR.asXmlValue()).isEqualTo("11");
	}

	@Test
	public void reverseFromNull() {
		assertThat(FolderSyncStatus.fromSpecificationValue(null)).isNull();
	}
	
	@Test
	public void reverseFromEmpty() {
		assertThat(FolderSyncStatus.fromSpecificationValue("")).isNull();
	}
	
	@Test
	public void reverseFromNotExisting() {
		assertThat(FolderSyncStatus.fromSpecificationValue("blabla")).isNull();
	}

	@Test
	public void reverseFromSpecOK() {
		assertThat(FolderSyncStatus.fromSpecificationValue("1")).isEqualTo(FolderSyncStatus.OK);
	}

	@Test
	public void reverseFromSpecServerError() {
		assertThat(FolderSyncStatus.fromSpecificationValue("6")).isEqualTo(FolderSyncStatus.SERVER_ERROR);
	}

	@Test
	public void reverseFromSpecAccessDenied() {
		assertThat(FolderSyncStatus.fromSpecificationValue("7")).isEqualTo(FolderSyncStatus.ACCESS_DENIED);
	}
	
	@Test
	public void reverseFromSpecTimeout() {
		assertThat(FolderSyncStatus.fromSpecificationValue("8")).isEqualTo(FolderSyncStatus.TIMED_OUT);
	}
	
	@Test
	public void reverseFromSpecInvalidSyncKey() {
		assertThat(FolderSyncStatus.fromSpecificationValue("9")).isEqualTo(FolderSyncStatus.INVALID_SYNC_KEY);
	}
	
	@Test
	public void reverseFromSpecInvalidRequest() {
		assertThat(FolderSyncStatus.fromSpecificationValue("10")).isEqualTo(FolderSyncStatus.INVALID_REQUEST);
	}

	@Test
	public void reverseFromSpecUnkonwnError() {
		assertThat(FolderSyncStatus.fromSpecificationValue("11")).isEqualTo(FolderSyncStatus.UNKNOW_ERROR);
	}
	
}

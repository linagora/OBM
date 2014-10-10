/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.imap.archive.beans.ArchiveStatus.ERROR;
import static org.obm.imap.archive.beans.ArchiveStatus.RUNNING;
import static org.obm.imap.archive.beans.ArchiveStatus.SCHEDULED;
import static org.obm.imap.archive.beans.ArchiveStatus.SUCCESS;
import static org.obm.imap.archive.beans.ArchiveStatus.fromSpecificationValue;

import org.junit.Test;

public class ArchiveStatusTest {

	@Test
	public void error() {
		assertThat(ERROR.asSpecificationValue()).isEqualTo("ERROR");
		assertThat(fromSpecificationValue("ERROR")).isEqualTo(ERROR);
	}
	
	@Test
	public void scheduled() {
		assertThat(SCHEDULED.asSpecificationValue()).isEqualTo("SCHEDULED");
		assertThat(fromSpecificationValue("SCHEDULED")).isEqualTo(SCHEDULED);
	}
	
	@Test
	public void success() {
		assertThat(SUCCESS.asSpecificationValue()).isEqualTo("SUCCESS");
		assertThat(fromSpecificationValue("SUCCESS")).isEqualTo(SUCCESS);
	}
	
	@Test
	public void running() {
		assertThat(RUNNING.asSpecificationValue()).isEqualTo("RUNNING");
		assertThat(fromSpecificationValue("RUNNING")).isEqualTo(RUNNING);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void unknown() {
		fromSpecificationValue("unknown");
	}

	@Test
	public void terminated() {
		assertThat(ArchiveStatus.TERMINATED).containsOnly(ArchiveStatus.ERROR, ArchiveStatus.SUCCESS);
	}
}

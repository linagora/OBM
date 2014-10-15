/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

import java.util.UUID;

import org.junit.Test;

public class ExcludedUserTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenIdIsNull() {
		ExcludedUser.builder().id(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void builderShouldThrowWhenNotaUUID() {
		ExcludedUser.builder().id("not-uuid").build();
	}
	
	@Test
	public void builderShouldBuild() {
		ExcludedUser excludedUser = ExcludedUser.builder().id("08607f19-05a4-42a2-9b02-6f11f3ceff3b").build();
		assertThat(excludedUser.getId()).isEqualTo(UUID.fromString("08607f19-05a4-42a2-9b02-6f11f3ceff3b"));
	}
	
	@Test
	public void fromShouldBuild() {
		ExcludedUser excludedUser = ExcludedUser.from("08607f19-05a4-42a2-9b02-6f11f3ceff3b");
		assertThat(excludedUser.getId()).isEqualTo(UUID.fromString("08607f19-05a4-42a2-9b02-6f11f3ceff3b"));
	}
	
	@Test
	public void serialize() {
		ExcludedUser excludedUser = ExcludedUser.from("08607f19-05a4-42a2-9b02-6f11f3ceff3b");
		assertThat(excludedUser.serialize()).isEqualTo("08607f19-05a4-42a2-9b02-6f11f3ceff3b");
	}
}

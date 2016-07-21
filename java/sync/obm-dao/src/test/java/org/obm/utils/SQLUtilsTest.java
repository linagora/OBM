/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2016 Linagora
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
package org.obm.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

public class SQLUtilsTest {

	@Test
	public void testSelectCalendarsConditionNullCollection() {
		Collection<String> emails = null;

		assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("");
	}

	@Test
	public void testSelectCalendarsConditionEmptyCollection() {
		Collection<String> emails = Collections.emptySet();

		assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("");
	}

	@Test
	public void testSelectCalendarsConditionSingleElementCollection() {
		Collection<String> emails = Collections.singleton("test@test.com");

		assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("AND (userobm_email LIKE ?) ");
	}

	@Test
	public void testSelectCalendarsCondition() {
		Collection<String> emails = new ArrayList<String>();

		emails.add("test@test.com");
		emails.add("test2@test.com");
		emails.add("test3@test.com");

		assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("AND (userobm_email LIKE ? OR userobm_email LIKE ? OR userobm_email LIKE ?) ");
	}

	@Test
	public void testSelectCalendarsConditionMoreElements() {
		Collection<String> emails = new ArrayList<String>();

		emails.add("test@test.com");
		emails.add("test2@test.com");
		emails.add("test3@test.com");
		emails.add("test4@test.com");
		emails.add("test5@test.com");
		emails.add("test6@test.com");

		assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo(
				"AND (userobm_email LIKE ? OR userobm_email LIKE ? OR userobm_email LIKE ? OR userobm_email LIKE ? OR userobm_email LIKE ? OR userobm_email LIKE ?) ");
	}

	@Test
	public void testSelectUsersMatchingPatternCondition_NullPattern() {
		assertThat(SQLUtils.selectUsersMatchingPatternCondition(null)).isEmpty();
	}

	@Test
	public void testSelectUsersMatchingPatternCondition_EmptyPattern() {
		assertThat(SQLUtils.selectUsersMatchingPatternCondition("")).isEmpty();
	}

	@Test
	public void testSelectUsersMatchingPatternCondition() {
		assertThat(SQLUtils.selectUsersMatchingPatternCondition("user")).isEqualTo(
			"AND (LOWER(userobm_login) LIKE ? OR LOWER(userobm_lastname) LIKE ? OR LOWER(userobm_firstname) LIKE ?) "
		);
	}

	@Test
	public void testSelectResourcesMatchingPatternCondition_NullPattern() {
		assertThat(SQLUtils.selectResourcesMatchingPatternCondition(null)).isEmpty();
	}

	@Test
	public void testSelectResourcesMatchingPatternCondition_EmptyPattern() {
		assertThat(SQLUtils.selectResourcesMatchingPatternCondition("")).isEmpty();
	}

	@Test
	public void testSelectResourcesMatchingPatternCondition() {
		assertThat(SQLUtils.selectResourcesMatchingPatternCondition("resource")).isEqualTo(
			"AND (LOWER(r.resource_name) LIKE ? OR LOWER(r.resource_description) LIKE ?) "
		);
	}

}

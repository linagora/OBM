package org.obm.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import org.obm.utils.SQLUtils;

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

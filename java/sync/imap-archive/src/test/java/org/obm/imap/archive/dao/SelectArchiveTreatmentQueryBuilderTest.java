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

package org.obm.imap.archive.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Limit;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun.Fields;
import org.obm.utils.ObmHelper;

import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class SelectArchiveTreatmentQueryBuilderTest {

	private IMocksControl control;
	private ObmHelper obmHelper;
	
	@Before
	public void setup() {
		control = createControl();
		obmHelper = control.createMock(ObmHelper.class);
		
		control.replay();
	}
	
	@After
	public void tearDown() {
		control.verify();
	}
	
	@Test(expected=NullPointerException.class)
	public void archiveStatusShouldNotBeNull() {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).where((Iterable<ArchiveStatus>) null);
	}
	
	@Test(expected=NullPointerException.class)
	public void archiveStatusElementShouldNotBeNull() {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).where(Sets.newHashSet(ArchiveStatus.ERROR, null, ArchiveStatus.SUCCESS));
	}

	@Test(expected=NullPointerException.class)
	public void archiveTreatmentRunIdShouldNotBeNull() {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).where((ArchiveTreatmentRunId) null);
	}

	@Test(expected=NullPointerException.class)
	public void domainIdShouldNotBeNull() {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).where((ObmDomainUuid) null);
	}

	@Test(expected=NullPointerException.class)
	public void orderingFieldShouldNotBeNull() {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).orderBy(null, null);
	}

	@Test(expected=NullPointerException.class)
	public void orderingShouldNotBeNull() {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).orderBy(Fields.DOMAIN_UUID, null);
	}

	@Test(expected=NullPointerException.class)
	public void limitShouldNotBeNull() {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).limit(null);
	}
	
	@Test
	public void simpleSelect() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run");
	}
	
	@Test
	public void selectWhereArchiveStatus() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.where(ArchiveStatus.TERMINATED).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run WHERE " + Fields.STATUS + " IN (?,?)");
	}
	
	@Test
	public void selectWhereArchiveTreatmentRunId() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.where(ArchiveTreatmentRunId.from("12dee3ed-bbbe-462e-8b84-ce4f5c9cffa5")).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run WHERE " + Fields.UUID + " = ?");
	}
	
	@Test
	public void selectWhereObmDomainUuid() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.where(ObmDomainUuid.of("cd2b23e2-e2a9-40dd-846d-53c584c98760")).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run WHERE " + Fields.DOMAIN_UUID + " = ?");
	}
	
	@Test
	public void selectAllWhere() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.where(ArchiveStatus.TERMINATED)
				.where(ArchiveTreatmentRunId.from("12dee3ed-bbbe-462e-8b84-ce4f5c9cffa5"))
				.where(ObmDomainUuid.of("cd2b23e2-e2a9-40dd-846d-53c584c98760")).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run WHERE " + Fields.UUID + " = ? AND " + Fields.DOMAIN_UUID + " = ? AND " + Fields.STATUS + " IN (?,?)");
	}
	
	@Test
	public void selectDescOrderBy() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.orderBy(Fields.DOMAIN_UUID, Ordering.DESC).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run ORDER BY " + Fields.DOMAIN_UUID + " DESC");
	}
	
	@Test
	public void selectAscOrderBy() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.orderBy(Fields.DOMAIN_UUID, Ordering.ASC).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run ORDER BY " + Fields.DOMAIN_UUID + " ASC");
	}
	
	@Test
	public void selectAllWhereOrderBy() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.where(ArchiveStatus.TERMINATED)
				.where(ArchiveTreatmentRunId.from("12dee3ed-bbbe-462e-8b84-ce4f5c9cffa5"))
				.where(ObmDomainUuid.of("cd2b23e2-e2a9-40dd-846d-53c584c98760"))
				.orderBy(Fields.DOMAIN_UUID, Ordering.DESC).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run"
					+ " WHERE " + Fields.UUID + " = ? AND " + Fields.DOMAIN_UUID + " = ? AND " + Fields.STATUS + " IN (?,?)"
					+ " ORDER BY " + Fields.DOMAIN_UUID + " DESC");
	}
	
	@Test
	public void selectLimit() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.limit(Limit.from(2)).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run LIMIT 2");
	}
	
	@Test
	public void selectAllWhereOrderByLimit() {
		assertThat(new SelectArchiveTreatmentQueryBuilder(obmHelper)
				.where(ArchiveStatus.TERMINATED)
				.where(ArchiveTreatmentRunId.from("12dee3ed-bbbe-462e-8b84-ce4f5c9cffa5"))
				.where(ObmDomainUuid.of("cd2b23e2-e2a9-40dd-846d-53c584c98760"))
				.orderBy(Fields.DOMAIN_UUID, Ordering.DESC)
				.limit(Limit.from(2)).buildQueryString())
			.isEqualTo("SELECT " + ArchiveTreatmentJdbcImpl.REQUESTS.ALL + " FROM mail_archive_run"
					+ " WHERE " + Fields.UUID + " = ? AND " + Fields.DOMAIN_UUID + " = ? AND " + Fields.STATUS + " IN (?,?)"
					+ " ORDER BY " + Fields.DOMAIN_UUID + " DESC"
					+ " LIMIT 2");
	}
	
	@Test(expected=NullPointerException.class)
	public void test() throws Exception {
		new SelectArchiveTreatmentQueryBuilder(obmHelper).prepareStatement(null);
	}
}

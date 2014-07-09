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

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class ArchiveTerminatedTreatmentTest {
	

	ObmDomainUuid domainUuid;
	ArchiveTreatmentRunId runId;
	DateTime scheduledTime;
	DateTime startedAt;
	DateTime endTime;
	DateTime higherBoundary;

	@Before
	public void setUp() {
		domainUuid = ObmDomainUuid.of("b7e91835-68de-498f-bff8-97d43acf222c");
		runId = ArchiveTreatmentRunId.from("f393aab2-4a98-4f59-8a25-4b7a8db2b377");
		scheduledTime = DateTime.parse("2020-10-1T02:00Z");
		startedAt = DateTime.parse("2020-10-3T02:00Z");
		endTime = DateTime.parse("2020-10-5T02:00Z");
		higherBoundary = DateTime.parse("2020-10-8T02:00Z");
	}
	
	@Test(expected=NullPointerException.class)
	public void forDomainShouldTriggerNPEWhenNull() {
		ArchiveTerminatedTreatment.forDomain(null);
	}

	@Test(expected=IllegalStateException.class)
	public void buildShouldTriggerExceptionWhenNoScheduledTime() {
		ArchiveTerminatedTreatment
			.forDomain(domainUuid)
			.runId(runId)
			.higherBoundary(higherBoundary)
			.startedAt(startedAt)
			.terminatedAt(endTime)
			.status(ArchiveStatus.SUCCESS)
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildShouldTriggerExceptionWhenNoRunId() {
		ArchiveTerminatedTreatment
			.forDomain(domainUuid)
			.scheduledAt(scheduledTime)
			.higherBoundary(higherBoundary)
			.startedAt(startedAt)
			.terminatedAt(endTime)
			.status(ArchiveStatus.SUCCESS)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void buildShouldTriggerExceptionWhenNoHigherBoundary() {
		ArchiveTerminatedTreatment
			.forDomain(domainUuid)
			.runId(runId)
			.scheduledAt(scheduledTime)
			.startedAt(startedAt)
			.terminatedAt(endTime)
			.status(ArchiveStatus.SUCCESS)
			.build();
	}
	
	@Test
	public void buildWhenAsError() {
		ArchiveTerminatedTreatment testee = ArchiveTerminatedTreatment
			.forDomain(domainUuid)
			.runId(runId)
			.scheduledAt(scheduledTime)
			.higherBoundary(higherBoundary)
			.startedAt(startedAt)
			.terminatedAt(endTime)
			.status(ArchiveStatus.ERROR)
			.build();
		
		assertThat(testee.getArchiveStatus()).isEqualTo(ArchiveStatus.ERROR);
		assertThat(testee.getDomainUuid()).isEqualTo(domainUuid);
		assertThat(testee.getRunId()).isEqualTo(runId);
		assertThat(testee.getScheduledTime()).isEqualTo(scheduledTime);
		assertThat(testee.getStartTime()).isEqualTo(startedAt);
		assertThat(testee.getHigherBoundary()).isEqualTo(higherBoundary);
	}
	
	@Test
	public void buildWhenAsSuccess() {
		ArchiveTerminatedTreatment testee = ArchiveTerminatedTreatment
			.forDomain(domainUuid)
			.runId(runId)
			.scheduledAt(scheduledTime)
			.higherBoundary(higherBoundary)
			.startedAt(startedAt)
			.terminatedAt(endTime)
			.status(ArchiveStatus.SUCCESS)
			.build();
		
		assertThat(testee.getArchiveStatus()).isEqualTo(ArchiveStatus.SUCCESS);
		assertThat(testee.getDomainUuid()).isEqualTo(domainUuid);
		assertThat(testee.getRunId()).isEqualTo(runId);
		assertThat(testee.getScheduledTime()).isEqualTo(scheduledTime);
		assertThat(testee.getStartTime()).isEqualTo(startedAt);
		assertThat(testee.getHigherBoundary()).isEqualTo(higherBoundary);
	}
}

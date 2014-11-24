/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

package org.obm.imap.archive.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveRunningTreatmentTest {

	ObmDomainUuid domainUuid;
	ArchiveTreatmentRunId runId;
	DateTime scheduledTime;
	DateTime startTime;
	DateTime higherBoundary;

	@Before
	public void setUp() {
		domainUuid = ObmDomainUuid.of("b7e91835-68de-498f-bff8-97d43acf222c");
		runId = ArchiveTreatmentRunId.from("f393aab2-4a98-4f59-8a25-4b7a8db2b377");
		scheduledTime = DateTime.parse("2020-10-1T02:00Z");
		startTime = DateTime.parse("2020-10-3T02:00Z");
		higherBoundary = DateTime.parse("2020-10-8T02:00Z");
	}
	
	@Test(expected=NullPointerException.class)
	public void forDomainShouldTriggerNPEWhenNull() {
		ArchiveRunningTreatment.forDomain(null);
	}

	@Test(expected=IllegalStateException.class)
	public void buildShouldTriggerExceptionWhenNoScheduledTime() {
		ArchiveRunningTreatment
			.forDomain(domainUuid)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.startedAt(startTime)
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildShouldTriggerExceptionWhenNoRunId() {
		ArchiveRunningTreatment
			.forDomain(domainUuid)
			.recurrent(true)
			.scheduledAt(scheduledTime)
			.higherBoundary(higherBoundary)
			.startedAt(startTime)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void buildShouldTriggerExceptionWhenNoHigherBoundary() {
		ArchiveRunningTreatment
			.forDomain(domainUuid)
			.recurrent(true)
			.runId(runId)
			.scheduledAt(scheduledTime)
			.startedAt(startTime)
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildShouldTriggerExceptionWhenNoRecurrent() {
		ArchiveRunningTreatment
		.forDomain(domainUuid)
		.runId(runId)
		.higherBoundary(higherBoundary)
		.scheduledAt(scheduledTime)
		.startedAt(startTime)
		.build();
	}
	
	@Test
	public void buildWhenAsRunning() {
		ArchiveRunningTreatment testee = ArchiveRunningTreatment
			.forDomain(domainUuid)
			.runId(runId)
			.recurrent(true)
			.scheduledAt(scheduledTime)
			.higherBoundary(higherBoundary)
			.startedAt(startTime)
			.build();
		
		assertThat(testee.getArchiveStatus()).isEqualTo(ArchiveStatus.RUNNING);
		assertThat(testee.getDomainUuid()).isEqualTo(domainUuid);
		assertThat(testee.getRunId()).isEqualTo(runId);
		assertThat(testee.getScheduledTime()).isEqualTo(scheduledTime);
		assertThat(testee.getStartTime()).isEqualTo(startTime);
		assertThat(testee.getHigherBoundary()).isEqualTo(higherBoundary);
		assertThat(testee.isRecurrent()).isTrue();
	}

	@Test
	public void asErrorShouldBuildWithSameProperty() {
		DateTime startTime = DateTime.parse("2020-10-4T02:00Z");
		DateTime endTime = DateTime.parse("2020-10-5T02:00Z");
		ArchiveTerminatedTreatment testee = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(scheduledTime)
				.build()
				.asRunning(startTime)
				.asError(endTime);
		
		assertThat(testee.getArchiveStatus()).isEqualTo(ArchiveStatus.ERROR);
		assertThat(testee.getDomainUuid()).isEqualTo(domainUuid);
		assertThat(testee.getRunId()).isEqualTo(runId);
		assertThat(testee.getScheduledTime()).isEqualTo(scheduledTime);
		assertThat(testee.getHigherBoundary()).isEqualTo(higherBoundary);
		assertThat(testee.getStartTime()).isEqualTo(startTime);
		assertThat(testee.getEndTime()).isEqualTo(endTime);
		assertThat(testee.isRecurrent()).isTrue();
	}

	@Test
	public void asSuccessShouldBuildWithSameProperty() {
		DateTime startTime = DateTime.parse("2020-10-4T02:00Z");
		DateTime endTime = DateTime.parse("2020-10-5T02:00Z");
		ArchiveTerminatedTreatment testee = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(scheduledTime)
				.build()
				.asRunning(startTime)
				.asSuccess(endTime);

		assertThat(testee.getArchiveStatus()).isEqualTo(ArchiveStatus.SUCCESS);
		assertThat(testee.getDomainUuid()).isEqualTo(domainUuid);
		assertThat(testee.getRunId()).isEqualTo(runId);
		assertThat(testee.getScheduledTime()).isEqualTo(scheduledTime);
		assertThat(testee.getHigherBoundary()).isEqualTo(higherBoundary);
		assertThat(testee.getStartTime()).isEqualTo(startTime);
		assertThat(testee.getEndTime()).isEqualTo(endTime);
		assertThat(testee.isRecurrent()).isTrue();
	}
}

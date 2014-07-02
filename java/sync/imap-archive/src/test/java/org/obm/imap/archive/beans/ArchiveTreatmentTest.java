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
import org.junit.Test;

import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class ArchiveTreatmentTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenRunIdIsNull() {
		ArchiveTreatment.builder().runId(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenDomainIdIsNull() {
		ArchiveTreatment.builder().domainId(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenArchiveStatusIsNull() {
		ArchiveTreatment.builder().archiveStatus(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenStartIsNull() {
		ArchiveTreatment.builder().start(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenEndIsNull() {
		ArchiveTreatment.builder().end(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenLowerBoundaryIsNull() {
		ArchiveTreatment.builder().lowerBoundary(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenHigherBoundaryIsNull() {
		ArchiveTreatment.builder().higherBoundary(null);
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenRunIsNotProvided() {
		ArchiveTreatment.builder().build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenDomainIdIsNotProvided() {
		ArchiveTreatment.builder()
			.runId(ArchiveTreatmentRunId.from("e294d88f-a0b6-4f9c-b310-88cfe7be8679"))
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenArchiveStatusIsNotProvided() {
		ArchiveTreatment.builder()
			.runId(ArchiveTreatmentRunId.from("e294d88f-a0b6-4f9c-b310-88cfe7be8679"))
			.domainId(ObmDomainUuid.of("496610b6-400e-4b6d-abde-fdc8e4015b04"))
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenStartIsNotProvided() {
		ArchiveTreatment.builder()
			.runId(ArchiveTreatmentRunId.from("e294d88f-a0b6-4f9c-b310-88cfe7be8679"))
			.domainId(ObmDomainUuid.of("496610b6-400e-4b6d-abde-fdc8e4015b04"))
			.archiveStatus(ArchiveStatus.SUCCESS)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenEndIsNotProvidedAndSuccess() {
		ArchiveTreatment.builder()
			.runId(ArchiveTreatmentRunId.from("e294d88f-a0b6-4f9c-b310-88cfe7be8679"))
			.domainId(ObmDomainUuid.of("496610b6-400e-4b6d-abde-fdc8e4015b04"))
			.archiveStatus(ArchiveStatus.SUCCESS)
			.start(DateTime.now())
			.build();
	}
	
	@Test
	public void builderShouldNotThrowWhenEndIsNotProvidedAndRunning() {
		ArchiveTreatment.builder()
			.runId(ArchiveTreatmentRunId.from("e294d88f-a0b6-4f9c-b310-88cfe7be8679"))
			.domainId(ObmDomainUuid.of("496610b6-400e-4b6d-abde-fdc8e4015b04"))
			.archiveStatus(ArchiveStatus.RUNNING)
			.start(DateTime.now())
			.build();
	}

	@Test
	public void builderShouldBuildWhenEveryThingIsProvided() {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("e294d88f-a0b6-4f9c-b310-88cfe7be8679");
		ObmDomainUuid domainId = ObmDomainUuid.of("496610b6-400e-4b6d-abde-fdc8e4015b04");
		DateTime start = DateTime.parse("2014-07-01T00:00:00.000Z");
		DateTime end = DateTime.parse("2014-07-01T00:01:00.000Z");
		DateTime lowerBoundary = DateTime.parse("2014-07-01T00:02:00.000Z");
		DateTime higherBoundary = DateTime.parse("2014-07-01T00:03:00.000Z");
		
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder()
			.runId(runId)
			.domainId(domainId)
			.archiveStatus(ArchiveStatus.SUCCESS)
			.start(start)
			.end(end)
			.lowerBoundary(lowerBoundary)
			.higherBoundary(higherBoundary)
			.build();
		
		assertThat(archiveTreatment.getRunId()).isEqualTo(runId);
		assertThat(archiveTreatment.getDomainId()).isEqualTo(domainId);
		assertThat(archiveTreatment.getArchiveStatus()).isEqualTo(ArchiveStatus.SUCCESS);
		assertThat(archiveTreatment.getStart()).isEqualTo(start);
		assertThat(archiveTreatment.getEnd()).isEqualTo(end);
		assertThat(archiveTreatment.getLowerBoundary()).isEqualTo(lowerBoundary);
		assertThat(archiveTreatment.getHigherBoundary()).isEqualTo(higherBoundary);
	}
}

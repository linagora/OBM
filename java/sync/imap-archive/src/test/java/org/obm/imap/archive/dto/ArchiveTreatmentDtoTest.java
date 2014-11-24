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

package org.obm.imap.archive.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;

import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class ArchiveTreatmentDtoTest {

	@Test
	public void fromArchiveTreatmentShouldCopyAllFields() {
		ArchiveTreatment archiveTreatment = 
				ArchiveTreatment.builder(ObmDomainUuid.of("6d5dfed2-ee66-427f-9186-152751efb97e"))
					.runId(ArchiveTreatmentRunId.from("20bafa17-c621-4c87-825e-d4de15d69d51"))
					.status(ArchiveStatus.RUNNING)
					.scheduledAt(DateTime.parse("2014-09-05T14:10:00.000Z"))
					.startedAt(DateTime.parse("2014-09-05T14:10:05.000Z"))
					.terminatedAt(DateTime.parse("2014-09-05T14:10:10.000Z"))
					.higherBoundary(DateTime.parse("2014-08-05T14:10:00.000Z"))
					.recurrent(true)
					.build();
		
		ArchiveTreatmentDto dto = ArchiveTreatmentDto.from(archiveTreatment);
		assertThat(dto.domainUuid).isEqualTo(UUID.fromString("6d5dfed2-ee66-427f-9186-152751efb97e"));
		assertThat(dto.runId).isEqualTo(UUID.fromString("20bafa17-c621-4c87-825e-d4de15d69d51"));
		assertThat(dto.archiveStatus).isEqualTo("RUNNING");
		assertThat(dto.scheduledTime).isEqualTo(DateTime.parse("2014-09-05T14:10:00.000Z"));
		assertThat(dto.startTime).isEqualTo(DateTime.parse("2014-09-05T14:10:05.000Z"));
		assertThat(dto.endTime).isEqualTo(DateTime.parse("2014-09-05T14:10:10.000Z"));
		assertThat(dto.higherBoundary).isEqualTo(DateTime.parse("2014-08-05T14:10:00.000Z"));
		assertThat(dto.recurrent).isEqualTo(true);

	}
	
}

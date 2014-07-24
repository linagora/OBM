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


public class ProcessedFolderTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenRunIsNull() {
		ProcessedFolder.builder().runId(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenFolderNull() {
		ProcessedFolder.builder().folder(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenStartNull() {
		ProcessedFolder.builder().start(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenEndNull() {
		ProcessedFolder.builder().end(null);
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenRunIdNotProvided() {
		ProcessedFolder.builder().build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenFolderNotProvided() {
		ProcessedFolder.builder()
			.runId(ArchiveTreatmentRunId.from("1fa66563-926c-4600-a7a3-f56877f38737"))
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenUidNextNotProvided() {
		ProcessedFolder.builder()
			.runId(ArchiveTreatmentRunId.from("1fa66563-926c-4600-a7a3-f56877f38737"))
			.folder(ImapFolder.from("user/usera/Test@mydomain.org"))
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenStartNotProvided() {
		ProcessedFolder.builder()
			.runId(ArchiveTreatmentRunId.from("1fa66563-926c-4600-a7a3-f56877f38737"))
			.folder(ImapFolder.from("user/usera/Test@mydomain.org"))
			.uidNext(12l)
			.build();
	}

	@Test(expected=IllegalStateException.class)
	public void builderShouldThrowWhenEndNotProvided() {
		ProcessedFolder.builder()
			.runId(ArchiveTreatmentRunId.from("1fa66563-926c-4600-a7a3-f56877f38737"))
			.folder(ImapFolder.from("user/usera/Test@mydomain.org"))
			.uidNext(12l)
			.start(DateTime.parse("2014-07-23T08:21:00.000Z"))
			.build();
	}

	@Test
	public void builderShouldBuildWhenEveryThingIsProvided() {
		ArchiveTreatmentRunId archiveTreatmentRunId = ArchiveTreatmentRunId.from("1fa66563-926c-4600-a7a3-f56877f38737");
		ImapFolder imapFolder = ImapFolder.from("user/usera/Test@mydomain.org");
		long uidNext = 12;
		DateTime start = DateTime.parse("2014-07-23T08:21:00.000Z");
		DateTime end = DateTime.parse("2014-07-23T08:21:03.000Z");
		
		ProcessedFolder processedFolder = ProcessedFolder.builder()
			.runId(archiveTreatmentRunId)
			.folder(imapFolder)
			.uidNext(uidNext)
			.start(start)
			.end(end)
			.build();
		
		assertThat(processedFolder.getRunId()).isEqualTo(archiveTreatmentRunId);
		assertThat(processedFolder.getFolder()).isEqualTo(imapFolder);
		assertThat(processedFolder.getUidNext()).isEqualTo(uidNext);
		assertThat(processedFolder.getStart()).isEqualTo(start);
		assertThat(processedFolder.getEnd()).isEqualTo(end);
	}
}

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

package org.obm.imap.archive;

import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.dao.DomainConfigurationJdbcImpl;
import org.obm.imap.archive.dao.SqlTables;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class DatabaseOperations {

	public static Operation cleanDB() {
		return Operations.sequenceOf(
				Operations.deleteAllFrom(DomainConfigurationJdbcImpl.TABLE.NAME),
				Operations.deleteAllFrom(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.NAME),
				Operations.deleteAllFrom(SqlTables.MailArchiveRun.NAME));
	}

	public static Insert insertDomainConfiguration(ObmDomainUuid domainId, ConfigurationState state) {
		return Operations.insertInto(DomainConfigurationJdbcImpl.TABLE.NAME)
			.columns(DomainConfigurationJdbcImpl.TABLE.FIELDS.DOMAIN_UUID, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.ACTIVATED, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.REPEAT_KIND, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_WEEK, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_MONTH, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_YEAR, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.HOUR, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.MINUTE)
			.values(domainId.get(), 
					ConfigurationState.ENABLE == state ? true : false, 
					RepeatKind.DAILY, 
					2, 10, 355, 10, 32)
			.build();
	}
	
	public static Insert insertArchiveTreatment(ArchiveTreatmentRunId runId, ObmDomainUuid domainId) {
		return Operations.insertInto(MailArchiveRun.NAME)
			.columns(MailArchiveRun.Fields.UUID,
					MailArchiveRun.Fields.DOMAIN_UUID,
					MailArchiveRun.Fields.STATUS, 
					MailArchiveRun.Fields.SCHEDULE,
					MailArchiveRun.Fields.START, 
					MailArchiveRun.Fields.END, 
					MailArchiveRun.Fields.HIGHER_BOUNDARY, 
					MailArchiveRun.Fields.RECURRENT)
			.values(runId.serialize(),
					domainId.get(),
					ArchiveStatus.SUCCESS, 
					DateTime.parse("2014-06-01T00:00:00.000Z").toDate(), 
					DateTime.parse("2014-06-01T00:01:00.000Z").toDate(), 
					DateTime.parse("2014-06-01T00:02:00.000Z").toDate(), 
					DateTime.parse("2014-06-01T00:03:00.000Z").toDate(),
					true)
			.build();
	}
}

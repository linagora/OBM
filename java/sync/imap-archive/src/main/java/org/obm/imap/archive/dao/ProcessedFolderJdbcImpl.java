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

package org.obm.imap.archive.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTimeZone;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.dao.ProcessedFolderJdbcImpl.TABLE.FIELDS;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.JDBCUtils;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProcessedFolderJdbcImpl implements ProcessedFolderDao {

	private final DatabaseConnectionProvider dbcp;
	private final ObmHelper obmHelper;
	private final ImapFolderDao imapFolderDao;
	
	public interface TABLE {
		
		String NAME = "mail_archive_processed_folder";
		
		interface FIELDS {
			String RUN_ID = "mail_archive_processed_folder_run_uuid";
			String FOLDER_ID = "mail_archive_processed_folder_id";
			String START = "mail_archive_processed_folder_start";
			String END = "mail_archive_processed_folder_end";
			String STATUS = "mail_archive_processed_folder_status";
			
			String ALL = Joiner.on(", ").join(RUN_ID, FOLDER_ID, START, END, STATUS);
		}
	}
	
	interface REQUESTS {
		
		String SELECT = String.format(
				"SELECT %s, %s FROM %s, %s WHERE %s = ? AND %s = ? AND %s = %s.id", FIELDS.ALL, ImapFolderJdbcImpl.TABLE.FIELDS.FOLDER, TABLE.NAME, ImapFolderJdbcImpl.TABLE.NAME,
					FIELDS.RUN_ID, ImapFolderJdbcImpl.TABLE.FIELDS.FOLDER,
					FIELDS.FOLDER_ID, ImapFolderJdbcImpl.TABLE.NAME);
		
		String INSERT = String.format(
				"INSERT INTO %s (%s) VALUES (?, (SELECT id FROM %s WHERE %s = ?), ?, ?, ?)", TABLE.NAME, FIELDS.ALL,
					ImapFolderJdbcImpl.TABLE.NAME, ImapFolderJdbcImpl.TABLE.FIELDS.FOLDER);
	}
	
	@Inject
	@VisibleForTesting ProcessedFolderJdbcImpl(DatabaseConnectionProvider dbcp, ObmHelper obmHelper, ImapFolderDao imapFolderDao) {
		this.dbcp = dbcp;
		this.obmHelper = obmHelper;
		this.imapFolderDao = imapFolderDao;
	}

	@Override
	public Optional<ProcessedFolder> get(ArchiveTreatmentRunId runId, ImapFolder imapFolder) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.SELECT)) {

			ps.setString(1, runId.serialize());
			ps.setString(2, imapFolder.getName());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return Optional.of(processedFolder(rs));
			} else {
				return Optional.absent();
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private ProcessedFolder processedFolder(ResultSet rs) throws SQLException {
		return ProcessedFolder.builder()
				.runId(ArchiveTreatmentRunId.from(rs.getString(FIELDS.RUN_ID)))
				.folder(ImapFolder.from(rs.getString(ImapFolderJdbcImpl.TABLE.FIELDS.FOLDER)))
				.start(JDBCUtils.getDateTime(rs, FIELDS.START, DateTimeZone.UTC))
				.end(JDBCUtils.getDateTime(rs, FIELDS.END, DateTimeZone.UTC))
				.status(ArchiveStatus.fromSpecificationValue(rs.getString(FIELDS.STATUS)))
				.build();
	}
	
	@Override
	public void insert(ProcessedFolder processedFolder) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.INSERT)) {

			ImapFolder imapFolder = processedFolder.getFolder();
			if (!imapFolderDao.get(imapFolder.getName()).isPresent()) {
				imapFolderDao.insert(imapFolder);
			}
			
			int idx = 1;
			ps.setString(idx++, processedFolder.getRunId().serialize());
			ps.setString(idx++, imapFolder.getName());
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(processedFolder.getStart()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(processedFolder.getEnd()));
			ps.setObject(idx++, obmHelper.getDBCP().getJdbcObject("mail_archive_status", processedFolder.getStatus().asSpecificationValue()));

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}
}

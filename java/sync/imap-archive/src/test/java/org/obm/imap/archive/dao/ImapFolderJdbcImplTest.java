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
import static org.assertj.guava.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2Destination;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.provisioning.dao.exceptions.DaoException;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

public class ImapFolderJdbcImplTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new DaoTestModule()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/mail_archive_folder.sql"));

	@Inject
	private H2InMemoryDatabase db;
	
	@Inject
	private ImapFolderJdbcImpl imapFolderJdbcImpl;
	
	@Rule
	public FluentExpectedException expectedException = FluentExpectedException.none();
	
	@Before
	public void setUp() {
		Operation operation =
				Operations.sequenceOf(
						Operations.deleteAllFrom(ImapFolderJdbcImpl.TABLE.NAME),
						Operations.insertInto(ImapFolderJdbcImpl.TABLE.NAME)
						.columns(ImapFolderJdbcImpl.TABLE.FIELDS.FOLDER)
						.values("user/usera/Test@mydomain.org")
						.build());

		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}
	
	@Test
	public void getShouldReturnWhenNameMatch() throws Exception {
		String name = "user/usera/Test@mydomain.org";
		Optional<ImapFolder> optionImapFolder = imapFolderJdbcImpl.get(name);
		assertThat(optionImapFolder).isPresent();
		
		ImapFolder imapFolder = optionImapFolder.get();
		assertThat(imapFolder.getName()).isEqualTo(name);
	}
	
	@Test
	public void getShouldReturnAbsentWhenNameDoesntMatch() throws Exception {
		assertThat(imapFolderJdbcImpl.get("unknown")).isAbsent();
	}
	
	@Test
	public void insertShouldReturnMatchingValues() throws Exception {
		ImapFolder expectedImapFolder = ImapFolder.from("user/usera/NewOne@mydomain.org");
		
		imapFolderJdbcImpl.insert(expectedImapFolder);
		ImapFolder imapFolder = imapFolderJdbcImpl.get(expectedImapFolder.getName()).get();
		assertThat(imapFolder).isEqualToComparingFieldByField(expectedImapFolder);
	}
	
	@Test
	public void insertShouldThrowWhenDuplicateRunId() throws Exception {
		ImapFolder imapFolder = ImapFolder.from("user/usera/Test@mydomain.org");
		
		expectedException.expect(DaoException.class);
		
		imapFolderJdbcImpl.insert(imapFolder);
	}
}

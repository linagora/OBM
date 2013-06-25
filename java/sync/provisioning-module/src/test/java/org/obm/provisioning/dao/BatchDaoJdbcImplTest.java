/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.provisioning.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(BatchDaoJdbcImplTest.Env.class)
public class BatchDaoJdbcImplTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");

			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bind(BatchDao.class).to(BatchDaoJdbcImpl.class);
		}

	}

	@Inject
	private BatchDao dao;

	@Rule
	@Inject
	public H2InMemoryDatabase db;

	@Test
	public void testGetWhenBatchNotFound() throws Exception {
		assertThat(dao.get(123)).isNull();
	}

	@Test
	public void testGet() throws Exception {
		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");

		Batch batch = dao.get(1);

		assertThat(batch.getStatus()).isEqualTo(BatchStatus.IDLE);
	}

	@Test(expected = SQLException.class)
	public void testCreateWithNonExistingDomain() throws Exception {
		ObmDomain domain = ObmDomain.builder().id(123).name("nonexisting.domain").build();
		Batch batch = Batch.builder().domain(domain).status(BatchStatus.IDLE).build();

		dao.create(batch);
	}

	@Test
	public void testCreate() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().domain(domain).status(BatchStatus.IDLE).build();
		Batch createdBatch = dao.create(batch);

		assertThat(createdBatch.getId()).isEqualTo(1);
	}

	@Test
	public void testCreateActuallyWritesToDB() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().domain(domain).status(BatchStatus.IDLE).build();

		dao.create(batch);

		ResultSet rs = db.execute("SELECT COUNT(*) FROM Batch");

		rs.next();

		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test
	public void testUpdate() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().id(1).domain(domain).status(BatchStatus.RUNNING).build();

		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");
		Batch updatedBatch = dao.update(batch);

		assertThat(updatedBatch.getStatus()).isEqualTo(BatchStatus.RUNNING);
	}

	@Test
	public void testUpdateActuallyWritesToDB() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().id(1).domain(domain).status(BatchStatus.RUNNING).build();

		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");
		dao.update(batch);

		ResultSet rs = db.execute("SELECT COUNT(*) FROM Batch WHERE id=1 AND status='RUNNING'");

		rs.next();

		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test(expected = BatchNotFoundException.class)
	public void testUpdateWhenBatchDoesntExist() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().id(666).domain(domain).status(BatchStatus.RUNNING).build();

		dao.update(batch);
	}

	@Test
	public void testDelete() throws Exception {
		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");
		dao.delete(1);

		ResultSet rs = db.execute("SELECT COUNT(*) FROM Batch");

		rs.next();

		assertThat(rs.getInt(1)).isEqualTo(0);
	}

	@Test(expected = BatchNotFoundException.class)
	public void testDeleteWhenBatchDoesntExist() throws Exception {
		dao.delete(1);
	}
}

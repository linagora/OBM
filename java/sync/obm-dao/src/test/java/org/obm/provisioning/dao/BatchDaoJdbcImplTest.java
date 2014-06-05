/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.Batch.Builder;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;

@RunWith(GuiceRunner.class)
@GuiceModule(BatchDaoJdbcImplTest.Env.class)
public class BatchDaoJdbcImplTest implements H2TestClass {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			install(new DaoTestModule());
			bind(BatchDao.class).to(BatchDaoJdbcImpl.class);
			bind(OperationDao.class).to(OperationDaoJdbcImpl.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
		}

	}
	
	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}

	@Inject
	private BatchDao dao;

	private final ObmDomain domain = ToolBox.getDefaultObmDomain();

	@Test(expected=BatchNotFoundException.class)
	public void testGetWhenBatchNotFound() throws Exception {
		dao.get(batchId(123), domain);
	}

	@Test
	public void testGet() throws Exception {
		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");
		db.executeUpdate("INSERT INTO batch_operation (status, resource_path, verb, entity_type, batch) VALUES ('IDLE', '/batches/1/users', 'POST', 'USER', 1)");
		db.executeUpdate("INSERT INTO batch_operation_param (param_key, value, operation) VALUES ('p1', 'v1', 1)");
		db.executeUpdate("INSERT INTO batch_operation (status, resource_path, verb, entity_type, batch) VALUES ('IDLE', '/batches/1/groups', 'POST', 'GROUP', 1)");

		Batch batch = dao.get(batchId(1), domain);

		assertThat(batch.getOperations()).hasSize(2);
	}

	@Test(expected = BatchNotFoundException.class)
	public void testGetInOtherDomain() throws Exception {
		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 2)");

		dao.get(batchId(1), domain);
	}

	@Test(expected = DaoException.class)
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

		assertThat(createdBatch.getId()).isEqualTo(batchId(1));
	}

	@Test
	public void testCreateActuallyWritesToDB() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().domain(domain).status(BatchStatus.IDLE).build();

		dao.create(batch);

		ResultSet rs = db.execute("SELECT COUNT(id) FROM Batch");

		rs.next();

		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test
	public void testUpdate() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().id(batchId(1)).domain(domain).status(BatchStatus.RUNNING).build();

		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");
		Batch updatedBatch = dao.update(batch);

		assertThat(updatedBatch.getStatus()).isEqualTo(BatchStatus.RUNNING);
	}

	@Test
	public void testUpdateAlsoUpdatesOperations() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Request request = Request.builder()
				.resourcePath("/batches/1/users")
				.verb(HttpVerb.POST)
				.param("p1", "v1")
				.build();
		Operation.Builder operationBuilder = Operation.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.request(request)
				.entityType(BatchEntityType.USER);
		Operation operation = operationBuilder
				.build();
		Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE);
		Batch batch = batchBuilder.build();

		batch = dao.create(batch);

		dao.addOperation(batch, operation);

		batch = batchBuilder
				.status(BatchStatus.SUCCESS)
				.operation(operationBuilder
						.status(BatchStatus.ERROR)
						.build())
				.build();

		Batch updatedBatch = dao.update(batch);

		assertThat(updatedBatch.getStatus()).isEqualTo(BatchStatus.SUCCESS);
		assertThat(updatedBatch.getOperations().get(0).getStatus()).isEqualTo(BatchStatus.ERROR);
	}

	@Test
	public void testUpdateActuallyWritesToDB() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().id(batchId(1)).domain(domain).status(BatchStatus.RUNNING).build();

		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");
		dao.update(batch);

		ResultSet rs = db.execute("SELECT COUNT(id) FROM batch WHERE id=1 AND status='RUNNING'");

		rs.next();

		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	@Test(expected = BatchNotFoundException.class)
	public void testUpdateWhenBatchDoesntExist() throws Exception {
		ObmDomain domain = ToolBox.getDefaultObmDomain();
		Batch batch = Batch.builder().id(batchId(666)).domain(domain).status(BatchStatus.RUNNING).build();

		dao.update(batch);
	}

	@Test
	public void testDelete() throws Exception {
		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");
		dao.delete(batchId(1));

		ResultSet rs = db.execute("SELECT COUNT(id) FROM batch");

		rs.next();

		assertThat(rs.getInt(1)).isEqualTo(0);
	}

	@Test(expected = BatchNotFoundException.class)
	public void testDeleteWhenBatchDoesntExist() throws Exception {
		dao.delete(batchId(1));
	}

	@Test
	public void testAddOperation() throws Exception {
		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");

		Request request = Request.builder()
				.resourcePath("/batches/1/users")
				.verb(HttpVerb.POST)
				.param("p1", "v1")
				.build();
		Operation operation = Operation.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.request(request)
				.entityType(BatchEntityType.USER)
				.build();

		Batch batch = dao.get(batchId(1), domain);

		dao.addOperation(batch, operation);

		assertThat(dao.get(batch.getId(), domain).getOperations()).isNotEmpty();
	}

	@Test
	public void testAddOperationActuallyWritesToDB() throws Exception {
		db.executeUpdate("INSERT INTO batch (status, domain) VALUES ('IDLE', 1)");

		Request request = Request.builder()
				.resourcePath("/batches/1/users")
				.verb(HttpVerb.POST)
				.param("p1", "v1")
				.build();
		Operation operation = Operation.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.request(request)
				.entityType(BatchEntityType.USER)
				.build();

		Batch batch = dao.get(batchId(1), domain);

		dao.addOperation(batch, operation);

		ResultSet rs = db.execute("SELECT COUNT(id) FROM batch_operation");

		rs.next();

		assertThat(rs.getInt(1)).isEqualTo(1);
	}

	private Batch.Id batchId(Integer id) {
		return Batch.Id.builder().id(id).build();
	}

	private Operation.Id operationId(Integer id) {
		return Operation.Id.builder().id(id).build();
	}
}

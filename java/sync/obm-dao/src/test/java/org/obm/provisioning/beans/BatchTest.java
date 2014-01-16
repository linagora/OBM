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
package org.obm.provisioning.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.provisioning.beans.Operation.Id;

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.ToolBox;

@RunWith(SlowFilterRunner.class)
public class BatchTest {

	@Test(expected = IllegalStateException.class)
	public void testBuildWhenNoDomain() {
		Batch.builder().status(BatchStatus.IDLE).build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildWhenNoStatus() {
		Batch.builder().domain(ToolBox.getDefaultObmDomain()).build();
	}

	@Test
	public void testBuild() {
		Batch batch = Batch.builder().status(BatchStatus.IDLE).domain(ToolBox.getDefaultObmDomain()).build();

		assertThat(batch.getStatus()).isEqualTo(BatchStatus.IDLE);
	}

	@Test
	public void testBatchIdValueOf() {
		assertThat(Batch.Id.valueOf("1")).isEqualTo(Batch.Id.builder().id(1).build());
	}

	@Test(expected = NumberFormatException.class)
	public void testBatchIdValueOfWithNull() {
		Batch.Id.valueOf(null);
	}

	@Test(expected = NumberFormatException.class)
	public void testBatchIdValueOfWithNotANumber() {
		Batch.Id.valueOf("test");
	}

	@Test
	public void testGetOperationsCount() {
		Batch batch = Batch
				.builder()
				.id(Batch.Id.builder().id(1).build())
				.status(BatchStatus.IDLE)
				.domain(ToolBox.getDefaultObmDomain())
				.operation(Operation
						.builder()
						.id(operationId(1))
						.status(BatchStatus.SUCCESS)
						.entityType(BatchEntityType.USER)
						.request(Request
								.builder()
								.resourcePath("/")
								.body("")
								.verb(HttpVerb.POST)
								.build())
						.build())
				.operation(Operation
						.builder()
						.id(Operation.Id.builder().id(2).build())
						.status(BatchStatus.IDLE)
						.entityType(BatchEntityType.USER)
						.request(Request
								.builder()
								.resourcePath("/1")
								.body("")
								.verb(HttpVerb.PUT)
								.build())
						.build())
				.build();

		assertThat(batch.getOperationsCount()).isEqualTo(2);
	}

	@Test
	public void testGetOperationsCountWhenNoOperations() {
		Batch batch = Batch
				.builder()
				.id(Batch.Id.builder().id(1).build())
				.status(BatchStatus.IDLE)
				.domain(ToolBox.getDefaultObmDomain())
				.build();

		assertThat(batch.getOperationsCount()).isEqualTo(0);
	}

	@Test
	public void testGetOperationsDoneCount() {
		Batch batch = Batch
				.builder()
				.id(Batch.Id.builder().id(1).build())
				.status(BatchStatus.IDLE)
				.domain(ToolBox.getDefaultObmDomain())
				.operation(Operation
						.builder()
						.id(operationId(1))
						.status(BatchStatus.SUCCESS)
						.entityType(BatchEntityType.USER)
						.request(Request
								.builder()
								.resourcePath("/")
								.body("")
								.verb(HttpVerb.POST)
								.build())
						.build())
				.operation(Operation
						.builder()
						.id(Operation.Id.builder().id(2).build())
						.status(BatchStatus.IDLE)
						.entityType(BatchEntityType.USER)
						.request(Request
								.builder()
								.resourcePath("/1")
								.body("")
								.verb(HttpVerb.PUT)
								.build())
						.build())
				.build();

		assertThat(batch.getOperationsDoneCount()).isEqualTo(1);
	}

	@Test
	public void testGetOperationsDoneCountWhenNoOperations() {
		Batch batch = Batch
				.builder()
				.id(Batch.Id.builder().id(1).build())
				.status(BatchStatus.IDLE)
				.domain(ToolBox.getDefaultObmDomain())
				.build();

		assertThat(batch.getOperationsDoneCount()).isEqualTo(0);
	}

	@Test
	public void testFrom() {
		Batch batch = Batch
				.builder()
				.id(Batch.Id.builder().id(1).build())
				.status(BatchStatus.IDLE)
				.domain(ToolBox.getDefaultObmDomain())
				.operation(Operation
						.builder()
						.id(operationId(1))
						.status(BatchStatus.SUCCESS)
						.entityType(BatchEntityType.USER)
						.request(Request
								.builder()
								.resourcePath("/")
								.body("")
								.verb(HttpVerb.POST)
								.build())
						.build())
				.build();

		assertThat(Batch.builder().from(batch).build()).isEqualTo(batch);
	}

	@Test
	public void testOperationsInsertionOrderIsMaintained() {
		Operation.Builder opBuilder = Operation
				.builder()
				.status(BatchStatus.SUCCESS)
				.entityType(BatchEntityType.USER)
				.request(Request
						.builder()
						.resourcePath("/")
						.body("")
						.verb(HttpVerb.POST)
						.build());
		Batch batch = Batch
				.builder()
				.id(Batch.Id.builder().id(1).build())
				.status(BatchStatus.IDLE)
				.domain(ToolBox.getDefaultObmDomain())
				.operation(opBuilder.id(operationId(1)).build())
				.operation(opBuilder.id(operationId(2)).build())
				.operation(opBuilder.id(operationId(3)).build())
				.operation(opBuilder.id(operationId(4)).build())
				.operation(opBuilder.id(operationId(5)).build())
				.build();

		assertThat(batch.getOperations()).isEqualTo(ImmutableList.of(
				opBuilder.id(operationId(1)).build(),
				opBuilder.id(operationId(2)).build(),
				opBuilder.id(operationId(3)).build(),
				opBuilder.id(operationId(4)).build(),
				opBuilder.id(operationId(5)).build()));
	}

	@Test
	public void testOperationsAreReplacedWhenReInserted() {
		Operation.Builder opBuilder = Operation
				.builder()
				.status(BatchStatus.SUCCESS)
				.entityType(BatchEntityType.USER)
				.request(Request
						.builder()
						.resourcePath("/")
						.body("")
						.verb(HttpVerb.POST)
						.build());
		Batch batch = Batch
				.builder()
				.id(Batch.Id.builder().id(1).build())
				.status(BatchStatus.IDLE)
				.domain(ToolBox.getDefaultObmDomain())
				.operation(opBuilder.id(operationId(1)).build())
				.operation(opBuilder.id(operationId(2)).build())
				.operation(opBuilder.id(operationId(2)).build())
				.operation(opBuilder.id(operationId(2)).build())
				.build();

		assertThat(batch.getOperations()).isEqualTo(ImmutableList.of(
				opBuilder.id(operationId(1)).build(),
				opBuilder.id(operationId(2)).build()));
	}

	private Id operationId(int id) {
		return Operation.Id.builder().id(id).build();
	}
}

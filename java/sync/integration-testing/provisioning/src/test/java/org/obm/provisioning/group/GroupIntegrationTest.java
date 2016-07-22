/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.provisioning.group;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.getAdminUserJson;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.groupUrl;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.startBatch;

import java.net.URL;

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.provisioning.TestingProvisioningModule;
import org.obm.server.WebServer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class GroupIntegrationTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new TestingProvisioningModule()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "dbInitialScriptGroup.sql"));

	@Inject private H2InMemoryDatabase db;
	@Inject private WebServer server;
	
	private URL baseURL;
	
	@Before
	public void init() throws Exception {
		server.start();
		baseURL = new URL("http", "localhost", server.getHttpPort(), "/");
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void testGetNonExistGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/NotExistingId");
	}

	@Test
	public void testGetGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{" +
					  "\"id\":\"AdminExtId\"," +
					  "\"name\":\"Admin\","    +
					  "\"email\":\"group_admin@obm.org\","    + 
					  "\"description\":\"Admin Group Desc\","  +
					  "\"members\":{"    +
					  "\"users\":[],"         +
					  "\"subgroups\":[]"	 +
					  "}"  +
				"}")).
		when()
			.get("/AdminExtId");
	}

	@Test
	public void testGetGroupWithUsers() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{" +
					"\"id\":\"GroupWithUsers\"," +
					"\"name\":\"GroupWithUsers\"," +
					"\"email\":\"group_with_user@obm.org\"," +
					"\"description\":\"Group With Users\"," +
					"\"members\":{" +
						"\"users\":[" +
							getAdminUserJson() +
						"]," +
						"\"subgroups\":[]" +
					"}"+
				"}")).
		when()
			.get("/GroupWithUsers");
	}



	@Test
	public void testGetGroupWithTwoSubgroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
					"{" +
							"\"id\":\"GroupA\"," +
							"\"name\":\"GroupA\"," +
							"\"email\":\"groupA@obm.org\"," +
							"\"description\":\"Group A\"," +
							"\"members\":{" +
							"\"users\":[]," +
							"\"subgroups\":[" +
									"{" +
									"\"id\":\"GroupAA\"," +
									"\"name\":\"GroupAA\"," +
									"\"email\":\"groupAA@obm.org\"," +
									"\"description\":\"Group AA\"," +
									"\"members\":{" +
											"\"users\":[]," +
											"\"subgroups\":[]" +
										"}" +
									"}," +
									"{" +
									  "\"id\":\"GroupAB\"," +
									  "\"name\":\"GroupAB\","    +
									  "\"email\":\"groupAB@obm.org\","    + 
									  "\"description\":\"Group AB\","  +
									  "\"members\":{"    +
											  "\"users\":[],"         +
											  "\"subgroups\":[]"	 +
										  "}" +
									"}" +
							"]" +
						"}"+
					"}")).
		when()
			.get("/GroupA?expandDepth=1");
	}
	
	@Test
	public void testGetUsersFromGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("[" + getAdminUserJson() + "]")).
		when()
			.get("/GroupWithUsers/users");
	}

	@Test
	public void testGetUserOfNonExistGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/NotExistingId/users");
	}

	@Test
	public void testGetUsersFromGroupWhoHaveNoUser() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"[]")).
		when()
			.get("/AdminExtId/users");
	}

	@Test
	public void testGetSubGroupOfNonExistGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.get("/NotExistingId/subgroups");
	}

	@Test
	public void testGetSubGroupWithUsers() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"[{" +
					"\"id\":\"GroupWithUsers\"," +
					"\"name\":\"GroupWithUsers\"," +
					"\"email\":\"group_with_user@obm.org\"," +
					"\"description\":\"Group With Users\"," +
					"\"members\":{" +
						"\"users\":[" +
							getAdminUserJson() +
						"]," +
						"\"subgroups\":[]" +
					"}"+
				"}]")).
		when()
			.get("/GroupWhoSubgroupHaveUser/subgroups");
	}

	@Test
	public void testGetGroupWhoHaveSubgroupWithDefaultExpandDepth() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{" +
					"\"id\":\"GroupWhoSubgroupHaveUser\"," +
					"\"name\":\"GroupWhoSubgroupHaveUser\"," +
					"\"email\":\"group_with_subgroup@obm.org\"," +
					"\"description\":\"Group Who Subgroup Have User\"," +
					"\"members\":{" +
						"\"users\":[]," +
						"\"subgroups\":[]" +
					"}" +
				"}")).
		when()
			.get("/GroupWhoSubgroupHaveUser");
	}

	@Test
	public void testGetGroupWhoHaveSubgroupWithOneExpandDepth() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{" +
					"\"id\":\"GroupWhoSubgroupHaveUser\"," +
					"\"name\":\"GroupWhoSubgroupHaveUser\"," +
					"\"email\":\"group_with_subgroup@obm.org\"," +
					"\"description\":\"Group Who Subgroup Have User\"," +
					"\"members\":{" +
					"\"users\":[]," +
					"\"subgroups\":[" +
							"{" +
							"\"id\":\"GroupWithUsers\"," +
							"\"name\":\"GroupWithUsers\"," +
							"\"email\":\"group_with_user@obm.org\"," +
							"\"description\":\"Group With Users\"," +
							"\"members\":{" +
								"\"users\":[" +
									getAdminUserJson() +
								"]," +
								"\"subgroups\":[]" +
							"}" +
						"}" +
					"]" +
				"}"+
			"}")).
		when()
			.get("/GroupWhoSubgroupHaveUser?expandDepth=1");
	}

	@Test
	public void testGetGroupWhoHaveSubgroupWithTwoExpandDepth() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{" +
					"\"id\":\"GroupWhoSubgroupHaveUser\"," +
					"\"name\":\"GroupWhoSubgroupHaveUser\"," +
					"\"email\":\"group_with_subgroup@obm.org\"," +
					"\"description\":\"Group Who Subgroup Have User\"," +
					"\"members\":{" +
						"\"users\":[]," +
						"\"subgroups\":[" +
							"{" +
								"\"id\":\"GroupWithUsers\"," +
								"\"name\":\"GroupWithUsers\"," +
								"\"email\":\"group_with_user@obm.org\"," +
								"\"description\":\"Group With Users\"," +
								"\"members\":{" +
									"\"users\":[" +
										getAdminUserJson() +
									"]," +
									"\"subgroups\":[]" +
								"}" +
							"}" + 
						"]" +
					"}" +
				"}")).
		when()
			.get("/GroupWhoSubgroupHaveUser?expandDepth=2");
	}

	@Test
	public void testGetGroupWhoHaveSubgroupWithInfiniteExpandDepth() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{" +
					"\"id\":\"GroupWhoSubgroupHaveUser\"," +
					"\"name\":\"GroupWhoSubgroupHaveUser\"," +
					"\"email\":\"group_with_subgroup@obm.org\"," +
					"\"description\":\"Group Who Subgroup Have User\"," +
						"\"members\":{" +
							"\"users\":[]," +
							"\"subgroups\":[" +
							"{" +
								"\"id\":\"GroupWithUsers\"," +
								"\"name\":\"GroupWithUsers\"," +
								"\"email\":\"group_with_user@obm.org\"," +
								"\"description\":\"Group With Users\"," +
								"\"members\":{" +
									"\"users\":[" +
									getAdminUserJson() +
									"]," +
									"\"subgroups\":[]" +
								"}" +
							"}" + 
						"]" +
					"}" +
				"}")).
		when()
			.get("/GroupWhoSubgroupHaveUser?expandDepth=-1");
	}

	@Test
	public void testGetGroupWithSubgroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"{" +
					"\"id\":\"GroupWithSubGroup\"," +
					"\"name\":\"GroupWithSubGroup\","    + 
					"\"email\":\"group_with_subgroup@obm.org\"," +
					"\"description\":\"Group With SubGroup\","  +
					"\"members\":{"    +
					"\"users\":[],"         +
					"\"subgroups\":[" +
							"{" +
								"\"id\":\"AdminExtId\"," +
								"\"name\":\"Admin\"," +
								"\"email\":\"group_admin@obm.org\"," +
								"\"description\":\"Admin Group Desc\"," +
								"\"members\":{" +
									"\"users\":[]," +
									"\"subgroups\":[]" +
								"}" +
							"}" + 
						"]" +
					"}" +
				"}")).
		when()
			.get("/GroupWithSubGroup?expandDepth=-1");
	}

	@Test
	public void testCreateGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		String body = "{" +
				"\"id\":\"CreatedGroup\"," +
				"\"name\":\"CreatedGroup\"," + 
				"\"description\":\"Created by provionning Group\"," +
			"}";
				
		given()
			.auth().basic("admin0@global.virt", "admin0")
			.content(body).contentType(ContentType.JSON).
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/groups");
				
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":["
						+ "{\"status\":\"IDLE\","
							+ "\"entityType\":\"GROUP\",\"entity\":{"
								+ "\"id\":\"CreatedGroup\","
								+ "\"name\":\"CreatedGroup\","
								+ "\"description\":\"Created by provionning Group\","
							+ "},"
							+ "\"operation\":\"POST\","
							+ "\"error\":null"
						+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testModifyGroupByPutMethod() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		String body = "{" +
				"\"id\":\"AdminExtId\"," +
				"\"name\":\"Put Info\","    + 
				"\"description\":\"Put Info to AdminExtId\","  +
				"\"members\":{"    +
				"\"users\":[],"         +
				"\"subgroups\":[]"	 +
				"}"  +
			"}";

		given()
			.auth().basic("admin0@global.virt", "admin0")
			.content(body).contentType(ContentType.JSON).
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.put("/groups/AdminExtId");
				
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":["
						+ "{\"status\":\"IDLE\","
							+ "\"entityType\":\"GROUP\",\"entity\":{"
								+ "\"id\":\"AdminExtId\","
								+ "\"name\":\"Put Info\","
								+ "\"description\":\"Put Info to AdminExtId\","
								+ "\"members\":{"
									+ "\"users\":[],"
									+ "\"subgroups\":[]"
								+ "}"
							+ "},"
							+ "\"operation\":\"PUT\","
							+ "\"error\":null"
						+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testModifyGroupByPatchMethod() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		String body = "{" + "\"description\":\"Patched AdminExtId group\"" + "}";
				
		given()
			.auth().basic("admin0@global.virt", "admin0")
			.content(body).contentType(ContentType.JSON).
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.patch("/groups/AdminExtId");
				
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":["
						+ "{\"status\":\"IDLE\","
							+ "\"entityType\":\"GROUP\",\"entity\":{"
								+ "\"description\":\"Patched AdminExtId group\""
							+ "},"
							+ "\"operation\":\"PATCH\","
							+ "\"error\":null"
						+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testDeleteGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.delete("/groups/AdminExtId");

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":[{"
						+ "\"status\":\"IDLE\","
						+ "\"entityType\":\"GROUP\","
						+ "\"entity\":{\"id\":\"AdminExtId\"},"
						+ "\"operation\":\"DELETE\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testAddUserToGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.put("/groups/GroupWithUsers/users/User1");

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":[{"
						+ "\"status\":\"IDLE\","
						+ "\"entityType\":\"USER_MEMBERSHIP\","
						+ "\"entity\":null,"
						+ "\"operation\":\"PUT\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testDeleteUserFromGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.delete("/groups/GroupWithUsers/users/User1");

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":[{"
						+ "\"status\":\"IDLE\","
						+ "\"entityType\":\"USER_MEMBERSHIP\","
						+ "\"entity\":{\"userId\":\"User1\",\"groupId\":\"GroupWithUsers\"},"
						+ "\"operation\":\"DELETE\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testAddSubgroupToGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.put("/groups/GroupWithUsers/subgroups/AdminExtId");

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":[{"
						+ "\"status\":\"IDLE\","
						+ "\"entityType\":\"GROUP_MEMBERSHIP\","
						+ "\"entity\":null,"
						+ "\"operation\":\"PUT\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testDeleteSubgroupFromGroup() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.delete("/groups/GroupWithUsers/subgroups/AdminExtId");

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":[{"
						+ "\"status\":\"IDLE\","
						+ "\"entityType\":\"GROUP_MEMBERSHIP\","
						+ "\"entity\":{\"subgroupId\":\"AdminExtId\",\"groupId\":\"GroupWithUsers\"},"
						+ "\"operation\":\"DELETE\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	public void testListPublicGroups() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		RestAssured.baseURI = groupUrl(baseURL, obmDomainUuid);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString(
				"[" +
					"{" +
						"\"id\":\"GroupAB\"," +
						"\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/groups/GroupAB\"" +
					"}," +
					"{" +
						"\"id\":\"GroupAA\"," +
						"\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/groups/GroupAA\"" +
					"}," +
					"{" +
						"\"id\":\"GroupA\"," +
						"\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/groups/GroupA\"" +
					"}," +
					"{" +
						"\"id\":\"AdminExtId\"," +
						"\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/groups/AdminExtId\"" +
					"}," +
					"{" +
						"\"id\":\"GroupWithSubGroup\"," +
						"\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/groups/GroupWithSubGroup\"" +
					"}," +
					"{" +
						"\"id\":\"GroupWhoSubgroupHaveUser\"," +
						"\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/groups/GroupWhoSubgroupHaveUser\"" +
					"}," +
					"{" +
						"\"id\":\"GroupWithUsers\"," +
						"\"url\":\"/ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6/groups/GroupWithUsers\"" +
					"}" +
				"]")).
		when()
			.get("/");
	}
}

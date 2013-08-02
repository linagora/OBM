/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.batchUrl;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.domainUrl;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.groupUrl;

import java.io.File;
import java.net.URL;

import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.provisioning.ProvisioningArchiveUtils;
import org.obm.push.arquillian.ManagedTomcatSlowGuiceArquillianRunner;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import fr.aliacom.obm.common.domain.ObmDomainUuid;


@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class GroupIntegrationTest {

	@Test
	@RunAsClient
	public void testGetNonExistGroup(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetGroup(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetGroupWithUsers(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetUsersFromGroup(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetUserOfNonExistGroup(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetUsersFromGroupWhoHaveNoUser(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetSubGroupOfNonExistGroup(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetSubGroupWithUsers(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetGroupWhoHaveSubgroupWithDefaultExpandDepth(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetGroupWhoHaveSubgroupWithOneExpandDepth(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetGroupWhoHaveSubgroupWithTwoExpandDepth(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetGroupWhoHaveSubgroupWithInfiniteExpandDepth(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testGetGroupWithSubgroup(@ArquillianResource URL baseURL) {
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
	@RunAsClient
	public void testCreateGroup(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
	@RunAsClient
	public void testModifyGroupByPutMethod(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
	@RunAsClient
	public void testModifyGroupByPatchMethod(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
	@RunAsClient
	public void testDeleteGroup(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
						+ "\"entity\":null,"
						+ "\"operation\":\"DELETE\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	@RunAsClient
	public void testAddUserToGroup(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
	@RunAsClient
	public void testDeleteUserFromGroup(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
						+ "\"entity\":null,"
						+ "\"operation\":\"DELETE\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	@Test
	@RunAsClient
	public void testAddSubgroupToGroup(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
	@RunAsClient
	public void testDeleteSubgroupFromGroup(@ArquillianResource URL baseURL) {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = getBatchId(baseURL, obmDomainUuid);
		RestAssured.baseURI = batchUrl(baseURL, obmDomainUuid, batchId);

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
						+ "\"entity\":null,"
						+ "\"operation\":\"DELETE\","
						+ "\"error\":null"
					+ "}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}

	private static String getBatchId(@ArquillianResource URL baseURL, ObmDomainUuid obmDomainUuid) {
		RestAssured.baseURI = domainUrl(baseURL, obmDomainUuid);

		  String batchId =  given()
				.auth().basic("admin0@global.virt", "admin0")
			.post("/batches").jsonPath().getString("id");

		return batchId;
	}
	
	private static String getAdminUserJson(){
		return "{\"id\":\"Admin0ExtId\",\"login\":\"admin0\",\"lastname\":\"Lastname\",\"profile\":\"admin\","
				+ "\"firstname\":\"Firstname\",\"commonname\":null,\"password\":\"admin0\","
				+ "\"kind\":null,\"title\":null,\"description\":null,\"company\":null,\"service\":null,"
				+ "\"direction\":null,\"addresses\":[],\"town\":null,\"zipcode\":null,\"business_zipcode\":null,"
				+ "\"country\":\"0\",\"phones\":[],\"mobile\":null,\"faxes\":[],\"mail_quota\":\"0\","
				+ "\"mail_server\":null,\"mails\":[\"admin0@test.tlse.lng\"],\"timecreate\":null,\"timeupdate\":null,"
				+ "\"groups\":[\"Not implemented yet\"]}";
	}

	@Deployment
	public static WebArchive createDeployment() throws Exception {
		return ProvisioningArchiveUtils.buildWebArchive(
				new File(ClassLoader.getSystemResource("dbInitialScriptGroup.sql").toURI()));
	}
}

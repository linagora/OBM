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

package org.obm.imap.archive.treatment;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.obm.imap.archive.DBData.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.rules.TemporaryFolder;
import org.obm.dao.utils.H2Destination;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.imap.archive.DatabaseOperations;
import org.obm.imap.archive.Expectations;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.beans.ScopeUser;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.icegreen.greenmail.imap.AuthorizationException;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.SimpleStoredMessage;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.aliacom.obm.common.user.UserExtId;

public class TreatmentStepdefs {
	
	@Inject WebServer server;
	@Inject GreenMail imapServer;
	@Inject H2InMemoryDatabase db;
	@Inject TemporaryFolder temporaryFolder;
	@Inject ClientDriverRule driver;

	private Expectations expectations;
	private DomainConfiguration.Builder configurationBuilder;
	private GreenMailUser adminUser;
	private List<GreenMailUser> users;
	private MailFolder imapFolder;
	
	@Before
	public void setup() throws Exception {
		configurationBuilder = DomainConfiguration.builder()
				.domain(domain)
				.archiveMainFolder("arChive");
		
		expectations = new Expectations(driver);
		initDb();
		initGreenMail();
		
		server.start();
		users = Lists.newArrayList();
	}

	private void initDb() throws Exception {
		db.resetDatabase();
		db.importSchema("sql/initial.sql");
		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), Operations.sequenceOf(DatabaseOperations.cleanDB()));
		dbSetup.launch();
	}
	
	private void initGreenMail() throws FolderException, AuthorizationException {
		imapServer.start();
		adminUser = createUser("cyrus", "cyrus");
	}
	
	private GreenMailUser createUser(String login, String password) throws FolderException, AuthorizationException {
		GreenMailUser user = imapServer.setAdminUser(login, password);
		imapServer.getManagers().getImapHostManager().deleteMailbox(user, "INBOX");
		return user;
	}

	@After
	public void tearDown() throws Exception {
		System.setProperty("testingMode", "false");
		server.stop();
		
		adminUser.delete();
		for (GreenMailUser user : users) {
			user.delete();
		}
		imapServer.stop();
		
		db.closeConnections();
	}
	
	@Given("configuration state is \"(.*?)\"")
	public void configurationState(String state) {
		configurationBuilder.state(ConfigurationState.valueOf(state));
	}

	@Given("configuration repeat kind is set to \"(.*?)\" at (\\d+):(\\d+)")
	public void configurationRepeatKind(String repeatKind, int hour, int minute) {
		configurationBuilder.schedulingConfiguration(SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
						.repeat(RepeatKind.valueOf(repeatKind))
						.dayOfWeek(DayOfWeek.MONDAY)
						.dayOfMonth(DayOfMonth.last())
						.dayOfYear(DayOfYear.of(365))
						.build())
				.time(new LocalTime(hour, minute))
				.build());
	}

	@Given("move feature is enabled")
	public void configurationMove() {
		configurationBuilder.moveEnabled(true);
	}
	
	@Given("configuration excludes users?")
	public void configurationExcludeUsers(Map<String, String> users) {
		configurationBuilder.scopeIncludes(false)
			.scopeUsers(usersMapToScopeUserList(users));
	}
	
	@Given("configuration includes users?")
	public void configurationIncludeUsers(Map<String, String> users) {
		configurationBuilder.scopeIncludes(true)
			.scopeUsers(usersMapToScopeUserList(users));
	}

	private ArrayList<ScopeUser> usersMapToScopeUserList(Map<String, String> users) {
		return Lists.newArrayList(
			Maps.transformEntries(users, new Maps.EntryTransformer<String, String, ScopeUser>() {

				@Override
				public ScopeUser transformEntry(String user, String extId) {
					return ScopeUser.builder()
							.login(user)
							.id(UserExtId.valueOf(extId))
							.build();
				}
		}).values());
	}
	
	@Given("a user \"(.*?)\" with \"(.*?)\" imap folders?")
	public void createUserWithFolder(String user, List<String> imapFolders) throws Exception {
		GreenMailUser greenMailUser = createUser(user, user);
		users.add(greenMailUser);
		
		for (String imapFolder : imapFolders) {
			this.imapFolder = imapServer.getManagers().getImapHostManager().createMailbox(greenMailUser, imapFolder);
		}
	}

	@Given("this user has (\\d+) mails? at \"(.*?)\" in this folder with subject \"(.*?)\"")
	public void appendMails(int numberOfMails, String internalDate, String subject) throws Exception {
		for (int i = 0; i < numberOfMails; i++) {
			imapFolder.store(GreenMailUtil.buildSimpleMessage("from@" + domain.getName(), subject, "message", imapServer.getSmtp().getServerSetup()), 
					DateTime.parse(internalDate).toDate());
		}
	}

	@Given("current date is \"(.*?)\"")
	public void currentDate(String currentDate) {
		given()
			.port(server.getHttpPort())
			.body(currentDate).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.put("/imap-archive/testing/date");
	}

	@When("admin launches an immediate treatment")
	public void adminLaunchesAnImmediateTreatment() throws Exception {
		expectations
			.expectTrustedLogin(domain)
			.expectTrustedLogin(domain)
			.expectTrustedLogin(domain);
		
		putConfiguration();
		
		UUID expectedRunId = launchImmediately();
		
		waitForTheEnd(expectedRunId, 1);
	}

	private void putConfiguration() {
		given()
			.auth().basic("admin@" + domain.getName(), "trust3dToken")
			.port(server.getHttpPort())
			.contentType(ContentType.JSON)
			.body(DomainConfigurationDto.from(configurationBuilder.build())).
		expect()
			.header("Location", endsWith("/imap-archive/service/v1/domains/" + domain.getUuid().get() + "/configuration"))
			.statusCode(Status.CREATED.getStatusCode()).
		when()
			.put("/imap-archive/service/v1/domains/" + domain.getUuid().get() + "/configuration");
	}

	private UUID launchImmediately() {
		UUID expectedRunId = TestImapArchiveModules.uuid2;
		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic("admin@" + domain.getName(), "trust3dToken")
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.REAL_RUN).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domain.getUuid().get() + "/treatments/" + expectedRunId.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domain.getUuid().get() + "/treatments");
		return expectedRunId;
	}

	private void waitForTheEnd(UUID expectedRunId, long sleepTimeInSeconds) throws InterruptedException {
		Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepTimeInSeconds, TimeUnit.SECONDS));
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@" + domain.getName(), "trust3dToken")
			.queryParam("live_view", true)
			.contentType(ContentType.JSON).
		expect()
			.body(containsString("Starting IMAP Archive in REAL_RUN for domain mydomain.org")).
		when()
			.get("/imap-archive/service/v1/domains/" + domain.getUuid().get() + "/treatments/" + expectedRunId.toString() + "/logs");
	}
	
	@Then("(\\d+) mails? should be archived in the \"(.*?)\" imap folder with subject \"(.*?)\"")
	public void mailsShouldBeArchivedInFolder(int numberOfArchivedEmails, String archiveFolderName, String subject) throws Exception {
		List<SimpleStoredMessage> messages = mailsShouldBeInFolder(numberOfArchivedEmails, archiveFolderName);
		for (SimpleStoredMessage message : messages) {
			assertThat(message.getMimeMessage().getSubject()).isEqualTo(subject);
		}
	}
	
	@Then("(\\d+) mails? should be in the \"(.*?)\" imap folder")
	public List<SimpleStoredMessage> mailsShouldBeInFolder(int numberOfArchivedEmails, String folderName) throws Exception {
		MailFolder archivedFolder = imapServer.getManagers().getImapHostManager().getFolder(adminUser, folderName);
		assertThat(archivedFolder).isNotNull();
		
		List<SimpleStoredMessage> messages = archivedFolder.getMessages();
		assertThat(messages).hasSize(numberOfArchivedEmails);
		return messages;
	}
	
	@Then("this user imap folders should contain (\\d+) mails?")
	public void userHasMails(int numberOfEmails) {
		List<SimpleStoredMessage> messages = imapServer.getManagers().getImapHostManager().getAllMessages(adminUser);
		assertThat(messages).hasSize(numberOfEmails);
	}
	
	@Then("an archive treatment has been processed by the scheduler after (\\d+) second")
	public void processShouldBeFired(int sleepTimeInSeconds) throws Exception {
		expectations
			.expectTrustedLogin(domain)
			.expectTrustedLogin(domain);
		
		putConfiguration();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		waitForTheEnd(expectedRunId, sleepTimeInSeconds);
	}
	
	@Then("^imap folder \"(.*?)\" doesn't exists$")
	public void imapFolderDoesntExists(String folderName) throws Exception {
		MailFolder archivedFolder = imapServer.getManagers().getImapHostManager().getFolder(adminUser, folderName);
		assertThat(archivedFolder).isNull();
	}
}

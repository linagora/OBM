/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.mail;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.MSEmailChanges;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.bean.ms.MSEmailMetadata;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.mail.bean.Email;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(SlowFilterRunner.class)
public class EmailChangesFetcherImplTest {

	private int collectionId;
	private String collectionPath;
	private UserDataRequest udr;

	private IMocksControl mocks;
	private MSEmailFetcher msEmailFetcher;
	private List<BodyPreference> bodyPreferences;

	@Before
	public void setUp() {
		collectionId = 385;
		collectionPath = "obm:\\\\login@domain\\email\\INBOX";
		udr = new UserDataRequest(null,  null, null);
		bodyPreferences = ImmutableList.<BodyPreference>of();

		mocks = createControl();
		msEmailFetcher = mocks.createMock(MSEmailFetcher.class); 
	}
	
	@Test(expected=NullPointerException.class)
	public void onNullChanges() throws Exception {
		mocks.replay();
		new EmailChangesFetcherImpl(msEmailFetcher).fetch(
				udr, collectionId, collectionPath, bodyPreferences, null);
	}
	
	@Test
	public void onEmptyChanges() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder().build();

		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemChanges()).isEmpty();
		assertThat(result.getItemDeletions()).isEmpty();
	}

	@Test
	public void onOneAddition() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder()
				.additions(ImmutableSet.of(
						Email.builder().uid(33).read(false).date(date("2004-12-13T21:39:45Z")).build()))
				.build();

		UidMSEmail emailChangesData = UidMSEmail.uidBuilder()
				.uid(33)
				.email(MSEmail.builder()
					.read(false)
					.header(MSEmailHeader.builder()
							.subject("a subject")
							.from(new MSAddress("from@domain.org"))
							.to(new MSAddress("to@domain.org"))
							.date(date("2008-02-03T20:37:05Z"))
							.build())
					.body(MSEmailBody.builder()
							.bodyType(MSEmailBodyType.PlainText)
							.charset(Charsets.UTF_8)
							.estimatedDataSize(1000)
							.mimeData(new SerializableInputStream(new ByteArrayInputStream("mail data".getBytes())))
							.truncated(false)
							.build())
					.build())
				.build();
		
		expect(msEmailFetcher.fetch(udr, collectionId, collectionPath, ImmutableList.of(33l), bodyPreferences))
			.andReturn(ImmutableList.of(emailChangesData));
		
		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemDeletions()).isEmpty();
		assertThat(result.getItemChanges()).containsOnly(
				new ItemChangeBuilder()
					.serverId(collectionId + ":33")
					.withNewFlag(true)
					.withApplicationData(emailChangesData)
					.build());
	}

	@Test
	public void onTwoAdditions() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder()
				.additions(ImmutableSet.of(
						Email.builder().uid(33).read(false).date(date("2004-12-13T21:39:45Z")).build(),
						Email.builder().uid(156).read(false).date(date("2004-10-10T21:39:45Z")).build()))
				.build();
		

		UidMSEmail email1ChangeData = UidMSEmail.uidBuilder()
				.uid(33)
				.email(MSEmail.builder()
					.read(false)
					.header(MSEmailHeader.builder()
							.subject("a subject")
							.from(new MSAddress("from@domain.org"))
							.to(new MSAddress("to@domain.org"))
							.date(date("2008-02-03T20:37:05Z"))
							.build())
					.body(MSEmailBody.builder()
							.bodyType(MSEmailBodyType.PlainText)
							.charset(Charsets.UTF_8)
							.estimatedDataSize(1000)
							.mimeData(new SerializableInputStream(new ByteArrayInputStream("mail data".getBytes())))
							.truncated(false)
							.build())
					.build())
				.build();
		
		UidMSEmail email2ChangeData = UidMSEmail.uidBuilder()
				.uid(156)
				.email(MSEmail.builder()
					.read(false)
					.header(MSEmailHeader.builder()
							.subject("a subject2")
							.from(new MSAddress("from2@domain.org"))
							.to(new MSAddress("to2@domain.org"))
							.date(date("2008-02-03T20:37:05Z"))
							.build())
					.body(MSEmailBody.builder()
							.bodyType(MSEmailBodyType.PlainText)
							.charset(Charsets.UTF_8)
							.estimatedDataSize(1000)
							.mimeData(new SerializableInputStream(new ByteArrayInputStream("mail data2".getBytes())))
							.truncated(false)
							.build())
					.build())
				.build();
		
		expect(msEmailFetcher.fetch(udr, collectionId, collectionPath, ImmutableList.of(33l, 156l), bodyPreferences))
			.andReturn(ImmutableList.of(email1ChangeData, email2ChangeData));
		
		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemDeletions()).isEmpty();
		assertThat(result.getItemChanges()).containsOnly(
				new ItemChangeBuilder()
					.serverId(collectionId + ":33")
					.withNewFlag(true)
					.withApplicationData(email1ChangeData)
					.build(),
				new ItemChangeBuilder()
					.serverId(collectionId + ":156")
					.withNewFlag(true)
					.withApplicationData(email2ChangeData)
					.build());
	}

	@Test
	public void onOneChange() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder()
				.changes(ImmutableSet.of(
						Email.builder().uid(33).read(false).date(date("2004-12-13T21:39:45Z")).build()))
				.build();
		
		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemDeletions()).isEmpty();
		assertThat(result.getItemChanges()).containsOnly(
				new ItemChangeBuilder()
					.serverId(collectionId + ":33")
					.withNewFlag(false)
					.withApplicationData(new MSEmailMetadata(false))
					.build());
	}

	@Test
	public void onTwoChanges() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder()
				.changes(ImmutableSet.of(
						Email.builder().uid(33).read(false).date(date("2004-12-13T21:39:45Z")).build(),
						Email.builder().uid(156).read(true).date(date("2004-10-10T21:39:45Z")).build()))
				.build();
		
		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemDeletions()).isEmpty();
		assertThat(result.getItemChanges()).containsOnly(
				new ItemChangeBuilder()
					.serverId(collectionId + ":33")
					.withApplicationData(new MSEmailMetadata(false))
					.withNewFlag(false)
					.build(),
				new ItemChangeBuilder()
					.serverId(collectionId + ":156")
					.withApplicationData(new MSEmailMetadata(true))
					.withNewFlag(false)
					.build());
	}

	@Test
	public void onOneDeletion() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder()
				.deletions(ImmutableSet.of(
						Email.builder().uid(33).read(false).date(date("2004-12-13T21:39:45Z")).build()))
				.build();
		
		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemChanges()).isEmpty();
		assertThat(result.getItemDeletions()).containsOnly(
				ItemDeletion.builder()
					.serverId(collectionId + ":33")
					.build());
	}

	@Test
	public void onTwoDeletions() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder()
				.deletions(ImmutableSet.of(
						Email.builder().uid(33).read(false).date(date("2004-12-13T21:39:45Z")).build(),
						Email.builder().uid(654).read(false).date(date("2006-02-03T21:39:45Z")).build()))
				.build();
		
		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemChanges()).isEmpty();
		assertThat(result.getItemDeletions()).containsOnly(
				ItemDeletion.builder()
					.serverId(collectionId + ":33")
					.build(),
				ItemDeletion.builder()
					.serverId(collectionId + ":654")
					.build());
	}

	@Test
	public void onOneDeletionOneChangeOneAddition() throws Exception {
		EmailChanges emailChanges = EmailChanges.builder()
				.changes(ImmutableSet.of(
						Email.builder().uid(33).read(false).date(date("2004-12-13T21:39:48Z")).build()))
				.deletions(ImmutableSet.of(
						Email.builder().uid(39).read(true).date(date("2012-10-10T11:49:45Z")).build()))
				.additions(ImmutableSet.of(
						Email.builder().uid(15).read(false).date(date("2008-02-03T20:37:05Z")).build()))
				.build();
		
		UidMSEmail emailAddedData = UidMSEmail.uidBuilder()
				.uid(15)
				.email(MSEmail.builder()
					.read(false)
					.header(MSEmailHeader.builder()
							.subject("a subject2")
							.from(new MSAddress("from2@domain.org"))
							.to(new MSAddress("to2@domain.org"))
							.date(date("2008-02-03T20:37:05Z"))
							.build())
					.body(MSEmailBody.builder()
							.bodyType(MSEmailBodyType.HTML)
							.charset(Charsets.UTF_8)
							.estimatedDataSize(1000)
							.mimeData(new SerializableInputStream(new ByteArrayInputStream("mail data2".getBytes())))
							.truncated(false)
							.build())
					.build())
				.build();
		expect(msEmailFetcher.fetch(udr, collectionId, collectionPath, ImmutableList.of(15l), bodyPreferences))
			.andReturn(ImmutableList.of(emailAddedData));
		
		mocks.replay();
		MSEmailChanges result = 
				new EmailChangesFetcherImpl(msEmailFetcher).fetch(
						udr, collectionId, collectionPath, bodyPreferences, emailChanges);
		mocks.verify();
		
		assertThat(result.getItemChanges()).containsOnly(
				new ItemChangeBuilder()
					.serverId(collectionId + ":33")
					.withApplicationData(new MSEmailMetadata(false))
					.withNewFlag(false)
					.build(),
				new ItemChangeBuilder()
					.serverId(collectionId + ":15")
					.withNewFlag(true)
					.withApplicationData(emailAddedData)
					.build());
		assertThat(result.getItemDeletions()).containsOnly(
				ItemDeletion.builder()
					.serverId(collectionId + ":39")
					.build());
	}
}

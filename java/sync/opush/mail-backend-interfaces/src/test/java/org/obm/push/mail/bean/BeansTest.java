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
package org.obm.push.mail.bean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.mime.BodyParam;
import org.obm.push.mail.mime.BodyParams;
import org.obm.push.mail.mime.ContentType;
import org.obm.push.mail.mime.IMimePart;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;
import org.obm.sync.bean.EqualsVerifierUtils;
import org.obm.sync.bean.EqualsVerifierUtils.EqualsVerifierBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(SlowFilterRunner.class)
public class BeansTest {

	private EqualsVerifierUtils equalsVerifierUtilsTest;
	
	@Before
	public void init() {
		equalsVerifierUtilsTest = new EqualsVerifierUtils();
	}
	
	@Test
	public void test() {
		ImmutableList<Class<?>> list = 
				ImmutableList.<Class<?>>builder()
					.add(Address.class)
					.add(BodyParam.class)
					.add(Envelope.class)
					.add(FastFetch.class)
					.add(ListInfo.class)
					.add(MailboxFolder.class)
					.add(MailboxFolders.class)
					.add(MimeAddress.class)
					.add(Email.class)
					.add(Snapshot.class)
					.add(SnapshotKey.class)
					.add(MessageSet.class)
					.add(WindowingIndexKey.class)
					.build();
		equalsVerifierUtilsTest.test(list);
		
		EqualsVerifierBuilder.builder()
			.equalsVerifiers(ImmutableList.<Class<?>>of(ContentType.class))
			.prefabValue(BodyParams.class, 
					BodyParams.builder().add(new BodyParam("white", "wine")).build(),
					BodyParams.builder().add(new BodyParam("blond", "beer")).build())
			.verify();

		EqualsVerifierBuilder.builder()
			.equalsVerifiers(ImmutableList.<Class<?>>of(BodyParams.class))
			.prefabValue(ImmutableMap.class, 
					ImmutableMap.of("key", "value"),
					ImmutableMap.of("first", "second"))
			.verify();

		EqualsVerifierBuilder.builder()
			.equalsVerifiers(ImmutableList.<Class<?>>of(
					MimePart.class))
			.prefabValue(BodyParams.class, 
					BodyParams.builder().add(new BodyParam("white", "wine")).build(),
					BodyParams.builder().add(new BodyParam("blond", "beer")).build())
			.withSuperClass(true)
			.verify();
		
		EqualsVerifierBuilder.builder()
		.equalsVerifiers(ImmutableList.<Class<?>>of(
				EmailMetadata.class,
				MimeMessage.class))
		.prefabValue(IMimePart.class,
				MimePart.builder().contentType("text/plain").encoding("7BIT").build(),
				MimePart.builder().contentType("text/html").encoding("8BIT").build())
		.verify();
	}
	
}

/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.mime.MimePart;

import com.google.common.collect.ImmutableList;


public class StrictMatchBodyPreferencePolicyTest {
	
	@Test
	public void selectBetterFitNoFetchInstructionEntryStrictPolicy() {
		FetchInstruction fetchInstruction = new StrictMatchBodyPreferencePolicy().selectBetterFit(
				ImmutableList.<FetchInstruction>of(), 
				BodyPreferencePolicyUtils.bodyPreferences(MSEmailBodyType.HTML));
		assertThat(fetchInstruction).isNull();
	}
	
	@Test
	public void selectBetterFitNoBodyPreferenceEntry() {
		MimePart mimePart = EasyMock.createNiceMock(MimePart.class);
		FetchInstruction onlyFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction actual = 
			new StrictMatchBodyPreferencePolicy().selectBetterFit(
				ImmutableList.of(onlyFetchInstruction), 
				BodyPreferencePolicyUtils.bodyPreferences());
		assertThat(actual).isSameAs(onlyFetchInstruction);
	}
	
	@Test
	public void selectBetterFitHtmlAndMimePreferences() {
		MimePart mimePart = EasyMock.createNiceMock(MimePart.class);
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction mimeFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.MIME).mimePart(mimePart).build();
		FetchInstruction actual = 
			new StrictMatchBodyPreferencePolicy().selectBetterFit(
				ImmutableList.of(htmlFetchInstruction, mimeFetchInstruction),
				BodyPreferencePolicyUtils.bodyPreferences());
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}
	
	@Test
	public void selectBetterFitHtmlAndMimePreferencesReverseOrder() {
		MimePart mimePart = EasyMock.createNiceMock(MimePart.class);
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction mimeFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.MIME).mimePart(mimePart).build();
		FetchInstruction actual = 
			new StrictMatchBodyPreferencePolicy().selectBetterFit(
				ImmutableList.of(mimeFetchInstruction, htmlFetchInstruction),
				BodyPreferencePolicyUtils.bodyPreferences());
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}

	@Test
	public void selectBetterFitRespectPreferencesOrdering() {
		MimePart mimePart = EasyMock.createNiceMock(MimePart.class);
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();
		FetchInstruction plainFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();
		FetchInstruction rtfFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.RTF).mimePart(mimePart).build();
		FetchInstruction actual =
			new StrictMatchBodyPreferencePolicy().selectBetterFit(
				ImmutableList.of(plainFetchInstruction, htmlFetchInstruction, rtfFetchInstruction),
				BodyPreferencePolicyUtils.bodyPreferences(MSEmailBodyType.RTF, MSEmailBodyType.PlainText, MSEmailBodyType.HTML));
		assertThat(actual).isSameAs(rtfFetchInstruction);
	}
	
	@Test
	public void selectBetterFitTransformationIsNotTheBestFit() {
		MimePart mimePart = EasyMock.createNiceMock(MimePart.class);
		FetchInstruction transformedHtmlFetchInstruction = FetchInstruction.builder()
				.bodyType(MSEmailBodyType.HTML).mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML).mimePart(mimePart).build();
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.HTML).mimePart(mimePart).build();

		FetchInstruction actual =
			new StrictMatchBodyPreferencePolicy().selectBetterFit(
				ImmutableList.of(transformedHtmlFetchInstruction, htmlFetchInstruction),
				BodyPreferencePolicyUtils.bodyPreferences(MSEmailBodyType.HTML));
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}
	
	@Test
	public void selectBetterFitTransformationIsBetterThanFollowingPreferences() {
		MimePart mimePart = EasyMock.createNiceMock(MimePart.class);
		FetchInstruction transformedHtmlFetchInstruction = FetchInstruction.builder()
				.bodyType(MSEmailBodyType.HTML).mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML).mimePart(mimePart).build();
		FetchInstruction htmlFetchInstruction = FetchInstruction.builder().bodyType(MSEmailBodyType.PlainText).mimePart(mimePart).build();

		FetchInstruction actual =
			new StrictMatchBodyPreferencePolicy().selectBetterFit(
				ImmutableList.of(transformedHtmlFetchInstruction, htmlFetchInstruction),
				BodyPreferencePolicyUtils.bodyPreferences(MSEmailBodyType.HTML, MSEmailBodyType.PlainText));
		assertThat(actual).isSameAs(htmlFetchInstruction);
	}
}

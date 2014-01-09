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

package org.obm.push.protocol.data.ms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.*;
import static org.obm.DateUtils.date;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.mail.internet.AddressException;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSImportance;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Charsets;


public class MSEmailDecoderTest {

	private MSMeetingRequestDecoder meetingRequestDecoder;
	private MSEmailDecoder decoder;

	@Before
	public void setup(){
		meetingRequestDecoder = createMock(MSMeetingRequestDecoder.class);
		decoder = new MSEmailDecoder(meetingRequestDecoder);
	}

	@Test
	public void parseAddressWhenSimple() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("login@domain.org");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWhenNoDisplayName() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("<login@domain.org>");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWhenNoDisplayNameButSpace() throws AddressException {
		List<MSAddress> addresses = decoder.addresses(" <login@domain.org> ");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWhenNoDisplayNameButInnerSpace() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("< login@domain.org >");
		assertThat(addresses).containsOnly(new MSAddress("login@domain.org"));
	}
	
	@Test
	public void parseAddressWithDisplayName() throws AddressException {
		List<MSAddress> addresses = decoder.addresses("\"display name\" <login@domain.org>");
		assertThat(addresses).containsOnly(new MSAddress("display name", "login@domain.org"));
	}
	
	@Test
	public void parseAddressReturnsEmptyAsItsOptionalWhenNull() throws AddressException {
		assertThat(decoder.addresses(null)).isEmpty();
	}
	
	@Test
	public void parseAddressReturnsEmptyAsItsOptionalWhenEmpty() throws AddressException {
		assertThat(decoder.addresses("")).isEmpty();
	}
	
	@Test(expected=AddressException.class)
	public void parseAddressWithoutEmailData() throws AddressException {
		decoder.addresses("\"display name\" <>");
	}
	
	@Test(expected=AddressException.class)
	public void parseAddressWithoutEmail() throws AddressException {
		decoder.addresses("\"display name\"");
	}
	
	@Test(expected=ParseException.class)
	public void parseDateNeedsPunctuation() throws ParseException {
		decoder.date("20021126T160000Z");
	}
	
	@Test(expected=ParseException.class)
	public void parseDateNeedsTime() throws ParseException {
		decoder.date("2002-11-26");
	}
	
	@Test
	public void parseDateReturnNullAsItsOptionalWhenNull() throws ParseException {
		assertThat(decoder.date(null)).isNull();
	}

	@Test
	public void parseDateReturnNullAsItsOptionalWhenEmpty() throws ParseException {
		assertThat(decoder.date("")).isNull();
	}
	
	@Test
	public void parseDate() throws ParseException {
		Date parsed = decoder.date("2000-12-25T08:35:00.000Z");
		assertThat(parsed).isEqualTo(date("2000-12-25T08:35:00+00"));
	}
	
	@Test
	public void parseBody() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getBodyType()).isEqualTo(MSEmailBodyType.HTML);
		assertThat(body.getEstimatedDataSize()).isEqualTo(930);
		assertThat(body.isTruncated()).isTrue();
		assertThat(body.getMimeData()).hasContentEqualTo(new ByteArrayInputStream("Email data".getBytes(Charsets.UTF_8)));
	}

	@Test
	public void parseBodyTruncatedTrue() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.isTruncated()).isTrue();
	}

	@Test
	public void parseBodyTruncatedFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>0</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.isTruncated()).isFalse();
	}

	@Test
	public void parseBodyTypeMime() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>4</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>0</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getBodyType()).isEqualTo(MSEmailBodyType.MIME);
	}
	
	@Test
	public void parseBodyHasOptionalData() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<Type>2</Type>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getMimeData()).isNull();
	}
	
	@Test
	public void parseBodyHasOptionalType() throws Exception {
		Document doc = DOMUtils.parse(
			"<Body>" +
				"<EstimatedDataSize>930</EstimatedDataSize>" +
				"<Truncated>1</Truncated>" +
				"<Data>Email data</Data>" +
			"</Body>");

		MSEmailBody body = decoder.msEmailBody(doc.getDocumentElement());
		
		assertThat(body.getBodyType()).isNull();
	}
	
	@Test
	public void parseMessageClassNote() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<MessageClass>IPM.Note</MessageClass>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.getMessageClass()).isEqualTo(MSMessageClass.NOTE);
	}
	
	@Test
	public void parseMessageClassNoteSMIME() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<MessageClass>IPM.Note.SMIME</MessageClass>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.getMessageClass()).isEqualTo(MSMessageClass.NOTE_SMIME);
	}

	@Test
	public void parseImportanceHigh() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<Importance>2</Importance>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.getImportance()).isEqualTo(MSImportance.HIGH);
	}

	@Test
	public void parseImportanceLow() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<Importance>0</Importance>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.getImportance()).isEqualTo(MSImportance.LOW);
	}

	@Test
	public void parseImportanceNormal() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<Importance>1</Importance>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.getImportance()).isEqualTo(MSImportance.NORMAL);
	}

	@Test
	public void parseImportanceDefaultIsNormal() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.getImportance()).isEqualTo(MSImportance.NORMAL);
	}
	
	@Test
	public void parseReadFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<Read>0</Read>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.isRead()).isFalse();
	}

	@Test
	public void parseReadTrue() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<Read>1</Read>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());

		assertThat(email.isRead()).isTrue();
	}

	@Test
	public void parseReadDefaultIsFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());

		assertThat(email.isRead()).isFalse();
	}

	@Test
	public void parseMeetingRequestIsCall() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				// MeetingRequest added below
			"</ApplicationData>");

		Element meetingRequestElement = DOMUtils.parse(
			"<MeetingRequest>" +
				"<AllDayEvent>0</AllDayEvent>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>").getDocumentElement();
				
		doc.adoptNode(meetingRequestElement);
		doc.getDocumentElement().appendChild(meetingRequestElement);
		
		expect(meetingRequestDecoder.decode(meetingRequestElement))
			.andReturn(createMock(MSMeetingRequest.class));
		
		replay(meetingRequestDecoder);
		MSEmail email= decoder.decode(doc.getDocumentElement());
		verify(meetingRequestDecoder);
		
		assertThat(email.getMeetingRequest()).isNotNull();
	}

	@Test
	public void parseMeetingRequestIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		assertThat(email.getMeetingRequest()).isNull();
	}

	@Test
	public void parseAttachments() throws Exception {
		Document doc = DOMUtils.parse(
				"<ApplicationData>" +
					"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
					"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
					"<Subject>email subject</Subject>" +
					"<Attachments>" +
						"<Attachment>" +
							"<DisplayName>name.JPG</DisplayName>" +
							"<FileReference>57_44_2_aW1hZ2UvanBlZw==_QkFTRTY0</FileReference>" +
							"<Method>1</Method>" +
							"<EstimatedDataSize>38260</EstimatedDataSize>" +
							"<ContentId>555343607@11062013-0EC1</ContentId>" +
							"<ContentLocation>TB_import.JPG</ContentLocation>" +
							"<IsInline>1</IsInline>" +
						"</Attachment>" +
					"</Attachments>" +
				"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		MSAttachement expectedAttachment = new MSAttachement();
		expectedAttachment.setDisplayName("name.JPG");
		expectedAttachment.setMethod(MethodAttachment.NormalAttachment);
		expectedAttachment.setFileReference("57_44_2_aW1hZ2UvanBlZw==_QkFTRTY0");
		expectedAttachment.setEstimatedDataSize(38260);
		expectedAttachment.setContentId("555343607@11062013-0EC1");
		expectedAttachment.setContentLocation("TB_import.JPG");
		expectedAttachment.setInline(true);
		assertThat(email.getAttachments()).containsOnly(expectedAttachment);
	}

	@Test
	public void parseAttachmentsWithoutOptionalFields() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<Attachments>" +
					"<Attachment>" +
						"<FileReference>57_44_2_aW1hZ2UvanBlZw==_QkFTRTY0</FileReference>" +
						"<Method>5</Method>" +
						"<EstimatedDataSize>0</EstimatedDataSize>" +
					"</Attachment>" +
				"</Attachments>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		
		MSAttachement expectedAttachment = new MSAttachement();
		expectedAttachment.setDisplayName(null);
		expectedAttachment.setMethod(MethodAttachment.EmbeddedMessage);
		expectedAttachment.setFileReference("57_44_2_aW1hZ2UvanBlZw==_QkFTRTY0");
		expectedAttachment.setEstimatedDataSize(0);
		expectedAttachment.setInline(false);
		
		assertThat(email.getAttachments()).containsOnly(expectedAttachment);
	}

	@Test
	public void parseAttachmentsWhenTwoItems() throws Exception {
		Document doc = DOMUtils.parse(
			"<ApplicationData>" +
				"<From> &lt;from@thilaire.lng.org&gt;, &lt;from2@thilaire.lng.org&gt; </From>" +
				"<To> &lt;to@thilaire.lng.org&gt;, &lt;to2@thilaire.lng.org&gt; </To>" +
				"<Subject>email subject</Subject>" +
				"<Attachments>" +
					"<Attachment>" +
						"<DisplayName>name.JPG</DisplayName>" +
						"<FileReference>aaa</FileReference>" +
						"<Method>1</Method>" +
						"<EstimatedDataSize>38260</EstimatedDataSize>" +
					"</Attachment>" +
					"<Attachment>" +
						"<FileReference>bbb</FileReference>" +
						"<Method>5</Method>" +
						"<EstimatedDataSize>0</EstimatedDataSize>" +
					"</Attachment>" +
				"</Attachments>" +
			"</ApplicationData>");

		MSEmail email= decoder.decode(doc.getDocumentElement());
		

		MSAttachement expectedAttachmentOne = new MSAttachement();
		expectedAttachmentOne.setDisplayName("name.JPG");
		expectedAttachmentOne.setMethod(MethodAttachment.NormalAttachment);
		expectedAttachmentOne.setFileReference("aaa");
		expectedAttachmentOne.setEstimatedDataSize(38260);
		
		MSAttachement expectedAttachmentTwo = new MSAttachement();
		expectedAttachmentTwo.setDisplayName(null);
		expectedAttachmentTwo.setMethod(MethodAttachment.EmbeddedMessage);
		expectedAttachmentTwo.setFileReference("bbb");
		expectedAttachmentTwo.setEstimatedDataSize(0);
		
		assertThat(email.getAttachments()).containsOnly(expectedAttachmentOne, expectedAttachmentTwo);
	}
}

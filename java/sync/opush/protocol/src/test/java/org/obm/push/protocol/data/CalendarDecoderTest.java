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

package org.obm.push.protocol.data;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.push.TestUtils.getXml;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GregorianChronology;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventException;
import org.obm.push.protocol.bean.ASSystemTime;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.type.UnsignedShort;
import org.w3c.dom.Document;


public class CalendarDecoderTest {
	
	private CalendarDecoder decoder;
	private Base64ASTimeZoneDecoder base64AsTimeZoneDecoder;
	private ASTimeZoneConverter asTimeZoneConverter;

	@Before
	public void prepareEventConverter(){
		base64AsTimeZoneDecoder = createMock(Base64ASTimeZoneDecoder.class);
		asTimeZoneConverter = createMock(ASTimeZoneConverter.class);
		decoder = new CalendarDecoder(base64AsTimeZoneDecoder, asTimeZoneConverter);
	}
	
	@Test
	public void testDecodeAttendees() throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<AllDayEvent>0</AllDayEvent>");
		builder.append("<BusyStatus>2</BusyStatus>");
		builder.append("<DTStamp>20110228T144758Z</DTStamp>");
		builder.append("<EndTime>20110306T120000Z</EndTime>");
		builder.append("<Location>Toulouse</Location>");
		builder.append("<MeetingStatus>1</MeetingStatus>");
		builder.append("<Sensitivity>0</Sensitivity>");
		builder.append("<Subject>opush2cccttra</Subject>");
		builder.append("<StartTime>20110306T110000Z</StartTime>");
		builder.append("<UID>d68eb415</UID>");
		builder.append("<Attendees>");
		builder.append("<Attendee>");
		builder.append("<AttendeeName>Poupard Adrien</AttendeeName>");
		builder.append("<AttendeeEmail>adrien@test.tlse.lng</AttendeeEmail>");
		builder.append("<AttendeeStatus>3</AttendeeStatus>");
		builder.append("<AttendeeType>1</AttendeeType>");
		builder.append("</Attendee>");
		builder.append("<Attendee>");
		builder.append("<AttendeeName>Admin instrator</AttendeeName>");
		builder.append("<AttendeeEmail>administrator@test.tlse.lng</AttendeeEmail>");
		builder.append("<AttendeeStatus>5</AttendeeStatus>");
		builder.append("<AttendeeType>2</AttendeeType>");
		builder.append("</Attendee>");
		builder.append("<Attendee>");
		builder.append("<AttendeeName>Sara Connor</AttendeeName>");
		builder.append("<AttendeeEmail>sara@test.tlse.lng</AttendeeEmail>");
		builder.append("<AttendeeStatus>4</AttendeeStatus>");
		builder.append("<AttendeeType>1</AttendeeType>");
		builder.append("</Attendee>");
		builder.append("</Attendees>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		assertThat(event.getAttendees().size()).isEqualTo(3);
		MSAttendee adrien = null;
		MSAttendee administrator = null;
		MSAttendee sara = null;
		for(MSAttendee att : event.getAttendees()){
			if("adrien@test.tlse.lng".equals(att.getEmail())){
				adrien = att;
			} else if("administrator@test.tlse.lng".equals(att.getEmail())){
				administrator = att;
			} else if("sara@test.tlse.lng".equals(att.getEmail())){
				sara = att;
			}
		}
		
		checkAttendee(adrien, "Poupard Adrien", "adrien@test.tlse.lng", AttendeeStatus.ACCEPT, AttendeeType.REQUIRED);
		checkAttendee(administrator, "Admin instrator", "administrator@test.tlse.lng", AttendeeStatus.NOT_RESPONDED, AttendeeType.OPTIONAL);
		checkAttendee(sara, "Sara Connor", "sara@test.tlse.lng", AttendeeStatus.DECLINE, AttendeeType.REQUIRED);
	}
	
	private void checkAttendee(MSAttendee att, String name,
			String email, AttendeeStatus status, AttendeeType type) {
		assertThat(att).isNotNull();
		assertThat(att.getName()).isEqualTo(name);
		assertThat(att.getEmail()).isEqualTo(email);
		assertThat(att.getAttendeeStatus()).isEqualTo(status);
		assertThat(att.getAttendeeType()).isEqualTo(type);
	}
	
	private UnsignedShort zeroUnsignedShort() {
		return UnsignedShort.checkedCast(0);
	}
	
	private ASSystemTime zeroSystemTime() {
		ASSystemTime.Builder asSystemTimeBuilder = ASSystemTime.builder();
		asSystemTimeBuilder.year(zeroUnsignedShort())
			.month(zeroUnsignedShort())
			.dayOfWeek(zeroUnsignedShort())
			.weekOfMonth(zeroUnsignedShort())
			.hour(zeroUnsignedShort())
			.minute(zeroUnsignedShort())
			.second(zeroUnsignedShort())
			.milliseconds(zeroUnsignedShort());
		return asSystemTimeBuilder.build();
	}

	private ASTimeZone gmtTimeZone() {
		String gmtName = "Greenwich Mean Time";
		
		ASTimeZone.Builder asTimeZoneBuilder = ASTimeZone.builder();
		asTimeZoneBuilder.bias(0)
			.standardName(gmtName)
			.standardDate(zeroSystemTime())
			.standardBias(0)
			.dayLightName(gmtName)
			.dayLightDate(zeroSystemTime())
			.dayLightBias(0);
		return asTimeZoneBuilder.build();
	}
	
	private void mockTimeZoneConversion(TimeZone timeZone, String asEncryptedTimeZone, ASTimeZone asTimeZone) {
		expect(base64AsTimeZoneDecoder.decode(aryEq(asEncryptedTimeZone.getBytes())))
			.andReturn(asTimeZone).anyTimes();
		
		expect(asTimeZoneConverter.convert(eq(asTimeZone)))
			.andReturn(timeZone).anyTimes();
		
		replay(base64AsTimeZoneDecoder, asTimeZoneConverter);
	}
	
	@Test
	public void testGMTTimeZone() throws Exception{
		TimeZone timeZone = TimeZone.getTimeZone("GMT");
		String gmt = "AAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbgAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbgAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
		
		mockTimeZoneConversion(timeZone, gmt, gmtTimeZone());
		
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<TimeZone>" + gmt + "</TimeZone>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		assertThat(event.getTimeZone()).isNotNull().isEqualTo(timeZone);
	}
	
	private Date dateForTimeZone(DateTimeZone dateTimeZone, int year, int month, int dayOfMonth, int hour, int minute, int second, int milliseconds) {
		GregorianChronology gregorianChronology = GregorianChronology.getInstance(dateTimeZone);
		long millis = gregorianChronology.getDateTimeMillis(year, month, dayOfMonth, hour, minute, second, milliseconds);
		return new Date(millis);
	}
	
	@Test
	public void testGMTDateTime() throws Exception{
		String gmt = "AAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbgAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbgAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";
		
		mockTimeZoneConversion(TimeZone.getTimeZone("GMT"), gmt, gmtTimeZone());
		
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<TimeZone>" + gmt + "</TimeZone>");
		builder.append("<DTStamp>20110228T144758Z</DTStamp>");
		builder.append("<EndTime>20110306T120000Z</EndTime>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		
		Date startTime = dateForTimeZone(DateTimeZone.UTC, 2011, 2, 28, 14, 47, 58, 0);
		assertThat(event.getDtStamp()).isEqualTo(startTime);
		
		Date endTime = dateForTimeZone(DateTimeZone.UTC, 2011, 3, 6, 12, 0, 0, 0);
		assertThat(event.getEndTime()).isEqualTo(endTime);
	}
	
	private ASSystemTime asSummerTime() {
		ASSystemTime.Builder asSystemTimeBuilder = ASSystemTime.builder();
		asSystemTimeBuilder.year(UnsignedShort.checkedCast(2012))
			.month(UnsignedShort.checkedCast(10))
			.dayOfWeek(UnsignedShort.checkedCast(0))
			.weekOfMonth(UnsignedShort.checkedCast(5))
			.hour(UnsignedShort.checkedCast(3))
			.minute(UnsignedShort.checkedCast(0))
			.second(UnsignedShort.checkedCast(0))
			.milliseconds(UnsignedShort.checkedCast(0));
		return asSystemTimeBuilder.build();
	}
	
	private ASSystemTime asWinterTime() {
		ASSystemTime.Builder asSystemTimeBuilder = ASSystemTime.builder();
		asSystemTimeBuilder.year(UnsignedShort.checkedCast(2012))
			.month(UnsignedShort.checkedCast(03))
			.dayOfWeek(UnsignedShort.checkedCast(0))
			.weekOfMonth(UnsignedShort.checkedCast(5))
			.hour(UnsignedShort.checkedCast(2))
			.minute(UnsignedShort.checkedCast(0))
			.second(UnsignedShort.checkedCast(0))
			.milliseconds(UnsignedShort.checkedCast(0));
		return asSystemTimeBuilder.build();
	}
	
	private ASTimeZone parisTimeZone() {
		ASTimeZone.Builder asTimeZoneBuilder = ASTimeZone.builder();
		asTimeZoneBuilder.bias(1)
			.standardName("Central European Summer Time")
			.standardDate(asSummerTime())
			.standardBias(3)
			.dayLightName("Central European Time")
			.dayLightDate(asWinterTime())
			.dayLightBias(2);
		return asTimeZoneBuilder.build();
	}
	
	@Test
	public void testParisTimeZone() throws Exception{
		String paris = "xP///0MAZQBuAHQAcgBhAGwAIABFAHUAcgBvAHAAZQBhAG4AIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAEMAZQBuAHQAcgBhAGwAIABFAHUAcgBvAHAAZQBhAG4AIABTAHUAbQBtAGUAcgAgAFQAaQBtAGUAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==";
		TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");
		
		mockTimeZoneConversion(timeZone, paris, parisTimeZone());
		
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<TimeZone>" + paris + "</TimeZone>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		assertThat(event.getTimeZone()).isNotNull().isEqualTo(timeZone);
	}
	
	@Test
	public void testParisDateTime() throws Exception{
		String paris = "xP///0MAZQBuAHQAcgBhAGwAIABFAHUAcgBvAHAAZQBhAG4AIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAEMAZQBuAHQAcgBhAGwAIABFAHUAcgBvAHAAZQBhAG4AIABTAHUAbQBtAGUAcgAgAFQAaQBtAGUAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==";
		
		mockTimeZoneConversion(TimeZone.getTimeZone("Europe/Paris"), paris, parisTimeZone());
		
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<TimeZone>" + paris + "</TimeZone>");
		builder.append("<DTStamp>20110228T144758Z</DTStamp>");
		builder.append("<EndTime>20110306T120000Z</EndTime>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		
		Date startTime = dateForTimeZone(DateTimeZone.forID("Europe/Paris"), 2011, 2, 28, 15, 47, 58, 0); 
		assertThat(event.getDtStamp()).isEqualTo(startTime);
		
		Date endTime = dateForTimeZone(DateTimeZone.forID("Europe/Paris"), 2011, 3, 6, 13, 0, 0, 0);
		assertThat(event.getEndTime()).isEqualTo(endTime);
	}
	
	@Test
	public void testDecodeOneCategory() throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<Categories>");
		builder.append("<Category>Cat</Category>");
		builder.append("</Categories>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		
		assertThat(event.getCategories()).containsOnly("Cat");
	}
	
	@Test
	public void testDecodeCategories() throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<Categories>");
		builder.append("<Category>Cat1</Category>");
		builder.append("<Category>Cat2</Category>");
		builder.append("</Categories>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		
		assertThat(event.getCategories()).containsOnly("Cat1", "Cat2");
	}
	
	@Test
	public void testDecodeOneCategoryInException() throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<Exceptions>");
		builder.append("<Exception>");
		builder.append("<Categories>");
		builder.append("<Category>Cat</Category>");
		builder.append("</Categories>");
		builder.append("</Exception>");
		builder.append("</Exceptions>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		
		assertThat(event.getExceptions()).hasSize(1);
		MSEventException eventException = event.getExceptions().get(0);
		assertThat(eventException.getCategories()).containsOnly("Cat");
	}
	
	@Test
	public void testDecodeCategoriesInException() throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("<ApplicationData>");
		builder.append("<Exceptions>");
		builder.append("<Exception>");
		builder.append("<Categories>");
		builder.append("<Category>Cat1</Category>");
		builder.append("<Category>Cat2</Category>");
		builder.append("</Categories>");
		builder.append("</Exception>");
		builder.append("</Exceptions>");
		builder.append("</ApplicationData>");
		Document doc = getXml(builder.toString());
		
		IApplicationData  data = decoder.decode(doc.getDocumentElement());
		assertThat(data).isInstanceOf(MSEvent.class);
		MSEvent event = (MSEvent)data;
		
		assertThat(event.getExceptions()).hasSize(1);
		MSEventException eventException = event.getExceptions().get(0);
		assertThat(eventException.getCategories()).containsOnly("Cat1", "Cat2");
	}
}

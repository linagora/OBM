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
package org.obm.sync.server.mailer;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.internal.matchers.StringContains;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;

import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class TemplateTest {
	
	private String applyTemplate(Template template,
			ImmutableMap<Object, Object> datamodel) throws TemplateException,
			IOException {
		StringWriter stringWriter = new StringWriter();
		template.process(datamodel, stringWriter);
		String message = stringWriter.toString();
		return message;
	}

	private ImmutableMap<Object, Object> buildDatamodel() {
		ImmutableMap<Object, Object> datamodel = ImmutableMap.builder().
			put("start", date(2001, 8, 11, 9, 12)).
			put("end", date(2001, 8, 11, 19, 12)).
			put("subject", "test event").
			put("location", "Lyon").
			put("organizer", "Matthieu Baechler").
			put("creator", "Emmanuel Surleau").
			put("host", "obm.matthieu.lng").
			put("attendees", "attendee").
			put("calendarId", 12).build();
		return datamodel;
	}

	private Template retrieveEventInvitationTemplate(Locale locale) throws IOException {
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(getClass(), "template");
		cfg.setCustomAttribute("datetime_format", "string.medium_short");
		Template template = cfg.getTemplate("EventInvitationPlain.tpl", locale);
		return template;
	}

	private SimpleDate date(int year, int month, int day, int hour, int minute) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(year, month, day, hour, minute, 0);
		Date refDate = calendar.getTime();
		return new SimpleDate(refDate, TemplateDateModel.DATETIME);
	}
	
	
	@Test
	public void testFr() throws IOException, TemplateException {
		Template template = retrieveEventInvitationTemplate(new Locale("fr"));
		ImmutableMap<Object, Object> datamodel = buildDatamodel();
		String message = applyTemplate(template, datamodel);
		assertThat(message)
			.contains("sujet         : test event")
			.contains("lieu          : Lyon")
			.contains("organisateur  : Matthieu Baechler")
			.contains("créé par      : Emmanuel Surleau")
			.contains("du            : 11 sept. 2001 09:12")
			.contains("au            : 11 sept. 2001 19:12")
			.contains("participant(s): attendee");
	}

	@Test
	public void testEn() throws IOException, TemplateException {
		Template template = retrieveEventInvitationTemplate(new Locale("en"));
		ImmutableMap<Object, Object> datamodel = buildDatamodel();
		String message = applyTemplate(template, datamodel);
		assertThat(message)
			.contains("subject       : test event")
			.contains("location      : Lyon")
			.contains("organizer     : Matthieu Baechler")
			.contains("created by    : Emmanuel Surleau")
			.contains("from          : Sep 11, 2001 9:12 AM")
			.contains("to            : Sep 11, 2001 7:12 PM")
			.contains("attendee(s)   : attendee");
	}
	
	@Test
	public void testNested() throws IOException, TemplateException {
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(getClass(), "template");
		Template template = cfg.getTemplate("Nested.tpl", Locale.FRENCH);
		ImmutableMap<Object, Object> datamodel = ImmutableMap.of((Object)"top", (Object)"one", "sub", ImmutableMap.of("message", "two"));
		String message = applyTemplate(template, datamodel);
		assertThat(message)
			.contains("Normal variable : one")
			.contains("Nested variable : two");
	}

}

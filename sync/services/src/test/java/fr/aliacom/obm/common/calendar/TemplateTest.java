package fr.aliacom.obm.common.calendar;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;

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
			put("author", "Matthieu Baechler").
			put("host", "obm.matthieu.lng").
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
		Assert.assertThat(message, StringContains.containsString("sujet  : test event"));
		Assert.assertThat(message, StringContains.containsString("lieu   : Lyon"));
		Assert.assertThat(message, StringContains.containsString("auteur : Matthieu Baechler"));
		Assert.assertThat(message, StringContains.containsString("du     : 11 sept. 2001 09:12"));
		Assert.assertThat(message, StringContains.containsString("au     : 11 sept. 2001 19:12"));
	}

	@Test
	@Ignore("bug with datetime_format")
	public void testEn() throws IOException, TemplateException {
		Template template = retrieveEventInvitationTemplate(new Locale("en"));
		ImmutableMap<Object, Object> datamodel = buildDatamodel();
		String message = applyTemplate(template, datamodel);
		Assert.assertThat(message, StringContains.containsString("subject  : test event"));
		Assert.assertThat(message, StringContains.containsString("location : Lyon"));
		Assert.assertThat(message, StringContains.containsString("author   : Matthieu Baechler"));
		Assert.assertThat(message, StringContains.containsString("from     : Sep 11, 2001 9:12 AM"));
		Assert.assertThat(message, StringContains.containsString("to       : Sep 11, 2001 7:12 PM"));
	}
	
	@Test
	public void testNested() throws IOException, TemplateException {
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(getClass(), "template");
		Template template = cfg.getTemplate("Nested.tpl", Locale.FRENCH);
		ImmutableMap<Object, Object> datamodel = ImmutableMap.of((Object)"top", (Object)"one", "sub", ImmutableMap.of("message", "two"));
		String message = applyTemplate(template, datamodel);
		Assert.assertThat(message, StringContains.containsString("Normal variable : one"));
		Assert.assertThat(message, StringContains.containsString("Nested variable : two"));
	}

}

package org.obm.sync.server.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import freemarker.template.Template;

public class TemplateLoaderFreeMarkerImplTest {

	private TemplateLoaderFreeMarkerImpl templateLoader;
	
	@Before
	public void setUp() {
		templateLoader = new TemplateLoaderFreeMarkerImpl(null);
	}
	
	@Test
	public void testTemplateLoaderCanLoadFrTemplates() throws IOException {
		Template template = templateLoader.getTemplate(
				"RecurrentEventUpdateInvitationPlain.tpl", Locale.FRENCH, TimeZone.getDefault());
		
		assertThat(template).isNotNull();
	}
	
	@Test
	public void testTemplateLoaderCanLoadEnTemplates() throws IOException {
		Template template = templateLoader.getTemplate(
				"RecurrentEventUpdateInvitationPlain.tpl", Locale.ENGLISH, TimeZone.getDefault());
		
		assertThat(template).isNotNull();
	}
	
	@Test
	public void testTemplateLoaderCanLoadZhTemplates() throws IOException {
		Template template = templateLoader.getTemplate(
				"RecurrentEventUpdateInvitationPlain.tpl", Locale.CHINESE, TimeZone.getDefault());
		
		assertThat(template).isNotNull();
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testTemplateLoaderCannotLoadJpTemplates() throws IOException {
		Template template = templateLoader.getTemplate(
				"RecurrentEventUpdateInvitationPlain.tpl", Locale.JAPAN, TimeZone.getDefault());
		
		assertThat(template).isNotNull();
	}
	
}

package org.obm.configuration.resourcebundle;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.common.collect.Lists;

public class Control extends ResourceBundle.Control {

	private final static Locale DEFAULT_LOCALE = Locale.FRENCH;
	
	@Override
	public List<String> getFormats(String baseName) {
		return Lists.newArrayList("java.properties");
	}
	
	@Override
	public Locale getFallbackLocale(String baseName, Locale locale) {
		Locale fallBackLocale = super.getFallbackLocale(baseName, locale);
		if (fallBackLocale == null) {
			fallBackLocale = DEFAULT_LOCALE;
		}
		return fallBackLocale;
	}
	
}

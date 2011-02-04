package org.obm.sync.server.template;

import java.io.IOException;
import java.util.Locale;

import freemarker.template.Template;

public interface ITemplateLoader {

	Template getTemplate(String templateName, Locale locale) throws IOException;

}

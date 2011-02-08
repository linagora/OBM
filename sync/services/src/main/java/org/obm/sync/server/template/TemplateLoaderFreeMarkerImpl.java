package org.obm.sync.server.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.server.mailer.AbstractMailer;

import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.services.constant.ConstantService;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class TemplateLoaderFreeMarkerImpl implements ITemplateLoader{
	

	private static final Log logger = LogFactory.getLog(CalendarBindingImpl.class);
	
	
	private ConstantService constantService;
	
	@Inject
	public TemplateLoaderFreeMarkerImpl(ConstantService constantService) {
		this.constantService = constantService;
	}
	
	
	private Configuration getDefaultCfg() {
		Configuration externalCfg = new Configuration();
		externalCfg.setClassForTemplateLoading(AbstractMailer.class, "template");
		return externalCfg;
	}
	
	
	private Configuration getOverrideCfg() throws IOException{
		Configuration externalCfg = new Configuration();
		externalCfg.setDirectoryForTemplateLoading(new File(constantService.getOverrideTemplateFolder()));
		return externalCfg;
	}
	
	public Template getTemplate(String templateName, Locale locale) throws IOException {
		Template ret = null;
		try{
			ret = getOverrideCfg().getTemplate(templateName, locale);
		} catch (Throwable e) {
			if(logger.isDebugEnabled()){
				logger.debug("Error while loading Template[ " + templateName + "] in " + constantService.getOverrideTemplateFolder(), e);
			}
		}
		if(ret == null){
			ret = getDefaultCfg().getTemplate(templateName, locale);
		}
		if(ret == null){
			throw new FileNotFoundException("Error while loading Template[ " + templateName + "] in " + constantService.getDefaultTemplateFolder() );
		}
		return ret;
	}
}

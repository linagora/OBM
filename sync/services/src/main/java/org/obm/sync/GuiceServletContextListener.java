package org.obm.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.UserTransaction;

import org.obm.sync.server.template.ITemplateLoader;
import org.obm.sync.server.template.TemplateLoaderFreeMarkerImpl;
import org.obm.sync.server.transactional.Transactional;
import org.obm.sync.server.transactional.TransactionalInterceptor;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.Message;

import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.calendar.CalendarDaoJdbcImpl;
import fr.aliacom.obm.common.domain.DomainCache;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.setting.SettingsServiceImpl;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserServiceImpl;
import fr.aliacom.obm.freebusy.FreeBusyPluginModule;
import fr.aliacom.obm.freebusy.DatabaseFreeBusyProvider;
import fr.aliacom.obm.freebusy.LocalFreeBusyProvider;
import fr.aliacom.obm.utils.ObmHelper.TransactionProvider;

public class GuiceServletContextListener implements ServletContextListener { 

	public static final String ATTRIBUTE_NAME = "GuiceInjecter";
	
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	XTrustProvider.install();
    	
        final ServletContext servletContext = servletContextEvent.getServletContext(); 

        
        try {
        	Injector injector = createInjector();
        	if (injector == null) { 
        		failStartup("Could not create injector: createInjector() returned null"); 
        	} 
        	servletContext.setAttribute(ATTRIBUTE_NAME, injector);
        } catch (Exception e) {
        	failStartup(e.getMessage());
        } 
    } 
    
    private Injector createInjector() throws Exception {
    	return Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(DomainService.class).to(DomainCache.class);
				bind(UserService.class).to(UserServiceImpl.class);
				bind(SettingsService.class).to(SettingsServiceImpl.class);
    			bind(ObmSmtpConf.class).to(ObmSmtpConfImpl.class);
    			bind(CalendarDao.class).to(CalendarDaoJdbcImpl.class);
    			bind(ITemplateLoader.class).to(TemplateLoaderFreeMarkerImpl.class);
    			bind(LocalFreeBusyProvider.class).to(DatabaseFreeBusyProvider.class);
    			
    			bind(UserTransaction.class).toProvider(TransactionProvider.class);

    		    ServiceLoader<FreeBusyPluginModule> pluginModules = ServiceLoader.load( FreeBusyPluginModule.class );
    		    
    		    List<FreeBusyPluginModule> pluginModulesList = new ArrayList<FreeBusyPluginModule>();
    		    for(FreeBusyPluginModule pluginModule : pluginModules) {
    		    	pluginModulesList.add(pluginModule);
    		    }

    		    Collections.sort(pluginModulesList, Collections.reverseOrder());
    		    for(FreeBusyPluginModule pluginModule : pluginModulesList) {
    		    	this.install(pluginModule);
    		    }
    		      

    			TransactionalInterceptor transactionalInterceptor = new TransactionalInterceptor();
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), 
						transactionalInterceptor);
				requestInjection(transactionalInterceptor);
			}
    	}, new MessageQueueModule());
    }
    
    private void failStartup(String message) { 
        throw new CreationException(Collections.nCopies(1, new Message(this, message))); 
    }
    
    public void contextDestroyed(ServletContextEvent servletContextEvent) { 
    	servletContextEvent.getServletContext().setAttribute(ATTRIBUTE_NAME, null); 
    }
}
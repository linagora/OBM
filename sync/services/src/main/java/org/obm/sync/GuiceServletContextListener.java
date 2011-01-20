package org.obm.sync;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.spi.Message;

import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.calendar.CalendarDaoJdbcImpl;
import fr.aliacom.obm.common.domain.DomainCache;
import fr.aliacom.obm.common.domain.DomainService;

public class GuiceServletContextListener implements ServletContextListener { 

	public static final String ATTRIBUTE_NAME = "GuiceInjecter";
	
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	XTrustProvider.install();
    	
        final ServletContext servletContext = servletContextEvent.getServletContext(); 

        Injector injector = createInjector(); 
        if (injector == null) { 
                failStartup("Could not create injector: createInjector() returned null"); 
        } 
        servletContext.setAttribute(ATTRIBUTE_NAME, injector); 
    } 
    
    private Injector createInjector() {
    	return Guice.createInjector(new Module() {
    		@Override
    		public void configure(Binder binder) {
    			binder.bind(DomainService.class).to(DomainCache.class);
    			binder.bind(ObmSmtpConf.class).to(ObmSmtpConfImpl.class);
    			binder.bind(CalendarDao.class).to(CalendarDaoJdbcImpl.class);
    		}
    	});
    }
    
    private void failStartup(String message) { 
        throw new CreationException(Collections.nCopies(1, new Message(this, message))); 
    }
    
    public void contextDestroyed(ServletContextEvent servletContextEvent) { 
    	servletContextEvent.getServletContext().setAttribute(ATTRIBUTE_NAME, null); 
    }
}
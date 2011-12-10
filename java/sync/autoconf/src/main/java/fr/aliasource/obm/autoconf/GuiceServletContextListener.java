package fr.aliasource.obm.autoconf;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.obm.annotations.transactional.TransactionalModule;
import org.obm.dbcp.DBCP;
import org.obm.dbcp.IDBCP;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.spi.Message;

public class GuiceServletContextListener implements ServletContextListener{
	public static final String ATTRIBUTE_NAME = "GuiceInjecter";
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	
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
	private Injector createInjector() {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            	bind(IDBCP.class).to(DBCP.class);
            }
        }, new TransactionalModule());

	}
	
    private void failStartup(String message) {
        throw new CreationException(Collections.nCopies(1, new Message(this, message)));
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setAttribute(ATTRIBUTE_NAME, null);
    }

}

package org.obm.push;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.obm.configuration.ConfigurationService;
import org.obm.push.backend.BackendFactory;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IBackendFactory;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.OBMBackend;
import org.obm.push.impl.InvitationFilterManagerImpl;
import org.obm.push.mail.EmailManager;
import org.obm.push.mail.IEmailManager;
import org.obm.push.store.IStorageFactory;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.StorageFactory;
import org.obm.push.store.SyncStorage;
import org.obm.sync.XTrustProvider;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.spi.Message;

public class GuiceServletContextListener implements ServletContextListener { 

	public static final String ATTRIBUTE_NAME = "OpushGuiceInjecter";
	
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
				bind(IStorageFactory.class).to(StorageFactory.class);
				bind(IBackendFactory.class).to(BackendFactory.class);
				bind(ISyncStorage.class).to(SyncStorage.class);
				bind(IEmailManager.class).to(EmailManager.class);
				bind(IHierarchyExporter.class).to(HierarchyExporter.class);
				bind(IContentsExporter.class).to(ContentsExporter.class);
				bind(ConfigurationService.class).to(OpushConfigurationService.class);
				bind(IInvitationFilterManager.class).to(InvitationFilterManagerImpl.class);	
				bind(IBackend.class).to(OBMBackend.class);
				bind(IContentsImporter.class).to(ContentsImporter.class);
				bind(IErrorsManager.class).to(ErrorsManager.class);
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
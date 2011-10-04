package org.obm.push;

import org.eclipse.jetty.continuation.ContinuationFilter;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

public class OpushServletModule extends ServletModule{

	 @Override
	    protected void configureServlets() {
	        super.configureServlets();

	        serve("/*").with(ActiveSyncServlet.class);

	        
	        bind(ContinuationFilter.class).in(Singleton.class);
	        filter("/*").through(ContinuationFilter.class);
	    }
}

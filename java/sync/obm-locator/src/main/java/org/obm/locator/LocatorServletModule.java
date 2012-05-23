package org.obm.locator;


import com.google.inject.servlet.ServletModule;

public class LocatorServletModule extends ServletModule{

	 @Override
	    protected void configureServlets() {
	        super.configureServlets();

	        serve("/*").with(HostLocationServlet.class);

	    }
}

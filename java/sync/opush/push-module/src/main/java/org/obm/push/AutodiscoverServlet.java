package org.obm.push;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.handler.AuthenticatedServlet;
import org.obm.push.handler.AutodiscoverHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.impl.ResponderImpl.Factory;
import org.obm.push.protocol.request.SimpleQueryString;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AutodiscoverServlet extends AuthenticatedServlet {

	private final AutodiscoverHandler autodiscoverHandler;
	private final Factory responderFactory;
	
	@Inject
	protected AutodiscoverServlet(LoginService loginService, AutodiscoverHandler autodiscoverHandler, 
			User.Factory userFactory, LoggerService loggerService, ResponderImpl.Factory responderFactory) {
		
		super(loginService, loggerService, userFactory);
		this.autodiscoverHandler = autodiscoverHandler;
		this.responderFactory = responderFactory;
	}

	@Override
	@Transactional
	protected void service(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		try {
			Credentials credentials = authentication(request);
			getLoggerService().initSession(credentials.getUser(), 0, "autodiscover");
			
			BackendSession backendSession = new BackendSession(credentials, "autodiscover", null, null);
			SimpleQueryString queryString = new SimpleQueryString(request);
			Responder responder = responderFactory.createResponder(response);
			
			autodiscoverHandler.process(null, backendSession, queryString, responder);
		} catch (AuthFault e) {
			logger.error(e.getMessage(), e);
			returnHttpUnauthorized(request, response);
			return;
		}

	}
	
}

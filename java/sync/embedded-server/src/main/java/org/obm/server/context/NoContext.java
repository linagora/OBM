/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.server.context;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.AttributesMap;

public class NoContext extends AttributesMap implements ServletContext {

	@Override
	public ServletContext getContext(String uripath) {
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 3;
	}

	@Override
	public String getMimeType(String file) {
		return null;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String uriInContext) {
		return null;
	}

	@Override
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return null;
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return null;
	}

	@Override
	public String getServerInfo() {
		return "jetty/" + Server.getVersion();
	}

	@Override
	@Deprecated
	public Servlet getServlet(String name) throws ServletException {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Deprecated
	public Enumeration<String> getServletNames() {
		return Collections.enumeration(Collections.EMPTY_LIST);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Deprecated
	public Enumeration<Servlet> getServlets() {
		return Collections.enumeration(Collections.EMPTY_LIST);
	}

	@Override
	public void log(Exception exception, String msg) {
	}

	@Override
	public void log(String msg) {
	}

	@Override
	public void log(String message, Throwable throwable) {
	}

	@Override
	public String getInitParameter(String name) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.enumeration(Collections.EMPTY_LIST);
	}

	@Override
	public String getServletContextName() {
		return "No Context";
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return false;
	}

	@Override
	public Dynamic addFilter(String filterName,
			Class<? extends Filter> filterClass) {
		return null;
	}

	@Override
	public Dynamic addFilter(String filterName, Filter filter) {
		return null;
	}

	@Override
	public Dynamic addFilter(String filterName, String className) {
		return null;
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(
			String servletName, Class<? extends Servlet> servletClass) {
		return null;
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(
			String servletName, Servlet servlet) {
		return null;
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(
			String servletName, String className) {
		return null;
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> c)
			throws ServletException {
		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> c)
			throws ServletException {
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return null;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return null;
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return null;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return null;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
	}

	@Override
	public void addListener(String className) {
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz)
			throws ServletException {
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		AccessController.checkPermission(new RuntimePermission("getClassLoader"));
		return ContextHandler.class.getClassLoader();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return 3;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 0;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return null;
	}

	@Override
	public void declareRoles(String... roleNames) {
	}

	@Override
	public String getVirtualServerName() {
		return null;
	}
}

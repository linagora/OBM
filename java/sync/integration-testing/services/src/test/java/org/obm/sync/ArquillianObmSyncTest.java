/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync;

import static org.fest.assertions.api.Assertions.assertThat;

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.sync.server.SyncServlet;

import com.google.common.base.Charsets;

@RunWith(Arquillian.class)
public class ArquillianObmSyncTest {

	@Test
	@RunAsClient
	public void testGet404(@ArquillianResource URL baseURL) throws Exception {
		Request request = Request.Get(baseURL + "this/url/does/not/exist");
		
		Response response = request.execute();
		
		assertThat(response.returnResponse().getStatusLine().getStatusCode())
			.isEqualTo(HttpServletResponse.SC_NOT_FOUND);
	}

	@Deployment
	public static WebArchive deployArchive() {
		Asset webXmlresource = new ByteArrayAsset(buildSimpleWebXml());
		return ShrinkWrap
				.create(WebArchive.class)
				.addClass(SyncServlet.class)
				.addAsWebInfResource(webXmlresource, "web.xml");
	}

	private static byte[] buildSimpleWebXml() {
		return (
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\">" +
			"<web-app>" +
				"<servlet>" +
					"<servlet-name>sync</servlet-name>" +
					"<display-name>OBM Sync Servlet</display-name>" +
					"<servlet-class>org.obm.sync.server.SyncServlet</servlet-class>" +
				"</servlet>" +
				"<servlet-mapping>" +
					"<servlet-name>sync</servlet-name>" +
					"<url-pattern>/services/*</url-pattern>" +
				"</servlet-mapping>" +
			"</web-app>"
			).getBytes(Charsets.UTF_8);
	}
	
}

/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.healthcheck.handlers;

import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.obm.healthcheck.HealthCheckHandler;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;

@Singleton
@Path("/")
public class RootHandler implements HealthCheckHandler {

	@Inject(optional = true)
	private Set<HealthCheckHandler> handlers;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<EndpointDescription> root() {
		List<EndpointDescription> endpoints = Lists.newArrayList();
		
		if (handlers != null) {
			for (HealthCheckHandler handler : handlers) {
				AbstractResource resource = IntrospectionModeller.createResource(handler.getClass());
				String resourcePath = resource.getPath().getValue();
				
				for (AbstractSubResourceMethod endpoint : resource.getSubResourceMethods()) {
					endpoints.add(new EndpointDescription(endpoint.getHttpMethod(), resourcePath + '/' + endpoint.getPath().getValue()));
				}
			}
		}
		
		return endpoints;
	}
	
	public static class EndpointDescription {
		private final String method;
		private final String path;
		
		public EndpointDescription(String method, String path) {
			this.method = method;
			this.path = path;
		}

		public String getMethod() {
			return method;
		}

		public String getPath() {
			return path;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(method, path);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EndpointDescription) {
				EndpointDescription other = (EndpointDescription) obj;
				
				return Objects.equal(method, other.method) && Objects.equal(path, other.path);
			}
			
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this).add("path", path).add("method", method).toString();
		}
		
	}
	
}

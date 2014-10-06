/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.satellite.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.obm.satellite.client.exceptions.ConnectionException;
import org.obm.satellite.client.exceptions.SatteliteClientException;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.collect.Iterables;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ConnectionImpl implements Connection {

	public static final String SATELLITE_MTA_PATH = "/postfixsmtpinmaps/host/%s";
	public static final String SATELLITE_IMAP_PATH = "/cyruspartition/host/add/%s";

	private final HttpClient client;
	private final Configuration configuration;
	private final ObmDomain domain;

	public ConnectionImpl(HttpClient client, Configuration configuration, ObmDomain domain) {
		this.client = client;
		this.configuration = configuration;
		this.domain = domain;
	}

	@Override
	public void updateMTA() throws SatteliteClientException, ConnectionException {
		ObmHost mtaHost = Iterables.getFirst(domain.getHosts().get(ServiceProperty.SMTP_IN), null);

		if (mtaHost == null) {
			throw new SatteliteClientException(String.format("Domain %s doesn't have a linked mail/smtp_in host", domain.getName()));
		}

		post(mtaHost.getName(), mtaHost.getIp(), SATELLITE_MTA_PATH);
	}

	@Override
	public void updateIMAPServer() throws SatteliteClientException, ConnectionException {
		if (!configuration.isIMAPServerManaged()) {
			return;
		}

		ObmHost imapHost = Iterables.getFirst(domain.getHosts().get(ServiceProperty.IMAP), null);

		if (imapHost == null) {
			throw new SatteliteClientException(String.format("Domain %s doesn't have a linked mail/imap host", domain.getName()));
		}

		post(imapHost.getName(), imapHost.getIp(), SATELLITE_IMAP_PATH);
	}

	@Override
	public void shutdown() throws ConnectionException {
	}

	private void post(String hostName, String host, String path) {
		try {
			URI uri = new URIBuilder()
			.setScheme(configuration.getSatelliteProtocol().getScheme())
			.setHost(host)
			.setPort(configuration.getSatellitePort())
			.setPath(String.format(path, hostName))
			.build();
			StatusLine statusLine = Executor
					.newInstance(client)
					.authPreemptive(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()))
					.auth(configuration.getUsername(), configuration.getPassword().getStringValue())
					.execute(Request.Post(uri))
					.returnResponse()
					.getStatusLine();

			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				throw new SatteliteClientException(String.format("The MTA at %s returned %s", host, statusLine));
			}
		}
		catch (IOException e) {
			throw new ConnectionException(e);
		}
		catch (URISyntaxException e) {
			throw new SatteliteClientException(e);
		}
	}
}

/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.handler;

import java.io.IOException;
import java.io.InputStream;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.technicallog.bean.KindToBeLogged;
import org.obm.push.technicallog.bean.TechnicalLogging;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Abstract class for handling client requests with a Content-Type set to
 * <code>application/vnd.ms-sync.wbxml</code>
 */
public abstract class WbxmlRequestHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected final IBackend backend;
	protected final EncoderFactory encoderFactory;
	protected final IContentsImporter contentsImporter;
	protected final IContentsExporter contentsExporter;
	protected final StateMachine stMachine;
	protected final CollectionDao collectionDao;

	private final WBXMLTools wbxmlTools;

	private final DOMDumper domDumper;

	protected WbxmlRequestHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter,
			StateMachine stMachine, CollectionDao collectionDao,
			WBXMLTools wbxmlTools, DOMDumper domDumper) {
		
		this.backend = backend;
		this.encoderFactory = encoderFactory;
		this.contentsImporter = contentsImporter;
		this.contentsExporter = contentsExporter;
		this.stMachine = stMachine;
		this.collectionDao = collectionDao;
		this.wbxmlTools = wbxmlTools;
		this.domDumper = domDumper;
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.REQUEST, onStartOfMethod=true, onEndOfMethod=true)
	public void process(IContinuation continuation, UserDataRequest udr,
			ActiveSyncRequest request, Responder responder) throws IOException {

		InputStream in = request.getInputStream();
		byte[] input = FileUtils.streamBytes(in, true);
		Document doc = null;

		if (input != null && input.length > 0) {
			try {
				doc = wbxmlTools.toXml(input);
			} catch (IOException e) {
				logger.error("Error parsing wbxml data.", e);
				return;
			}
		} else {
			logger.debug("empty wbxml command (valid for Ping & Sync)");
			// To reduce the amount of data sent in a Ping command request, the
			// server caches the heartbeat
			// interval and folder list. The client can omit the heartbeat
			// interval, the folder list, or both from
			// subsequent Ping requests if those parameters have not changed
			// from the previous Ping
			// request. If neither the heartbeat interval nor the folder list
			// has changed, the client can issue a
			// Ping request that does not contain an XML body. If the Ping
			// element is specified in an XML
			// request body, either the HeartbeatInterval element or the Folders
			// element or both MUST be
			// specified.
		}

		if (doc != null) {
			domDumper.dumpXml( doc);
		}

		process(continuation, udr, doc, request, responder);
	}

	/**
	 * Handles the client request. The wbxml was already decoded and is
	 * available in the doc parameter.
	 * 
	 * @param continuation
	 * @param bs
	 * @param doc
	 *            the decoded wbxml document.
	 * @param responder
	 */
	protected abstract void process(IContinuation continuation,
			UserDataRequest udr, Document doc, ActiveSyncRequest request,
			Responder responder);

	protected EncoderFactory getEncoders() {
		return encoderFactory;
	}

}

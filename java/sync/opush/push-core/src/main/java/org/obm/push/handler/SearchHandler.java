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

import java.util.Set;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.SearchStatus;
import org.obm.push.bean.StoreName;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.XMLValidationException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.SearchProtocol;
import org.obm.push.protocol.bean.SearchRequest;
import org.obm.push.protocol.bean.SearchResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.search.ISearchSource;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SearchHandler extends WbxmlRequestHandler {

	private final ImmutableMultimap<StoreName, ISearchSource> sources;
	private final SearchProtocol protocol;
	
	@Inject
	protected SearchHandler(IBackend backend, EncoderFactory encoderFactory,
			Set<ISearchSource> searchSources,
			IContentsImporter contentsImporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			SearchProtocol searchProtocol, CollectionDao collectionDao,
			WBXMLTools wbxmlTools, DOMDumper domDumper) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.protocol = searchProtocol;
		this.sources =
			Multimaps.index(searchSources, new Function<ISearchSource, StoreName>() {
				@Override
				public StoreName apply(ISearchSource input) {
					return input.getStoreName();
				}
			});
   	}
	
	@Override
	public void process(IContinuation continuation, UserDataRequest udr,
			Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			SearchRequest searchRequest = protocol.decodeRequest(doc);
			SearchResponse response = search(udr, searchRequest);
			Document document = protocol.encodeResponse(response);
			sendResponse(responder, document);
		} catch (XMLValidationException e) {
			logger.error("Protocol violation", e);
			sendError(responder, SearchStatus.PROTOCOL_VIOLATION);
		} catch (NoDocumentException e) {
			logger.error("Protocol violation", e);
			sendError(responder, SearchStatus.PROTOCOL_VIOLATION);
		}
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendWBXMLResponse("Search", document);
	}

	private void sendError(Responder responder, SearchStatus error) {
		Document document = protocol.buildError(error);
		sendResponse(responder, document);
	}

	private SearchResponse search(UserDataRequest udr, SearchRequest searchRequest) {
		SearchResponse.Builder searchResponseBuilder = SearchResponse.builder();
		for (final ISearchSource source: sources.get(searchRequest.getStoreName())) {
			searchResponseBuilder.addAll(source.search(udr, searchRequest.getQuery(), 1000));
		}
		return searchResponseBuilder
				.rangeLower(searchRequest.getRangeLower())
				.rangeUpper(searchRequest.getRangeUpper())
				.build();
	}

}

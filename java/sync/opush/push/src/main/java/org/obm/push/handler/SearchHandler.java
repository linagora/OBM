package org.obm.push.handler;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SearchResult;
import org.obm.push.bean.SearchStatus;
import org.obm.push.bean.StoreName;
import org.obm.push.exception.activesync.XMLValidationException;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.SearchProtocol;
import org.obm.push.protocol.bean.SearchRequest;
import org.obm.push.protocol.bean.SearchResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.search.ISearchSource;
import org.obm.push.search.ObmSearchContact;
import org.obm.push.search.ldap.BookSource;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SearchHandler extends WbxmlRequestHandler {

	private final ImmutableMultimap<StoreName, ISearchSource> sources;
	private final SearchProtocol protocol;
	
	@Inject
	protected SearchHandler(IBackend backend, EncoderFactory encoderFactory,
			BookSource bookSource, ObmSearchContact obmSearchContact,
			IContentsImporter contentsImporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			SearchProtocol searchProtocol, CollectionDao collectionDao,
			WBXMLTools wbxmlTools) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools);
		
		this.protocol = searchProtocol;
		this.sources = ImmutableMultimap.of(
				bookSource.getStoreName(), bookSource, 
				obmSearchContact.getStoreName(), obmSearchContact);
	}
	
	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			SearchRequest searchRequest = protocol.getRequest(doc.getDocumentElement());
			SearchResponse response = search(bs, searchRequest);
			Document document = protocol.encodeResponse(response);
			sendResponse(responder, document);
		} catch (XMLValidationException e) {
			logger.error("Protocol violation", e);
			sendError(responder, SearchStatus.PROTOCOL_VIOLATION);
		}
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendResponse("Search", document);
	}

	private void sendError(Responder responder, SearchStatus error) {
		Document document = protocol.buildError(error);
		sendResponse(responder, document);
	}

	private SearchResponse search(BackendSession bs, SearchRequest searchRequest) {
		final List<SearchResult> results = new LinkedList<SearchResult>();
		for (final ISearchSource source: sources.get(searchRequest.getStoreName())) {
			results.addAll(source.search(bs, searchRequest.getQuery(), 1000));
		}
		return new SearchResponse(results, searchRequest.getRangeLower(), searchRequest.getRangeUpper());
	}

}

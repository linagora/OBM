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
package org.obm.sync.solr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.obm.domain.dao.UserDao;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventType;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class EventIndexer extends SolrRequest {

	@Singleton
	public static class Factory implements IndexerFactory<Event> {

		private final ObmHelper obmHelper;
		private final UserDao userDao;

		@Inject
		private Factory(ObmHelper obmHelper, UserDao userDao) {
			this.obmHelper = obmHelper;
			this.userDao = userDao;
		}
		
		@Override
		public EventIndexer createIndexer(CommonsHttpSolrServer srv, ObmDomain domain, Event e) {
			return new EventIndexer(srv, obmHelper, userDao, domain, e);
		}

	}

	private final Event event;
	private final ObmDomain domain;
	private final ObmHelper obmHelper;
	private final UserDao userDao;

	
	@VisibleForTesting EventIndexer(CommonsHttpSolrServer srv, ObmHelper obmHelper, UserDao userDao, ObmDomain domain, Event e) {
		super(srv);
		
		this.obmHelper = obmHelper;
		this.userDao = userDao;
		this.domain = domain;
		this.event = e;
	}

	@Override
	public void run() throws Exception {
		if (event.getType() != EventType.VEVENT) {
			return;
		}

		int i = 0;
		while (true && i++ < 10) {
			boolean found = doIndex();
			if (found) {
				break;
			} else {
				try {
					logger.warn("waiting for event tx to index...");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.warn("error indexing event");
				}
			}
		}
	}

	@VisibleForTesting boolean doIndex() throws IOException, SolrServerException {
		SolrInputDocument solrInputDocument = null;
		
		try {
			solrInputDocument = buildDocument();
		}
		catch (Exception e) {
			logger.warn("Could not build SolR document, event cannot be indexed.", e);
			return true;
		}
		
		try {
			addTagsToEvent(solrInputDocument);
			server.add(solrInputDocument);
			server.commit();
			logger.info("[" + event.getObmId() + "] indexed in SOLR");
		} catch (SQLException t) {
			logger.error(t.getMessage(), t);
		}
		return true;
	}

	@VisibleForTesting SolrInputDocument buildDocument() {
		String owner = getOwner();
		ObmUser obmUser = userDao.findUser(owner, domain);
		if (obmUser == null) {
			throw new IllegalArgumentException("Cannot fetch owner details (using '" + owner + "') from database.");
		}
		
		SolrInputDocument sid = new SolrInputDocument();

		putField(sid, "id", event.getObmId().getObmId());
		putField(sid, "timecreate", event.getTimeCreate());
		putField(sid, "timeupdate", event.getTimeUpdate());
		putField(sid, "domain", domain.getId());
		putField(sid, "title", event.getTitle());
		putField(sid, "location", event.getLocation());
		putField(sid, "category", event.getCategory());
		putField(sid, "date", event.getStartDate());
		putField(sid, "duration", event.getDuration());
		putField(sid, "owner", obmUser.getLastName(), obmUser.getFirstName(), obmUser.getLogin(), obmUser.getEmailAtDomain());
		putField(sid, "ownerId", obmUser.getUid());
		putField(sid, "description", event.getDescription());

		List<String> attendees = new LinkedList<String>();
		for (Attendee attendee : event.getAttendees()) {
			String s = attendee.getDisplayName() != null ? attendee.getDisplayName() : "";
			s += " " + attendee.getEmail();
			attendees.add(s);
		}
		putField(sid, "with", attendees);
		
		putField(sid, "is",
			(event.isAllday() ? "allday" : null),
			(event.isRecurrent() ? "periodic" : null),
			(event.getOpacity() == EventOpacity.OPAQUE ? "busy" : "free"),
			(event.getPrivacy() == EventPrivacy.PUBLIC ? null : event.getPrivacy().name().toLowerCase()));
		
		return sid;
	}

	private String getOwner() {
		if (!Strings.isNullOrEmpty(event.getOwnerEmail())) {
			return event.getOwnerEmail();
		} else {
			return event.getOwner();
		}
	}
	
	private void putField(SolrInputDocument solrInputDocument, String field, Object... values) {
		SolrInputField solrInputField = new SolrInputField(field);
		for (Object value: values) {
			if (value != null) {
				solrInputField.addValue(value, 1);
			}
		}
		if (solrInputField.getValueCount() >= 1) {
			solrInputDocument.put(field, solrInputField);
		}
	}

	private void addTagsToEvent(SolrInputDocument sid) throws SQLException {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			con = obmHelper.getConnection();
			st = con.prepareStatement(
					"SELECT eventtag_label FROM Event " +
					"INNER JOIN EventTag ON event_tag_id=eventtag_id " +
					"WHERE event_id=?");
			
			st.setInt(1, event.getObmId().getObmId());
			rs = st.executeQuery();
			if (rs.next()) {
				putField(sid, "tag", rs.getString(1));
			}
		} finally {
			obmHelper.cleanup(con, st, rs);
		}
	}
}
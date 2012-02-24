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
package org.obm.sync.solr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.utils.ObmHelper;

public class EventIndexer implements Runnable {

	private static final Logger logger = LoggerFactory
			.getLogger(EventIndexer.class);
	
	@Singleton
	public static class Factory {

		private final ObmHelper obmHelper;
		private final UserDao userDao;

		@Inject
		private Factory(ObmHelper obmHelper, UserDao userDao) {
			this.obmHelper = obmHelper;
			this.userDao = userDao;
		}
		
		public EventIndexer createIndexer(CommonsHttpSolrServer srv, ObmDomain domain, Event e) {
			return new EventIndexer(srv, obmHelper, userDao, domain, e);
		}

	}

	private CommonsHttpSolrServer srv;
	private Event e;
	private ObmDomain domain;
	private final ObmHelper obmHelper;
	private final UserDao userDao;

	
	private EventIndexer(CommonsHttpSolrServer srv, ObmHelper obmHelper, UserDao userDao, ObmDomain domain, Event e) {
		this.obmHelper = obmHelper;
		this.userDao = userDao;
		this.domain = domain;
		this.srv = srv;
		this.e = e;
	}

	@Override
	public void run() {
		if (e.getType() != EventType.VEVENT) {
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

	private boolean doIndex() {
		SolrInputDocument sid = new SolrInputDocument();

		f(sid, "id", e.getObmId().getObmId());
		f(sid, "timecreate", e.getTimeCreate());
		f(sid, "timeupdate", e.getTimeUpdate());

		// TODO usercreate
		// TODO userupdate

		f(sid, "domain", domain);
		f(sid, "title", e.getTitle());
		f(sid, "location", e.getLocation());
		f(sid, "category", e.getCategory());
		f(sid, "date", e.getDate());
		f(sid, "duration", e.getDuration());

		// owner: login ownerEmail: lAtDomain
		ObmUser u = userDao.findUserByLogin(e.getOwner(),
				domain);

		f(sid, "owner", u.getLastName(), u.getFirstName(), u.getLogin(),
				u.getEmail());
		f(sid, "ownerId", u.getUid());
		f(sid, "description", e.getDescription());

		List<String> atts = new LinkedList<String>();
		for (Attendee at : e.getAttendees()) {
			String s = at.getDisplayName() != null ? at.getDisplayName() : "";
			s += " " + at.getEmail();
			atts.add(s);
		}
		f(sid, "with", atts);

		// withId is unused

		f(sid, "is", (e.isAllday() ? "allday" : null),
				(e.isRecurrent() ? "periodic" : null),
				(e.getOpacity() == EventOpacity.OPAQUE ? "busy" : "free"),
				(e.getPrivacy() == 1 ? "private" : null));

		// state is unused

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement st = null;
		try {
			con = obmHelper.getConnection();
			st = con.prepareStatement("SELECT eventtag_label FROM Event " +
                    "INNER JOIN EventTag ON event_tag_id=eventtag_id WHERE event_id=?");
			// query tag...
			st.setInt(1, e.getObmId().getObmId());
			rs = st.executeQuery();
			if (rs.next()) {
				f(sid, "tag", rs.getString(1));
			}

			srv.add(sid);
			srv.commit();
			logger.info("[" + e.getObmId() + "] indexed in SOLR");
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			obmHelper.cleanup(con, st, rs);
		}
		return true;
	}

	private void f(SolrInputDocument sid, String field,
			Collection<Object> values) {
		if (values != null && !values.isEmpty()) {
			SolrInputField sif = new SolrInputField(field);
			for (Object v : values) {
				sif.addValue(v, 1);
			}
			sid.put(field, sif);
		}
	}

	private void f(SolrInputDocument sid, String field, Object... values) {
		LinkedList<Object> l = new LinkedList<Object>();
		for (Object o : values) {
			if (o != null) {
				l.add(o);
			}
		}
		f(sid, field, l);
	}

}

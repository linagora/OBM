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
package org.obm.sync.solr.jms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.obm.domain.dao.UserDao;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.solr.SolrDocumentIndexer;
import org.obm.sync.solr.SolrRequest;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class EventUpdateCommand extends EventCommand {

	@Singleton
	public static class Factory {

		private UserDao userDao;
		private ObmHelper obmHelper;

		@Inject
		@VisibleForTesting Factory(UserDao userDao, ObmHelper obmHelper) {
			this.userDao = userDao;
			this.obmHelper = obmHelper;
		}

		public EventUpdateCommand create(ObmDomain domain, String login, Event data) {
			return new EventUpdateCommand(domain, data, login, userDao, obmHelper);
		}
	}

	private UserDao userDao;
	private ObmHelper obmHelper;

	private EventUpdateCommand(ObmDomain domain, Event data, String login, UserDao userDao, ObmHelper obmHelper) {
		super(domain, login, data);
		this.userDao = userDao;
		this.obmHelper = obmHelper;
	}

	@Override
	public SolrRequest asSolrRequest() {
		try {
			return new SolrDocumentIndexer(getLoginAtDomain(), getSolrService(), dataToDocument());
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	private SolrInputDocument dataToDocument() throws SQLException {
		SolrInputDocument solrInputDocument = buildDocument();
		addTagsToEvent(solrInputDocument);
		return solrInputDocument;
	}
	
	@VisibleForTesting SolrInputDocument buildDocument() {
		Event event = getObject();
		String owner = getOwner();
		ObmUser obmUser = userDao.findUser(owner, getDomain());
		if (obmUser == null) {
			throw new IllegalArgumentException("Cannot fetch owner details (using '" + owner + "') from database.");
		}
		
		SolrInputDocument sid = new SolrInputDocument();

		putField(sid, "id", event.getObmId().getObmId());
		putField(sid, "timecreate", event.getTimeCreate());
		putField(sid, "timeupdate", event.getTimeUpdate());
		putField(sid, "domain", getDomain().getId());
		putField(sid, "title", event.getTitle());
		putField(sid, "location", event.getLocation());
		putField(sid, "category", event.getCategory());
		putField(sid, "date", event.getStartDate());
		putField(sid, "duration", event.getDuration());
		putField(sid, "owner", obmUser.getLastName(), obmUser.getFirstName(), obmUser.getLogin(), obmUser.getEmail());
		putField(sid, "ownerId", obmUser.getUid());
		putField(sid, "description", event.getDescription());

		List<String> attendees = Lists.newArrayList();
		for (Attendee attendee : event.getAttendees()) {
			String s = attendee.getDisplayName() != null ? attendee.getDisplayName() + " " : "";
			s += attendee.getEmail();
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
		Event event = getObject();
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
			
			st.setInt(1, getObject().getObmId().getObmId());
			rs = st.executeQuery();
			if (rs.next()) {
				putField(sid, "tag", rs.getString(1));
			}
		} finally {
			obmHelper.cleanup(con, st, rs);
		}
	}

}

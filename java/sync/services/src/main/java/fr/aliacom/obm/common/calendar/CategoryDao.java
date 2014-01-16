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
package fr.aliacom.obm.common.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.base.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

@Singleton
public class CategoryDao {

	private static final Logger logger = LoggerFactory
			.getLogger(CategoryDao.class);
	private final ObmHelper obmHelper;

	@Inject
	private CategoryDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	public List<Category> getCategories(AccessToken at) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Category> ret = new LinkedList<Category>();
		try {
			con = obmHelper.getConnection();
			ps = con
					.prepareStatement("SELECT eventcategory1_id, eventcategory1_label "
							+ "FROM EventCategory1 WHERE eventcategory1_domain_id=?");
			ps.setInt(1, at.getDomain().getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Category c = new Category();
				c.setId(rs.getInt(1));
				c.setLabel(rs.getString(2));
				ret.add(c);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

}

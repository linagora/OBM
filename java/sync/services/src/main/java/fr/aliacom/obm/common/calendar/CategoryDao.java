/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
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
			ps.setInt(1, at.getDomainId());
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

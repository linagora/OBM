/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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

package org.obm.provisioning.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.UserDao;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;


@Singleton
public class GroupDaoJdbcImpl implements GroupDao {

    
    private DatabaseConnectionProvider connectionProvider;
	private UserDao userDao;

    @Inject
    private GroupDaoJdbcImpl(DatabaseConnectionProvider connectionProvider, UserDao userDao) {
        this.connectionProvider = connectionProvider;
        this.userDao = userDao;
    }

    @Override
    public Group get(ObmDomain domain, GroupExtId id) throws DaoException, GroupNotFoundException {
    	Connection conn = null;
    	try {
    		conn = connectionProvider.getConnection();
	    	return this.getGroupBuilder(conn, domain, id).build();
    	} catch (SQLException e) {
    		throw new DaoException(e);
    	} finally {
    		JDBCUtils.cleanup(conn, null, null);
    	}
    }
    		
    private Group.Builder getGroupBuilder(Connection conn, ObmDomain domain, GroupExtId id) throws SQLException, GroupNotFoundException {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
	        
	        ps = conn.prepareStatement(
	                "      SELECT group_name, group_desc" +
	                "        FROM UGroup " +
	                "  INNER JOIN Domain ON group_domain_id = domain_id" +
	                "       WHERE domain_uuid = ?" +
	                "         AND group_ext_id = ?" +
	                "       LIMIT 1");
	        ps.setString(1, domain.getUuid().get());
	        ps.setString(2, id.getId());
	        rs = ps.executeQuery();
	        Group.Builder groupBuilder = Group.builder();
	        
	        if (rs.next()) {
	        	return groupBuilder
			        	    .extId(id)
			        	    .name(rs.getString("group_name"))
			        	    .description(rs.getString("group_desc"));
	        } else {
	        	throw new GroupNotFoundException(id);
	        }
    	} finally {
			JDBCUtils.cleanup(null, ps, rs);
		}
    }
    
    @Override
    public Group getRecursive(ObmDomain domain, GroupExtId id, boolean includeUsers, int groupDepth) throws DaoException, GroupNotFoundException {
    	Group.Builder groupBuilder = Group.builder();
    	Connection conn = null;
    	
    	try {
    		conn = connectionProvider.getConnection();
    		return buildRecursiveGroup(groupBuilder, conn, domain, id, includeUsers, groupDepth);
    	} catch (SQLException e) {
    		throw new DaoException(e);
    	} finally {
    		JDBCUtils.cleanup(conn, null, null);
    	}
    }
    
    private Group buildRecursiveGroup(Group.Builder groupBuilder, Connection conn, ObmDomain domain, GroupExtId id, boolean includeUsers, int groupDepth) throws SQLException, GroupNotFoundException {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	
    	int internalGroupId = getInternalGroupId(conn, domain, id);
    	
    	if (includeUsers) {
    		try {
    			ps = conn.prepareStatement(
	 	                "      SELECT userobmgroup_userobm_id" +
	 	                "        FROM UserObmGroup " +
	 	                "  INNER JOIN UGroup  ON userobmgroup_group_id = group_id" +
	 	                "  INNER JOIN Domain  ON group_domain_id = domain_id" +
	 	                "       WHERE group_id = ?" +
	 	                "         AND domain_uuid = ?");
    			ps.setInt(1, internalGroupId);
    			ps.setString(2, domain.getUuid().get());
    			rs = ps.executeQuery();
	 	        
	 	        while (rs.next()) {
	 	        	ObmUser user = userDao.findUserById(rs.getInt("userobmgroup_userobm_id"), domain);
	 	        	if (user != null) {
	 	        		groupBuilder.user(user);
	 	        	}
	 	        }
    		} finally {
    			JDBCUtils.cleanup(null, ps, rs);
    		}
    	}

    	if (groupDepth != 0) {
    		int childDepth = (groupDepth > 0 ? groupDepth - 1 : -1);
        	
        	
        	try {
	 	        ps = conn.prepareStatement(
	 	                "      SELECT groupgroup_child_id" +
	 	                "        FROM GroupGroup " +
	 	                "  INNER JOIN UGroup ON groupgroup_parent_id = group_id" +
	 	                "  INNER JOIN Domain ON group_domain_id = domain_id" +
	 	                "       WHERE group_id = ?" +
	 	                "         AND domain_uuid = ?");
	 	        
	 	        ps.setInt(1, internalGroupId);
	 	        ps.setString(2, domain.getUuid().get());
	 	        rs = ps.executeQuery();
	 	        
	 	        while (rs.next()) {
	 	        	GroupExtId subid = GroupExtId.of(rs.getString("groupgroup_child_id")); 
	 	        	Group.Builder subgroup = getGroupBuilder(conn, domain, subid);
	 	        	groupBuilder.subgroup(buildRecursiveGroup(subgroup, conn, domain, subid, includeUsers, childDepth));
	 	        }
        	} finally {
        		JDBCUtils.cleanup(null, ps, rs);
        	}
        	
    	}
    	
    	return groupBuilder.build();
    }

    @Override
    public Group create(ObmDomain domain, Group info) throws DaoException {
      	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn =  connectionProvider.getConnection();
	        ps = conn.prepareStatement(
	                " INSERT INTO UGroup" +
	                "             (group_domain_id, group_ext_id, group_name, group_desc)" +
	                "      VALUES " +
	                "             (SELECT domain_id FROM Domain WHERE domain_uuid = ?," +
	                "              ?, ?, ?)");
	        
	        ps.setString(1, domain.getUuid().get());
	        ps.setString(2, info.getExtId().getId());
	        ps.setString(3, info.getName());
	        ps.setString(4, info.getDescription());
	        ps.executeUpdate();	        
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, ps, null);
		}
    	
    	try {
    		return get(domain, info.getExtId());
    	} catch (GroupNotFoundException e) {
    		// This shouldn't happen
    		return null;
    	}
    }

    @Override
    public Group update(ObmDomain domain, Group info) throws DaoException, GroupNotFoundException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
	    	conn =  connectionProvider.getConnection();
	        ps = conn.prepareStatement(
	                "      UPDATE UGroup" +
	                "		  SET group_name = ?," +
	                "			  group_desc = ?" +
	                "		WHERE group_ext_id = ?" +
	                "         AND group_domain_id = (SELECT domain_id FROM Domain WHERE domain_uuid = ?)");
	        
	        
	        ps.setString(1, info.getName());
	        ps.setString(2, info.getDescription());
	        ps.setString(3, info.getExtId().getId());
	        ps.setString(4, domain.getUuid().get());
	        
	        if (ps.executeUpdate() < 1) {
	        	throw new GroupNotFoundException(info.getExtId());
	        }
    	} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, ps, null);
		}
    	
    	return get(domain, info.getExtId());
    }

    @Override
    public void delete(ObmDomain domain, GroupExtId id) throws DaoException, GroupNotFoundException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
	    	conn =  connectionProvider.getConnection();
	        ps = conn.prepareStatement(
	                " DELETE FROM UGroup" +
	                "		WHERE group_ext_id = ?" +
	                "         AND group_domain_id = (SELECT domain_id FROM Domain WHERE domain_uuid = ?)");
	        
	        
	        ps.setString(1, id.getId());
	        ps.setString(2, domain.getUuid().get());
	        
	        if (ps.executeUpdate() < 1) {
	        	throw new GroupNotFoundException(id);
	        }
    	} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, ps, null);
		}
    }
    
    private int getInternalGroupId(Connection conn, ObmDomain domain, GroupExtId extId) throws SQLException, GroupNotFoundException {
		PreparedStatement ps = null;
		ResultSet rs = null;
	    try {	
	    	ps = conn.prepareStatement(
	    			"      SELECT group_id " +
	    			"        FROM UGroup " +
	    			"  INNER JOIN Domain ON group_domain_id = domain_id" +
	    			"       WHERE group_ext_id = ?" +
	    			"         AND domain_ext_id = ?" +
	    			"       LIMIT 1");

	    	ps.setString(1, extId.getId());
	    	ps.setString(2, domain.getUuid().get());
	    	rs = ps.executeQuery();
	    	if (rs.next()) {
	    		return rs.getInt("group_id");
	    	} else {
	    		throw new GroupNotFoundException(extId);
	    	}
    	} finally {
    		JDBCUtils.cleanup(null, ps, rs);
    	}
    }

    @Override
    public void addUser(ObmDomain domain, GroupExtId id, ObmUser user) throws DaoException, GroupNotFoundException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
	    	conn = connectionProvider.getConnection();
	    	int internalId = getInternalGroupId(conn, domain, id);
	        ps = conn.prepareStatement(
	                " INSERT INTO UserObmGroup" +
	                "		      (userobmgroup_group_id, userobmgroup_user_id)" +
	                "      VALUES (?,?)");
	                
	        ps.setInt(1, internalId);
	        ps.setInt(2, user.getUid());
	        ps.executeUpdate();
    	} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, ps, null);
		}
    }

    @Override
    public void addSubgroup(ObmDomain domain, GroupExtId id, Group subgroup) throws DaoException, GroupNotFoundException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = connectionProvider.getConnection();
	    	int internalId = getInternalGroupId(conn, domain, id);
	        ps = conn.prepareStatement(
	                " INSERT INTO GroupGroup" +
	                "		      (groupgroup_parent_id, groupgroup_child_id)" +
	                "      VALUES (?,?)");

	        ps.setInt(1, internalId);
	        ps.setInt(2, getInternalGroupId(conn, domain, subgroup.getExtId())); 
	        ps.executeUpdate();
    	} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, ps, null);
		}   
    }

    @Override
    public void removeUser(ObmDomain domain, GroupExtId id, ObmUser user)  throws DaoException, GroupNotFoundException, UserNotFoundException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
	    	conn = connectionProvider.getConnection();
	    	int internalId = getInternalGroupId(conn, domain, id);
	        ps = conn.prepareStatement(
	                " DELETE FROM UserObmGroup" +
	                "		WHERE userobmgroup_group_id = ?" +
	                "         AND userobmgroup_user_id = ?");
      
	        ps.setInt(1, internalId);
	        ps.setInt(2, user.getUid());  
	        if (ps.executeUpdate() < 1) {
	        	// The GroupNotFound case is already handled in
	        	// getInternalGroupId
	        	throw new UserNotFoundException(user.getExtId());
	        }
    	} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, ps, null);
		}    
    }

    @Override
    public void removeSubgroup(ObmDomain domain, GroupExtId id, Group subgroup)  throws DaoException, GroupNotFoundException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	try {
    		conn = connectionProvider.getConnection();
	    	int internalId = getInternalGroupId(conn, domain, id);
	        ps = conn.prepareStatement(
	                " DELETE FROM GroupGroup" +
	                "		WHERE groupgroup_parent_id = ?" +
	                "         AND groupgroup_child_id = ?");

	        ps.setInt(1, internalId);
	        ps.setInt(2, getInternalGroupId(conn, domain, subgroup.getExtId())); 
	        ps.executeUpdate();
    	} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, ps, null);
		}
    }
}

package fr.aliacom.obm.common.calendar.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Set;

import org.obm.push.utils.jdbc.IntegerSQLCollectionHelper;
import org.obm.sync.calendar.ResourceInfo;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ResourceLoader {

	public static class Builder {
		private Set<Integer> ids;
		private Connection conn;

		private Builder() {
			ids = Sets.newHashSet();
			conn = null;
		}

		public Builder connection(Connection conn) {
			this.conn = conn;
			return this;
		}

		public Builder ids(int... ids) {
			for (int id : ids) {
				this.ids.add(new Integer(id));
			}
			return this;
		}

		public ResourceLoader build() {
			Preconditions.checkState(conn != null, "The connection parameter is mandatory");
			Preconditions.checkState(!ids.isEmpty(), "The ids parameter is mandatory");
			return new ResourceLoader(conn, ids);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private Connection conn;
	private Set<Integer> ids;

	private ResourceLoader(Connection conn, Set<Integer> ids) {
		this.conn = conn;
		this.ids = ids;
	}

	public Collection<ResourceInfo> load() throws SQLException {
		IntegerSQLCollectionHelper idsHelper;
		if (!ids.isEmpty()) {
			idsHelper = new IntegerSQLCollectionHelper(ids);
		} else {
			idsHelper = null;
		}
		String query = buildQuery(idsHelper);
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(query);
			setParameters(stat, idsHelper);
			rs = stat.executeQuery();
			return buildResources(rs);
		} finally {
			cleanup(rs, stat);
		}
	}

	private String buildQuery(IntegerSQLCollectionHelper idsHelper) {
		return String.format("SELECT r.resource_id, r.resource_name, r.resource_email "
				+ "FROM Resource r "
				+ "WHERE r.resource_id IN (%s)", idsHelper.asPlaceHolders());
	}

	private void setParameters(PreparedStatement stat, IntegerSQLCollectionHelper idsHelper)
			throws SQLException {
		idsHelper.insertValues(stat, 1);
	}

	private Collection<ResourceInfo> buildResources(ResultSet rs) throws SQLException {
		Collection<ResourceInfo> resources = Lists.newArrayList();
		while (rs.next()) {
			ResourceInfo resource = buildResource(rs);
			resources.add(resource);
		}
		return resources;
	}

	private ResourceInfo buildResource(ResultSet rs) throws SQLException {
		return ResourceInfo.builder().id(rs.getInt(1)).name(rs.getString(2)).mail(rs.getString(3))
				.read(false).write(false).build();
	}

	private void cleanup(ResultSet rs, Statement stat) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Throwable t) {
			}
		}
		if (stat != null) {
			try {
				stat.close();
			} catch (Throwable t) {
			}
		}
	}
}

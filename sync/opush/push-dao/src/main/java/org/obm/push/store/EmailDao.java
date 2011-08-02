package org.obm.push.store;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.obm.push.bean.Email;

public interface EmailDao {

	void addMessages(Integer devId, Integer collectionId,
			Collection<Email> emails) throws SQLException;

	void addMessages(Integer devId, Integer collectionId, Date lastSync,
			Collection<Email> messages) throws SQLException;

	void removeMessages(Integer devId, Integer collectionId,
			Collection<Long> mailUids) throws SQLException;

	void removeMessages(Integer devId, Integer collectionId, Date lastSync,
			Collection<Long> uids) throws SQLException;
	
	Set<Email> getSyncedMail(Integer devId, Integer collectionId);

	Set<Long> getDeletedMail(Integer devId, Integer collectionId, Date lastSync);

	Set<Email> getUpdatedMail(Integer devId, Integer collectionId,
			Date updatedFrom);
}

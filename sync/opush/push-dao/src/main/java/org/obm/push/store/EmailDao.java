package org.obm.push.store;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.obm.push.bean.Email;
import org.obm.push.exception.DaoException;

public interface EmailDao {

	void addMessages(Integer devId, Integer collectionId,
			Collection<Email> emails) throws DaoException;

	void addMessages(Integer devId, Integer collectionId, Date lastSync,
			Collection<Email> messages) throws DaoException;

	void removeMessages(Integer devId, Integer collectionId,
			Collection<Long> mailUids) throws DaoException;

	void removeMessages(Integer devId, Integer collectionId, Date lastSync,
			Collection<Long> uids) throws DaoException;
	
	Set<Email> getSyncedMail(Integer devId, Integer collectionId) throws DaoException;

	Set<Long> getDeletedMail(Integer devId, Integer collectionId, Date lastSync) throws DaoException;

	Set<Email> getUpdatedMail(Integer devId, Integer collectionId, Date updatedFrom) throws DaoException;
	
}

package org.obm.push.store;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.obm.push.bean.Email;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailNotFoundException;

public interface EmailDao {

	void markEmailsAsSynced(Integer devId, Integer collectionId, Collection<Email> emails) throws DaoException;

	void markEmailsAsSynced(Integer devId, Integer collectionId, Date lastSync, Collection<Email> messages) throws DaoException;

	void deleteSyncEmails(Integer devId, Integer collectionId, Collection<Long> mailUids) throws DaoException;

	void deleteSyncEmails(Integer devId, Integer collectionId, Date lastSync, Collection<Long> uids) throws DaoException;
	
	Set<Email> listSyncedEmails(Integer devId, Integer collectionId, SyncState state) throws DaoException;

	Set<Long> getDeletedMail(Integer devId, Integer collectionId, Date lastSync) throws DaoException;

	Email getSyncedEmail(Integer devId, Integer collectionId, long uid) throws DaoException, EmailNotFoundException;

	Set<Email> filterSyncedEmails(int collectionId, int device, Collection<Email> emails) throws DaoException;
	
	void update(Integer devId, Integer collectionId, Email email) throws DaoException;

	void insert(Integer devId, Integer collectionId, Date lastSync, Email email) throws DaoException;

	Set<Email> listDeletedEmails(Integer devId, Integer collectionId) throws DaoException;
	
}

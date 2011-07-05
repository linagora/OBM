package org.obm.push;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.FolderBackend;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.mail.MailBackend;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HierarchyExporter implements IHierarchyExporter {

	private static final Logger logger = LoggerFactory
			.getLogger(HierarchyExporter.class);

	private FolderBackend folderExporter;
	private MailBackend mailExporter;
	private CalendarBackend calendarExporter;
	private ContactsBackend contactsBackend;

	@Inject
	private HierarchyExporter(FolderBackend folderExporter,
			MailBackend mailExporter, CalendarBackend calendarExporter,
			ContactsBackend contactsBackend) {
		
		this.folderExporter = folderExporter;
		this.mailExporter = mailExporter;
		this.calendarExporter = calendarExporter;
		this.contactsBackend = contactsBackend;
	}

	@Override
	public void configure(SyncState state, String dataClass,
			Integer filterType, int i, int j) {
		logger.info("configure(bs, " + dataClass + ", " + filterType + ", "
				+ state + ", " + i + ", " + j + ")");
		if (dataClass != null) {
			state.setDataType(PIMDataType.valueOf(dataClass.toUpperCase()));
		} else {
			state.setDataType(null);
		}
	}

	private List<ItemChange> getContactsChanges(BackendSession bs) {
		return contactsBackend.getHierarchyChanges(bs);
	}

	private List<ItemChange> getTasksChanges(BackendSession bs) {
		return calendarExporter.getHierarchyTaskChanges(bs);
	}

	private List<ItemChange> getCalendarChanges(BackendSession bs) {
		return calendarExporter.getHierarchyChanges(bs);
	}

	private List<ItemChange> getMailChanges(BackendSession bs) {
		return mailExporter.getHierarchyChanges(bs);
	}

	@Override
	public List<ItemChange> getChanged(BackendSession bs) {
		folderExporter.synchronize(bs);
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		changes.addAll(getCalendarChanges(bs));
		changes.addAll(getMailChanges(bs));
		changes.addAll(getContactsChanges(bs));
		changes.addAll(getTasksChanges(bs));
		return changes;
	}

	@Override
	public int getCount(BackendSession bs) {
		return getChanged(bs).size();
	}

	@Override
	public List<ItemChange> getDeleted(BackendSession bs) {
		return new LinkedList<ItemChange>();
	}

	@Override
	public int getRootFolderId(BackendSession bs) throws ActiveSyncException {
		return folderExporter.getServerIdFor(bs);
	}

	@Override
	public String getRootFolderUrl(BackendSession bs) {
		return folderExporter.getColName(bs);
	}

}

package org.obm.sync.push.client;

public enum FolderType {

	USER_FOLDER_GENERIC, // 1
	DEFAULT_INBOX_FOLDER, // 2
	DEFAULT_DRAFTS_FOLDERS, // 3
	DEFAULT_DELETED_ITEMS_FOLDERS, // 4
	DEFAULT_SENT_EMAIL_FOLDER, // 5
	DEFAULT_OUTBOX_FOLDER, // 6
	DEFAULT_TASKS_FOLDER, // 7
	DEFAULT_CALENDAR_FOLDER, // 8
	DEFAULT_CONTACTS_FOLDER, // 9
	DEFAULT_NOTES_FOLDER, // 10
	DEFAULT_JOURNAL_FOLDER, // 11
	USER_CREATED_EMAIL_FOLDER, // 12
	USER_CREATED_CALENDAR_FOLDER, // 13
	USER_CREATED_CONTACTS_FOLDER, // 14
	USER_CREATED_TASKS_FOLDER, // 15
	USER_CREATED_JOURNAL_FOLDER, // 16
	USER_CREATED_NOTES_FOLDER, // 17
	UNKNOWN_FOLDER_TYPE; // 18
	
	public static FolderType getValue(int type){
		switch (type) {
		case 1:
			return USER_FOLDER_GENERIC;
		case 2:
			return DEFAULT_INBOX_FOLDER;
		case 3:
			return DEFAULT_DRAFTS_FOLDERS;
		case 4:
			return DEFAULT_DELETED_ITEMS_FOLDERS;
		case 5:
			return DEFAULT_SENT_EMAIL_FOLDER;
		case 6:
			return DEFAULT_OUTBOX_FOLDER;
		case 7:
			return DEFAULT_TASKS_FOLDER;
		case 8:
			return DEFAULT_CALENDAR_FOLDER;
		case 9:
			return DEFAULT_CONTACTS_FOLDER;
		case 10:
			return DEFAULT_NOTES_FOLDER;
		case 11:
			return DEFAULT_JOURNAL_FOLDER;
		case 12:
			return USER_CREATED_EMAIL_FOLDER;
		case 13:
			return USER_CREATED_CALENDAR_FOLDER;
		case 14:
			return USER_CREATED_CONTACTS_FOLDER;
		case 15:
			return USER_CREATED_TASKS_FOLDER;
		case 16:
			return USER_CREATED_JOURNAL_FOLDER;
		case 17:
			return USER_CREATED_NOTES_FOLDER;
		case 18:
			return UNKNOWN_FOLDER_TYPE;
		default:
			return null;
		}
	}

	public String asIntString() {
		switch (this) {
		case USER_FOLDER_GENERIC:
			return "1";
		case DEFAULT_INBOX_FOLDER:
			return "2";
		case DEFAULT_DRAFTS_FOLDERS:
			return "3";
		case DEFAULT_DELETED_ITEMS_FOLDERS:
			return "4";
		case DEFAULT_SENT_EMAIL_FOLDER:
			return "5";
		case DEFAULT_OUTBOX_FOLDER:
			return "6";
		case DEFAULT_TASKS_FOLDER:
			return "7";
		case DEFAULT_CALENDAR_FOLDER:
			return "8";
		case DEFAULT_CONTACTS_FOLDER:
			return "9";
		case DEFAULT_NOTES_FOLDER:
			return "10";
		case DEFAULT_JOURNAL_FOLDER:
			return "11";
		case USER_CREATED_EMAIL_FOLDER:
			return "12";
		case USER_CREATED_CALENDAR_FOLDER:
			return "13";
		case USER_CREATED_CONTACTS_FOLDER:
			return "14";
		case USER_CREATED_TASKS_FOLDER:
			return "15";
		case USER_CREATED_JOURNAL_FOLDER:
			return "16";
		case USER_CREATED_NOTES_FOLDER:
			return "17";
		default:
			return "18";
		}
	}

}

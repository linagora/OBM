package org.obm.configuration;



import com.google.inject.Singleton;

@Singleton
public class ContactConfiguration extends AbstractConfigurationService{
	
	private static final String DEFAULT_ADDRESS_BOOK_NAME = "contacts";
	private static final String COLLECTED_ADDRESS_BOOK_NAME = "collected_contacts";

	private static final int ADDRESS_BOOK_USER_ID = -1;
	private static final String ADDRESS_BOOK_USERS_NAME = "users";
	
	private final static String DEFAULT_PARENT_ID = "0";

	protected ContactConfiguration() {
		super();
	}
	
	public String getDefaultAddressBookName() {
		return DEFAULT_ADDRESS_BOOK_NAME;
	}
	
	public String getCollectedAddressBookName() {
		return COLLECTED_ADDRESS_BOOK_NAME;
	}
	
	public int getAddressBookUserId() {
		return ADDRESS_BOOK_USER_ID;
	}
	
	public String getAddressBookUsersName() {
		return ADDRESS_BOOK_USERS_NAME;
	}
	
	public String getDefaultParentId() {
		return DEFAULT_PARENT_ID;
	}

}
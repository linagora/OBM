package org.obm.configuration;



import com.google.inject.Singleton;

@Singleton
public class ContactConfiguration extends AbstractConfigurationService{
	
	private static final String DEFAULT_ADDRESS_BOOK_NAME = "contacts";
	private static final String COLLECTED_ADDRESS_BOOK_NAME = "collected_contacts";

	protected ContactConfiguration() {
		super();
	}
	
	public String getDefaultAddressBookName() {
		return DEFAULT_ADDRESS_BOOK_NAME;
	}
	
	public String getCollectedAddressBookName() {
		return COLLECTED_ADDRESS_BOOK_NAME;
	}

}

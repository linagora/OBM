package fr.aliacom.obm.freebusy;

/**
 * Local provider. Essentially exists to let Guice bind correctly both local and
 * remote providers.
 * 
 * @see RemoteFreeBusyProvider
 */
public interface LocalFreeBusyProvider extends FreeBusyProvider {
	// Dummy comment to appease Eclipse
}

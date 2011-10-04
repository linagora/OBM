package fr.aliacom.obm.freebusy;

/**
 * Local provider. Essentially exists to let Guice bind correctly both local and
 * remote providers.
 * 
 * @see LocalFreeBusyProvider
 */
public interface RemoteFreeBusyProvider extends FreeBusyProvider {
	// Dummy comment to appease Eclipse
}

package fr.aliacom.obm.freebusy;

import com.google.inject.Module;

/**
 * <p>
 * Classes implementing this module should bind an associated
 * {@link FreeBusyProvider} implementation using
 * {@link com.google.inject.multibindings.Multibinder}.
 * </p>
 * 
 * <p>
 * The associated {@link FreeBusyProvider} implementation will be used to do
 * remote lookup by the FreeBusy servlet.
 * </p>
 */
public interface FreeBusyPluginModule extends Module, Comparable<FreeBusyPluginModule> {
	/**
	 * Returns an integer giving the module a priority. This information
	 * determines the order in which the associated {@link FreeBusyProvider}
	 * implementation will be consulted by the FreeBusy servlet. The higher the
	 * priority the better.
	 * 
	 * @return an integer representing a priority.
	 */
	public int getPriority();
}

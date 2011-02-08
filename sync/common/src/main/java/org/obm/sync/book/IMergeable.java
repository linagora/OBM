package org.obm.sync.book;

/**
 * Interface for objects supporting merge at sync time.
 */
public interface IMergeable {

	/**
	 * Merge the previous (stored in db) data into the new one coming from the
	 * sync client.
	 * 
	 * @param previous
	 */
	void merge(IMergeable previous);

}

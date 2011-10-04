package org.obm.push.utils.index;

import java.util.ArrayList;
import java.util.Collection;

public class IndexUtils {

	public static 
		<T extends Number, I extends Indexed<T>, C extends Collection<I>> 
			ArrayList<T> listIndexes(C objects) {
		
		ArrayList<T> indexes = new ArrayList<T>();
		for (Indexed<T> object: objects) {
			indexes.add(object.getIndex());
		}
		return indexes;
	}
	
}

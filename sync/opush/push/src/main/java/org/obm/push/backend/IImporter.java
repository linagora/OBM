package org.obm.push.backend;

import java.util.List;

import org.obm.push.ItemChange;

public interface IImporter {

	void addChanges(List<ItemChange> lic);

	void addDeletions(List<ItemChange> lic);

}

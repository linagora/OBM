package org.obm.opush;

import org.obm.opush.env.AbstractOverrideModule;
import org.obm.push.IContentsExporter;

public class ModuleUtils {

	public static AbstractOverrideModule buildContentsExporterBackendModule() {
		AbstractOverrideModule contentsExporterBackend = new AbstractOverrideModule() {

			@Override
			protected void configureImpl() {
				bindWithMock(IContentsExporter.class);
			}
		};
		return contentsExporterBackend;
	}
}

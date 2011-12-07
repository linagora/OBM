package org.obm.opush;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.HashSet;

import org.obm.opush.env.AbstractOpushEnv;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.backend.OBMBackend;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class PingHandlerTestModule extends AbstractOpushEnv {
	
	public PingHandlerTestModule() {
		super();
	}
	
	@Override
	protected Module overrideModule() throws Exception {
		
		Module overrideModule = super.overrideModule();
		
		Module obmBackend = new AbstractModule() {
			
			@Override
			protected void configure() {
				try {
					OBMBackend obmBackend = createMock(OBMBackend.class);
					bind(OBMBackend.class).toInstance(obmBackend);
					mockObmBackend(obmBackend);
					replay(obmBackend);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		return Modules.combine(overrideModule, obmBackend);
	}

	private OBMBackend mockObmBackend(OBMBackend obmBackend) throws CollectionNotFoundException, ProcessingEmailException, DaoException, UnknownObmSyncServerException, AuthFault {
		expect(obmBackend.login(
				anyObject(String.class), anyObject(String.class)))
				.andReturn(new AccessToken(1, "opush")).anyTimes();
		IListenerRegistration listenerRegistration = createMock(IListenerRegistration.class);
		expect(obmBackend.addChangeListener(
				anyObject(ICollectionChangeListener.class)))
				.andReturn(listenerRegistration).anyTimes();
		expect(obmBackend.getChangesSyncCollections(
				anyObject(ICollectionChangeListener.class)))
				.andReturn(new HashSet<SyncCollection>()).anyTimes();
		return obmBackend;
	}


}
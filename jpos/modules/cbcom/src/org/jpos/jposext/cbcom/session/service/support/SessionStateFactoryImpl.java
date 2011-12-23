package org.jpos.jposext.cbcom.session.service.support;

import java.util.HashMap;
import java.util.Map;

import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.ISessionStateFactory;

/**
 * Implementation based on a pre-populated map of state instances
 * 
 * @author dgrandemange
 * 
 */
public class SessionStateFactoryImpl implements ISessionStateFactory {

	private Map<Class<? extends IPseudoSessionState>, IPseudoSessionState> mapStateByClazz;

	private Class<? extends IPseudoSessionState> initialStateClazz;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.ISessionStateFactory#create
	 * (java.lang.Class)
	 */
	public IPseudoSessionState create(Class<? extends IPseudoSessionState> clazz) {
		if (null != mapStateByClazz) {
			return mapStateByClazz.get(clazz);
		}

		return null;
	}

	/**
	 * Helper method that populate map using the state classes passed as an
	 * array
	 * 
	 * @param tabClazz
	 *            Array of state classes to use for map population
	 */
	public void setAvailableStates(
			Class<? extends IPseudoSessionState>[] tabClazz,
			Class<? extends IPseudoSessionState> initialStateClazz) {
		Map<Class<? extends IPseudoSessionState>, IPseudoSessionState> mapStateByClazzInter = new HashMap<Class<? extends IPseudoSessionState>, IPseudoSessionState>();
		try {

			for (Class<? extends IPseudoSessionState> clazz : tabClazz) {
				mapStateByClazzInter.put(clazz, clazz.newInstance());
			}

			this.mapStateByClazz = mapStateByClazzInter;
			this.initialStateClazz = initialStateClazz;
			if (!(mapStateByClazz.containsKey(initialStateClazz))) {
				throw new RuntimeException(
						String
								.format(
										"Initial state [%s] is not available in registered states",
										initialStateClazz.getSimpleName()));
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.service.ISessionStateFactory#getInitialState
	 * ()
	 */
	public IPseudoSessionState getInitialState() {
		return mapStateByClazz.get(initialStateClazz);
	}

}

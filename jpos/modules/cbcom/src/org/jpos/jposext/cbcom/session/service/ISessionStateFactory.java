package org.jpos.jposext.cbcom.session.service;


/**
 * Session state factory
 * 
 * @author dgrandemange
 *
 */
public interface ISessionStateFactory {
	
	/**
	 * @param clazz Class of session state to get
	 * @return An instance of the specified session state class 
	 */
	IPseudoSessionState create(Class<? extends IPseudoSessionState> clazz);
	
	/**
	 * @return Initial state
	 */
	IPseudoSessionState getInitialState();
}

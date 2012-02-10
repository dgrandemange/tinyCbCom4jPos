package org.jpos.jposext.cbcom.exception;

/**
 * Exception raised typically when a transition is unexpectedly made against current
 * pseudo session state<BR>
 * 
 * @author dgrandemange
 * 
 */
public class CBCOMSessionStateException extends RuntimeException {

	private String state;

	private String transition;

	public CBCOMSessionStateException(String state, String transition) {
		this.state = state;
		this.transition = transition;
	}

	public String getState() {
		return state;
	}

	public String getTransition() {
		return transition;
	}

}

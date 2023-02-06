package org.xandercat.swing.file;

/**
 * SaveOnCloseObjectFinalizer can be used with a SaveOnCloseAction to finalize the state
 * of an Object before it is saved by the SaveOnCloseAction.
 * 
 * @author Scott C Arnold
 */
public interface SaveOnCloseObjectFinalizer<T> {

	/**
	 * Finalize the state of the Object.
	 */
	public void finalizeObjectState(T object);
}

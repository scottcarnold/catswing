package org.xandercat.swing.file;

/**
 * Listener for FileManager events, including the following:
 * <ul>
 *   <li>Before a file is saved or closed.</li>
 *   <li>After a file is closed.</li>
 *   <li>When a file name or path changes.</li>
 * </ul>
 * 
 * @author Scott Arnold
 *
 * @param <T>
 */
public interface FileManagerListener<T> {

	/**
	 * Event fired immediately before a save or close action.
	 * 
	 * @param toSave	object type being saved
	 */
	public void beforeSaveOrClose(T toSave);
	
	/**
	 * Event fired immediately after a file is opened.
	 * 
	 * @param key		key for file opened
	 */
	public void afterOpen(String key);
	
	/**
	 * Event fired immediately after a file is closed.
	 */
	public void afterClose();
	
	/**
	 * Event fired any time a file name or it's path changes.  
	 * 
	 * @param newAbsolutePath	new path to file
	 */
	public void filePathChange(String newAbsolutePath);
}

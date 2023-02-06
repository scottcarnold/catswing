package org.xandercat.swing.file;

/**
 * AutoSaveListener can be implemented by any class that wishes to be notifed each time an autosave is performed.
 * An AutoSaveListener is used with an AutoSaver.
 * 
 * @author Scott C Arnold.
 */
public interface AutoSaveListener {

	public void autoSaveExecuted();
}

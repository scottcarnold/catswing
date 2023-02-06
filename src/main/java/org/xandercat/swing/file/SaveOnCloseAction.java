package org.xandercat.swing.file;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.app.CloseListener;

/**
 * SaveOnCloseAction instances can be added to an ApplicationFrame to save an Object to a File
 * when the application is closing (for state saving operations).  If any work needs to be performed
 * before the actual save occurs, a SaveOnCloseObjectFinalizer can be used (to finalize the state
 * of the Object before it is saved).
 *   
 * @author Scott C Arnold
 */
public class SaveOnCloseAction<T> implements CloseListener {
	
	private static final Logger log = LogManager.getLogger(SaveOnCloseAction.class);
	
	private T object;
	private File file;
	private SaveOnCloseObjectFinalizer<T> finalizer;
	
	public SaveOnCloseAction(File file, T object) {
		this.object = object;
		this.file = file;
	}
	
	public SaveOnCloseAction(File file, T object, SaveOnCloseObjectFinalizer<T> finalizer) {
		this(file, object);
		this.finalizer = finalizer;
	}
	
	public boolean closeAction(WindowEvent event) {
		try {
			if (this.finalizer != null) {
				log.debug("Finalizing object state...");
				this.finalizer.finalizeObjectState(object);
			}
			log.debug("Saving data from " + this.object.getClass().getName() + " to " + this.file.getAbsolutePath() + " ...");
			FileManager.saveObject(file, object);
		} catch (IOException ioe) {
			log.error("Unable to save.", ioe);
		}
		return true;
	}
}

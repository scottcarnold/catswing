package org.xandercat.swing.file;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * AutoSaver is a thread that periodically calls the auto save method of an AutoSavable object.
 * AutoSaver will also notify any AutoSaveListeners registered with it each time an auto save is performed.
 * 
 * @author Scott C Arnold
 */
public class AutoSaver extends Thread {

	private static final Logger log = LogManager.getLogger(AutoSaver.class);
			
	private AutoSavable autoSavable;
	private long interval;
	private boolean stop;
	private List<AutoSaveListener> listeners;
	
	public AutoSaver(AutoSavable autoSavable, long interval) {
		this.autoSavable = autoSavable;
		this.interval = interval;
	}
	
	public void addAutoSaveListener(AutoSaveListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<AutoSaveListener>();
		}
		listeners.add(listener);
	}
	
	public void removeAutoSaveListener(AutoSaveListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
	
	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException ie) {
				log.warn("AutoSaver interrupted.", ie);
			}
			if (!stop) {
				autoSavable.autoSave();
				for (AutoSaveListener listener : listeners) {
					listener.autoSaveExecuted();
				}
			}
		}
	}
	
	public void stopRun() {
		stop = true;
	}

}

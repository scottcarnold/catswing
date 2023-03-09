package org.xandercat.swing.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.file.FileCopier.CopyResult;

/**
 * SwingFileCopier is a wrapper for FileCopier for use in Swing applications; SwingFileCopier runs
 * as a background thread; listener events are executed on the event dispatch thread.
 * 
 * @author Scott C Arnold
 */
public class SwingFileCopier extends SwingWorker<Void, SwingFileCopierEvent> implements FileCopyListener, FileCopyProgressListener {

	private static final Logger log = LogManager.getLogger(SwingFileCopier.class);
			
	private FileCopier fileCopier;
	private List<FileCopyListener> fileCopyListeners = new ArrayList<FileCopyListener>();
	private List<FileCopyProgressListener> fileCopyProgressListeners = new ArrayList<FileCopyProgressListener>(); 
	
	public SwingFileCopier(List<File> files, FileCopierPathGenerator pathGenerator) {
		this.fileCopier = new FileCopier(files, pathGenerator);
		this.fileCopier.addFileCopyListener(this);
		this.fileCopier.addFileCopyProgressListener(this);
	}
	
	public SwingFileCopier(List<File> files, File destination, File source) {
		this.fileCopier = new FileCopier(files, destination, source);
		this.fileCopier.addFileCopyListener(this);
		this.fileCopier.addFileCopyProgressListener(this);
	}
	
	public void addFileCopyListener(FileCopyListener listener) {
		this.fileCopyListeners.add(listener);
	}
	
	public void removeFileCopyListener(FileCopyListener listener) {
		this.fileCopyListeners.remove(listener);
	}
	
	public void addFileCopyProgressListener(FileCopyProgressListener listener) {
		this.fileCopyProgressListeners.add(listener);
	}
	
	public List<File> getCopiedFiles() {
		return fileCopier.getCopiedFiles();
	}
	
	public List<File> getOverwriteFiles() {
		return fileCopier.getOverwriteFiles();
	}
	
	public List<FileCopier.FileCopyError> getErrorFiles() {
		return fileCopier.getErrorFiles();
	}
	
	public Throwable getFileCopyException(File file) {
		return fileCopier.getFileCopyException(file);
	}
	
	public List<File> getSkippedFiles() {
		return fileCopier.getSkippedFiles();
	}
	
	public void resolveError(File file, boolean retry) {
		fileCopier.resolveError(file, retry);
	}
	
	public void resolveOverwrite(File file, boolean overwrite) {
		fileCopier.resolveOverwrite(file, overwrite);
	}
	
	public void enableTestMode() {
		fileCopier.enableTestMode();
	}
	
	public void cancel() {
		fileCopier.cancel();
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		try {
			this.fileCopier.copy();
		} catch (Exception e) {
			log.error("Error while copying files", e);
		}
		return null;
	}

	@Override
	protected void process(List<SwingFileCopierEvent> eventList) {
		for (SwingFileCopierEvent event : eventList) {
			switch (event.getType()) {
			case COPYING:
				fireFileCopying(event);
				break;
			case COPIED:
				fireFileCopied(event);
				break;
			case COPY_COMPLETE:
				fireCopyComplete(event);
				break;
			case COPY_PROGRESS:
				fireFileCopyProgress(event);
				break;
			}
		}
	}

	private void fireCopyComplete(SwingFileCopierEvent event) {
		for (FileCopyListener listener : fileCopyListeners) {
			listener.copyComplete(event.isResolutionRequired(), event.isCopyCancelled());
		}
	}
	
	private void fireFileCopied(SwingFileCopierEvent event) {
		for (FileCopyListener listener : fileCopyListeners) {
			listener.fileCopied(event.getFrom(), event.getTo(), event.getCopyResult());
		}
	}
	
	private void fireFileCopying(SwingFileCopierEvent event) {
		for (FileCopyListener listener : fileCopyListeners) {
			listener.fileCopying(event.getFrom(), event.getTo());
		}
	}
	
	private void fireFileCopyProgress(SwingFileCopierEvent event) {
		for (FileCopyProgressListener listener : fileCopyProgressListeners) {
			listener.fileCopying(event.getFrom(), event.getTo(), event.getBytesCopied(), event.isCopyComplete());
		}
	}
	
	public void copyComplete(boolean resolutionRequired, boolean copyCancelled) {
		SwingFileCopierEvent event = new SwingFileCopierEvent();
		event.setCopyCompleteType(resolutionRequired, copyCancelled);
		publish(event);
	}

	public void fileCopied(File from, File to, CopyResult copyResult) {
		if (isDone()) {
			final SwingFileCopierEvent event = new SwingFileCopierEvent();
			event.setCopiedType(from, to, copyResult);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireFileCopied(event);
				}
			});
		} else {
			SwingFileCopierEvent event = new SwingFileCopierEvent();
			event.setCopiedType(from, to, copyResult);
			publish(event);	
		}
	}

	public void fileCopying(File from, File to) {
		if (isDone()) {
			final SwingFileCopierEvent event = new SwingFileCopierEvent();
			event.setCopyingType(from, to);		
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireFileCopying(event);
				}
			});			
		} else {
			SwingFileCopierEvent event = new SwingFileCopierEvent();
			event.setCopyingType(from, to);
			publish(event);		
		}
	}

	public void fileCopying(File from, File to, long bytesCopied, boolean copyComplete) {
		if (isDone()) {
			final SwingFileCopierEvent event = new SwingFileCopierEvent();
			event.setCopyProgressType(from, to, bytesCopied, copyComplete);	
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireFileCopyProgress(event);
				}
			});
		} else {
			SwingFileCopierEvent event = new SwingFileCopierEvent();
			event.setCopyProgressType(from, to, bytesCopied, copyComplete);
			publish(event);	
		}
	}	
}

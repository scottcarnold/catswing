package org.xandercat.swing.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.util.PlatformTool;

/**
 * FilesSizeCalculator processes a list of files, fully recursing any directories, to compute
 * the number of files, directories, and total size of all files in bytes.  Results of the process
 * are fed back to a FilesSizeHandler.
 * 
 * @author Scott C Arnold
 */
public class FilesSizeCalculator extends SwingWorker<FilesSize, File> {

	private static final Logger log = LogManager.getLogger(FilesSizeCalculator.class);
	
	private FilesSize initialSize;
	private FilesSize currentSize;
	private List<File> addedFiles;
	private List<File> removedFiles;
	private FilesSizeHandler handler;
	private volatile long queuedFileCount;
	private volatile String status = "New";
	
	public FilesSizeCalculator(List<File> files, FilesSizeHandler handler) {
		this(new FilesSize(), files, new ArrayList<File>(), handler);
	}
	
	public FilesSizeCalculator(FilesSize currentSize, List<File> addedFiles, List<File> removedFiles, FilesSizeHandler handler) {
		this.initialSize = currentSize.clone();
		this.currentSize = currentSize.clone();
		this.addedFiles = addedFiles;
		this.removedFiles = removedFiles;
		this.handler = handler;
//		FilesSizeMonitor monitor = ResourceManager.getInstance().getResource(FilesSizeMonitor.class);
//		if (monitor != null) {
//			monitor.register(this, "Calculator for " + handler.getClass().getSimpleName());
//		}
		this.status = "Initialized";
	}
	
	public FilesSizeCalculator(FilesSizeCalculator fsc, List<File> addedFiles, List<File> removedFiles) {
		this(fsc.initialSize, fsc.addedFiles, fsc.removedFiles, fsc.handler);
		log.debug("Restarting calculator; previously had " + fsc.addedFiles.size() + " added and " + fsc.removedFiles.size() + " removed files.");
		log.debug("Next set has " + addedFiles.size() + " added and " + removedFiles.size() + " removed.");
		List<File> cancelFiles = new ArrayList<File>();
		List<File> newRemovedFiles = new ArrayList<File>();
		newRemovedFiles.addAll(removedFiles);
		cancelFiles.addAll(this.addedFiles);
		cancelFiles.retainAll(removedFiles);
		this.addedFiles.removeAll(removedFiles);
		this.addedFiles.addAll(addedFiles);
		this.removedFiles.removeAll(addedFiles);
		newRemovedFiles.removeAll(cancelFiles);
		this.removedFiles.addAll(newRemovedFiles);
		log.debug("Merged set has " + this.addedFiles.size() + " added and " + this.removedFiles.size() + " removed.");
		this.status = "Reinitialized";
	}
	
	@Override
	protected FilesSize doInBackground() throws Exception {
		this.status = "Started";
		if (addedFiles != null) {
			for (File file: addedFiles) {
				if (isCancelled()) {
					break;
				}
				if (file.isDirectory()) {
					currentSize.add(getDirectorySize(file));
					currentSize.addDirectories(1);
				} else {
					currentSize.addFiles(1);
				}
				currentSize.addBytes(file.length());
			}
		}
		if (removedFiles != null) {
			for (File file : removedFiles) {
				if (isCancelled()) {
					break;
				}
				if (file.isDirectory()) {
					currentSize.remove(getDirectorySize(file));
					currentSize.removeDirectories(1);
				}  else {
					currentSize.removeFiles(1);
				}
				currentSize.removeBytes(file.length());
			}
		}
		return currentSize;
	}

	public long getQueuedFileCount() {
		return queuedFileCount;
	}
	
	public String getStatus() {
		return status;
	}
	
	private FilesSize getDirectorySize(File file) {
		FilesSize ds = DirectorySizeCache.getInstance().getDirectorySize(file);
		if (ds != null) {
			return ds;
		}
		publish(file);
		ds = new FilesSize();
		File[] children = file.listFiles(PlatformTool.FILE_FILTER);
		if (children != null) {
			this.queuedFileCount += children.length;
			for (File child : children) {
				if (isCancelled()) {
					break;
				}
				if (child.isDirectory()) {
					ds.addDirectories(1);
					ds.add(getDirectorySize(child));
				} else {
					ds.addFiles(1);
				}
				ds.addBytes(child.length());
			}
			this.queuedFileCount -= children.length;	
		}
		if (!isCancelled()) {
			DirectorySizeCache.getInstance().setDirectorySize(file, ds);
		}
		return ds;
	}

	@Override
	protected void process(List<File> directories) {
		handler.handleDirectoryProcessing(directories);
	}

	@Override
	protected void done() {
		this.status = "Done";
		try {
//			FilesSizeMonitor monitor = ResourceManager.getInstance().getResource(FilesSizeMonitor.class);
//			if (monitor != null) {
//				monitor.unregister(this);
//			}
			if (isCancelled()) {
				throw new InterruptedException("Process was cancelled.");
			}
			handler.handleFilesSize(this, get());
		} catch (InterruptedException ie) {
			log.debug("Handling files size interrupted");
			handler.handleFilesSizeInterrupted();
		} catch (Exception e) {
			log.error("Handling files size interrupted", e);
			handler.handleFilesSizeInterrupted();
		}
	}
	
	
}

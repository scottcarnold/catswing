package org.xandercat.swing.table;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;

import org.xandercat.swing.file.DirectorySizeCache;
import org.xandercat.swing.file.FilesSize;

/**
 * FileTableModelLoader allows table to show immediate results and allows UI to remain responsive
 * when loading a HUGE number of files into the table.
 * 
 * FileTableModelLoaderListeners can be added to listen for file load completion or cancellation.
 * After worker completion, all listeners are automatically removed.
 * 
 * @author Scott C Arnold
 */
public class FileTableModelLoader extends SwingWorker<Void,FileData> {

	private FileTableModel model;
	private File[] files;
	private boolean useDirectorySizeCache;
	private DirectorySizeCache directorySizeCache;
	private List<FileTableModelLoaderListener> listeners = new ArrayList<FileTableModelLoaderListener>();
	
	public FileTableModelLoader(FileTableModel model, File[] files, boolean useDirectorySizeCache) {
		this.model = model;
		this.files = files;
		this.useDirectorySizeCache = useDirectorySizeCache;
		if (this.useDirectorySizeCache) {
			this.directorySizeCache = DirectorySizeCache.getInstance();
		}
	}

	public void addFileTableModelLoaderListener(FileTableModelLoaderListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeFileTableModelLoaderListener(FileTableModelLoaderListener listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		List<FileData> loadingDirectories = new ArrayList<FileData>();
		for (File file : files) {
			FileData data = new FileData(file);
			if (file.isDirectory() && this.useDirectorySizeCache) {
				FilesSize directorySize = this.directorySizeCache.getDirectorySize(file);
				if (directorySize != null) {
					data.setLength(directorySize.getBytes());
				} else {
					data.setLength(null);
					this.directorySizeCache.loadDirectorySizeAnsync(file);
					loadingDirectories.add(data);
				}
			}
			publish(data);
		}
		while (loadingDirectories.size() > 0) {
			// wait around for directory sizes to load
			// TODO: Polling like this is sloppy, clean it up
			Thread.sleep(500);
			for (Iterator<FileData> iter = loadingDirectories.iterator(); iter.hasNext();) {
				FileData directoryData = iter.next();
				FilesSize directorySize = this.directorySizeCache.getDirectorySize(directoryData.getFile());
				if (directorySize != null) {
					directoryData.setLength(directorySize.getBytes());
					iter.remove();
					publish(directoryData);
				}
			}
		}
		return null;
	}

	@Override
	protected void process(List<FileData> fileData) {
		for (FileData datum : fileData) {
			model.replaceElement(datum);	// replace will default to add if not already in model
		}
	}

	@Override
	protected void done() {
		if (isCancelled()) {
			for (FileTableModelLoaderListener listener : listeners) {
				listener.fileTableLoadingCancelled();
			}
		} else {
			for (FileTableModelLoaderListener listener : listeners) {
				listener.fileTableLoadingComplete();
			}			
		}
		listeners.clear();
	}
}

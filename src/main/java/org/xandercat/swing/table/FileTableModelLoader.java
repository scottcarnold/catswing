package org.xandercat.swing.table;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingWorker;

import org.xandercat.swing.file.DirectorySizeCache;
import org.xandercat.swing.file.DirectorySizeHandler;
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
public class FileTableModelLoader extends SwingWorker<Void,FileData> implements DirectorySizeHandler {

	private FileTableModel model;
	private File[] files;
	private boolean useDirectorySizeCache;
	private DirectorySizeCache directorySizeCache;
	private List<FileTableModelLoaderListener> listeners = new ArrayList<FileTableModelLoaderListener>();
	private CountDownLatch countDownLatch;
	private Map<File, FileData> loadingDirectories;
	
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
		this.loadingDirectories = new HashMap<File, FileData>();
		for (File file : files) {
			FileData data = new FileData(file);
			if (file.isDirectory() && this.useDirectorySizeCache) {
				FilesSize directorySize = this.directorySizeCache.getDirectorySize(file);
				if (directorySize != null) {
					data.setLength(directorySize.getBytes());
				} else {
					data.setLength(null);
					loadingDirectories.put(file, data);
				}
			}
			publish(data);
		}
		if (loadingDirectories.size() > 0) {
			this.countDownLatch = new CountDownLatch(loadingDirectories.size());
			for (File directory : loadingDirectories.keySet()) {
				this.directorySizeCache.loadDirectorySizeAsync(directory, this);
			}
			this.countDownLatch.await();
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

	@Override
	public void directorySizeLoaded(File directory, FilesSize size) {
		FileData directoryData = this.loadingDirectories.get(directory);
		directoryData.setLength(size.getBytes());
		publish(directoryData);
		this.countDownLatch.countDown();
	}
}

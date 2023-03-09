package org.xandercat.swing.file;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.xml.ws.handler.Handler;


/**
 * DirectorySizeCache provides a simple cache of computed directory sizes.  A directory size is the
 * size in bytes of all files and directories in all subdirectories beneath the given directory.
 * Such calculation can be time consuming; this class caches calculation results so that the size
 * of any particular directory need only be calculated at most once during the execution of
 * a program that tracks directory sizes.
 *  
 * The FilesSize class is used to store the directory size information.  For any FilesSize added
 * to the cache, the directory count represents the number of subdirectories.
 * 
 * TODO: Manage the cache size
 * 
 * @author Scott C Arnold
 */
public class DirectorySizeCache {

	private static DirectorySizeCache cache;
	
	private Map<File,FilesSize> sizes;
	
	private DirectorySizeCache() {
		sizes = Collections.synchronizedMap(new HashMap<File,FilesSize>());
	}
	
	public static synchronized DirectorySizeCache getInstance() {
		if (cache == null) {
			cache = new DirectorySizeCache();
		}
		return cache;
	}
	
	public int getSize() {
		return sizes.size();
	}
	
	public FilesSize getDirectorySize(File directory) {
		return sizes.get(directory);
	}
	
	public Long getBytes(File directory) {
		FilesSize size = sizes.get(directory);
		return (size == null)? null : Long.valueOf(size.getBytes());
	}
	
	public Integer getSubDirectories(File directory) {
		FilesSize size = sizes.get(directory);
		return (size == null)? null : Integer.valueOf(size.getDirectories());	
	}
	
	public Integer getFiles(File directory) {
		FilesSize size = sizes.get(directory);
		return (size == null)? null : Integer.valueOf(size.getFiles());		
	}
	
	public void setDirectorySize(File directory, FilesSize directorySize) {
		sizes.put(directory, directorySize);
	}
	
	public void setDirectorySize(File directory, long bytes, int subDirectories, int files) {
		sizes.put(directory, new FilesSize(bytes, subDirectories, files));
	}
	
	public void clear() {
		sizes.clear();
	}
	
	/**
	 * Load the given directory into the DirectorySizeCache and return it's size.
	 * 
	 * @param directory		directory to load into the cache
	 * 
	 * @return				directory size
	 */
	public FilesSize loadDirectorySize(File directory) {
		FilesSize size = getDirectorySize(directory);
		if (size != null) {
			return size;
		}
		size = new FilesSize();
		File[] subFiles = directory.listFiles();
		if (subFiles != null) {
			for (File subFile : subFiles) {
				if (subFile.isDirectory()) {
					size.addDirectories(1);
					size.add(loadDirectorySize(subFile));
				} else {
					size.addFiles(1);
					size.addBytes(subFile.length());
				}
			}
		}
		setDirectorySize(directory, size);
		return size;
	}
	
	public void loadDirectorySizeAsync(final File directory) {
		new Thread(() -> loadDirectorySize(directory)).start();
	}
	
	public void loadDirectorySizeAsync(final File directory, DirectorySizeHandler handler) {
		new Thread(() -> handler.directorySizeLoaded(directory, loadDirectorySize(directory))).start();
	}
}

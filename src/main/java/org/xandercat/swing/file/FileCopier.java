package org.xandercat.swing.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FileCopier copies a list of files as a batch operation and keeps track of any files that cannot be copied for
 * use in later resolution.
 * 
 * Any directories in the file copy list will be created but no files within that directory will be copied unless they
 * exist as separate files in the file copy list.  Also, directories cannot be copied unless a source directory or
 * path generator is specified.
 * 
 * @author Scott C Arnold
 */
public class FileCopier {

	public static final long DEFAULT_CHANNEL_BUFFER_SIZE = 1024 * 1024 * 4;		// 4MB
	
	private static final Logger log = LogManager.getLogger(FileCopier.class);
			
	public static enum CopyResult {
		COPIED, SKIPPED, ALREADY_EXISTS, ERROR;
	}
	
	public static class FileCopyError {
		private File file;
		private Throwable throwable;
		private FileCopyError(File file, Throwable throwable) {
			this.file = file;
			this.throwable = throwable;
		}
		public File getFile() {
			return file;
		}
		public Throwable getThrowable() {
			return throwable;
		}
	}
	
	private List<File> files;
	private List<File> overwriteFiles;		// files that exist at destination already (prompt for overwrite needed)
	private List<FileCopyError> errorFiles;	// files that could not be copied due to some error
	private List<File> skippedFiles;		// skipped files (file with same name, length, and mod time exists at dest)
	private List<File> copiedFiles;
	private String sourcePath;
	private String destinationPath;
	private FileCopierPathGenerator pathGenerator;
	private List<FileCopyListener> listeners;
	private List<FileCopyProgressListener> progressListeners;
	private boolean testMode = false;
	private long testModeSpeedFactor = 10000;
	private volatile boolean cancelled = false;
	private long channelBufferSize = DEFAULT_CHANNEL_BUFFER_SIZE;
	
	/**
	 * Close the given input stream.
	 * 
	 * @param in		InputStream to close
	 */
	public static void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException ioe) { } 
		}
	}
	
	/**
	 * Close the given output stream.
	 * 
	 * @param out		OutputStream to close
	 */
	public static void close(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException ioe) { }
		}
	}
	
	/**
	 * Closes the given file channel.
	 * 
	 * @param channel	file channel to close
	 */
	public static void close(FileChannel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException ioe) { }
		}
	}
	
	/**
	 * Copy the given file to the given destination using the default buffer size.
	 * 
	 * @param inFile		file to copy
	 * @param outFile		where to copy file to
	 * @param overwrite		whether or not to overwrite if outFile already exists
	 * @param makeDirectory	whether or not to make the parent directories if they do not already exist
	 * @throws IOException
	 */
	public static void copyFile(File inFile, File outFile, boolean overwrite, boolean makeDirectory) throws IOException {
		copyFile(inFile, outFile, overwrite, makeDirectory, DEFAULT_CHANNEL_BUFFER_SIZE, null);
	}
	
	/**
	 * Copy the given file to the given destination using the given buffer size.  
	 * 
	 * @param inFile		file to copy
	 * @param outFile		where to copy file to
	 * @param overwrite		whether or not to overwrite if outFile already exists
	 * @param makeDirectory	whether or not to make the parent directories if they do not already exist
	 * @param progressListeners	any listeners wishing to be notifed of copy status
	 * @throws IOException
	 */
	public static void copyFile(File inFile, File outFile, boolean overwrite, boolean makeDirectory, long bufferSize, List<FileCopyProgressListener> progressListeners) throws IOException {
		if (!inFile.exists()) {
			throw new IOException("Source file does not exist.");
		}
		if (!overwrite && outFile.exists()) {
			throw new IOException("Destination file already exists.");
		}
		if (makeDirectory) {
			File outDir = outFile;
			if (!inFile.isDirectory()) {
				int endIndex = outFile.getAbsolutePath().length() - outFile.getName().length();
				outDir = new File(outFile.getAbsolutePath().substring(0, endIndex));
			}
			if (!outDir.exists()) {
				if (!outDir.mkdirs()) {
					throw new IOException("Unable to make directories for destination file.");
				} 
			}
		}
		if (outFile.isDirectory()) {
			return;
		}
		FileChannel in = null;
		FileChannel out = null;
		try(FileInputStream fin = new FileInputStream(inFile); FileOutputStream fout = new FileOutputStream(outFile)) {
			in = fin.getChannel();
			out = fout.getChannel();
			if (progressListeners == null) {
				long size = in.size();
				long pos = 0;
				while (pos < size) {
					pos += in.transferTo(pos, bufferSize, out);
				}
			} else {
				long size = in.size();
				long pos = 0;
				while (pos < size) {
					pos += in.transferTo(pos, bufferSize, out);
					for (FileCopyProgressListener listener : progressListeners) {
						listener.fileCopying(inFile, outFile, pos, pos >= size);
					}
				}
			}
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			close(in);
			close(out);
		}
	}

	private boolean simulateCopyFileInternal(File inFile, File outFile) {
		if (inFile.isDirectory()) {
			return true;
		}
		final long partialSize = 1000;
		// simulate some copy time
		try {
			Thread.sleep(Math.min(3000, inFile.length()/(2*testModeSpeedFactor)));
		} catch (InterruptedException ie) { }
		// fire copyProgress for partial copy (or full copy if file is small)
		fireCopyProgress(inFile, outFile, partialSize, partialSize >= inFile.length());
		if (partialSize >= inFile.length()) {
			return true;
		}
		if (cancelled) {
			log.info("Simulation cancel occurred in middle of in progress file copy");
			return false;
		}
		// simulate remaining copy time
		try {
			Thread.sleep(Math.min(3000, inFile.length()/(2*testModeSpeedFactor)));
		} catch (InterruptedException ie) { }
		// fire final copyProgress for full copy
		fireCopyProgress(inFile, outFile, inFile.length()-partialSize, true);
		if (cancelled) {
			log.info("Simulation cancel occurred at completion of in progress file copy");
			return false;
		}
		return true;
	}
	
	/**
	 * Copy the given file to the given destination using the given buffer size.  
	 * 
	 * @param inFile		file to copy
	 * @param outFile		where to copy file to
	 * @param overwrite		whether or not to overwrite if outFile already exists
	 * @param makeDirectory	whether or not to make the parent directories if they do not already exist
	 * @param progressListeners	any listeners wishing to be notifed of copy status
	 * @return whether or not copy was completed (copy will not be completed if cancelled)
	 * @throws IOException
	 */
	private boolean copyFileInternal(File inFile, File outFile, boolean overwrite, boolean makeDirectory) throws IOException {
		if (!inFile.exists()) {
			throw new IOException("Source file does not exist.");
		}
		if (!overwrite && outFile.exists()) {
			throw new IOException("Destination file already exists.");
		}
		if (makeDirectory) {
			File outDir = outFile;
			if (!inFile.isDirectory()) {
				int endIndex = outFile.getAbsolutePath().length() - outFile.getName().length();
				outDir = new File(outFile.getAbsolutePath().substring(0, endIndex));
			}
			if (!outDir.exists()) {
				if (!outDir.mkdirs()) {
					throw new IOException("Unable to make directories for destination file.");
				} 
			}
		}
		if (outFile.isDirectory()) {
			return true;
		}
		FileChannel in = null;
		FileChannel out = null;
		try(FileInputStream fin = new FileInputStream(inFile); FileOutputStream fout = new FileOutputStream(outFile)) {
			in = fin.getChannel();
			out = fout.getChannel();
			long size = in.size();
			long pos = 0;
			while (pos < size && !cancelled) {
				pos += in.transferTo(pos, channelBufferSize, out);
				fireCopyProgress(inFile, outFile, pos, pos >= size);
			}
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			close(in);
			close(out);
			if (cancelled) { // don't care if it completed copy or not; cancel happened before finish so delete it regardless
				outFile.delete();
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Construct a new file copier to copy the given files from the given source directory
	 * to the given destination directory.  If source is null paths will not be preserved
	 * and directories cannot be specified.  Destination cannot be null.
	 * 
	 * @param files				files to copy
	 * @param destination		directory to copy files to
	 * @param source			directory to copy files from
	 */
	public FileCopier(List<File> files, File destination, File source) {
		initialize(files, destination, source);
	}
	
	/**
	 * Construct a new file copier to copy the given files using the given path generator.
	 * 
	 * @param files				files to copy
	 * @param pathGenerator		path generator
	 */
	public FileCopier(List<File> files, FileCopierPathGenerator pathGenerator) {
		this.pathGenerator = pathGenerator;
		initialize(files, null, null);
	}
	
	private void initialize(List<File> files, File destination, File source) {
		if (pathGenerator == null) {
			if (destination == null || (destination.exists() && !destination.isDirectory())) {
				String destPath = (destination == null)? "null" : destination.getAbsolutePath();
				throw new IllegalArgumentException("Destination is not a valid directory: " + destPath);
			}
			if (source != null && (!source.exists() || !source.isDirectory())) {
				throw new IllegalArgumentException("Source is not a valid directory.");
			}
			if (source != null) {
				sourcePath = source.getAbsolutePath();
			}
			for (File file : files) {
				if (source != null && !file.getAbsolutePath().startsWith(sourcePath)) {
					throw new IllegalArgumentException("If a source directory is specified, all files must be contained within the source directory.");
				} 
				if (source == null && file.isDirectory()) {
					throw new IllegalArgumentException("Directories cannot be copied unless a source directory is specified.");
				}
			}
			this.destinationPath = destination.getAbsolutePath();
		}
		this.files = files;
		this.overwriteFiles = new ArrayList<File>();
		this.skippedFiles = new ArrayList<File>();
		this.errorFiles = new ArrayList<FileCopyError>();
		this.copiedFiles = new ArrayList<File>();
	}
	
	/**
	 * Enable test mode, which causes file copier to simulate copying files without actually 
	 * copying them.  Primarily used for testing.
	 */
	public void enableTestMode() {
		this.testMode = true;
	}
	
	public void enableTestMode(long speedFactor) {
		if (speedFactor < 1) {
			throw new IllegalArgumentException("Speed factor must be > 0");
		}
		this.testMode = true;
		this.testModeSpeedFactor = speedFactor;
	}
	
	/**
	 * Add a FileCopyListener to be notified of files being copied.
	 * 
	 * @param listener				listener to add
	 */
	public void addFileCopyListener(FileCopyListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<FileCopyListener>();
		}
		listeners.add(listener);
	}
	
	public void removeFileCopyListener(FileCopyListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Add a FileCopyProgressListener to be notified periodically of file copy progress.
	 * This type of listener goes beyond the FileCopyListener by potentially firing multiple
	 * events for each file copied, providing continual progress updates regardless of file sizes.
	 * 
	 * @param listener				listener to add
	 */
	public void addFileCopyProgressListener(FileCopyProgressListener listener) {
		if (progressListeners == null) {
			progressListeners = new ArrayList<FileCopyProgressListener>();
		}
		progressListeners.add(listener);
	}
	
	private File getDestinationFile(File file) {
		String filePath = null;
		if (pathGenerator != null) {
			filePath = pathGenerator.generateDestinationPath(file);
		} else if (sourcePath == null) {
			//Note:  a filename collision can result in this case, but this should be handled by the normal skip/overwrite behavior			
			filePath = destinationPath + File.separator + file.getName();
		} else {
			filePath = destinationPath + file.getAbsolutePath().substring(sourcePath.length());
		}
		return (filePath == null)? null : new File(filePath);
	}
	
	/**
	 * Get the exception that occurred when attempting to copy the given file from the copy error queue.
	 * 
	 * @param file			a file in the copy error queue
	 * 
	 * @return				exception that occurred when trying to copy the file
	 */
	public Throwable getFileCopyException(File file) {
		for (FileCopyError fce : errorFiles) {
			if (fce.getFile().equals(file)) {
				return fce.getThrowable();
			}
		}
		return null;
	}
	
	private void fireFileCopying(File from, File to) {
		if (listeners != null) {
			for (FileCopyListener listener : listeners) {
				listener.fileCopying(from, to);
			}
		}
	}
	
	private void fireFileCopied(File from, File to, CopyResult result) {
		if (listeners != null) {
			for (FileCopyListener listener : listeners) {
				listener.fileCopied(from, to, result);
			}
		}
	}
	
	private void fireCopyComplete(boolean cancelled) {
		if (listeners != null) {
			int filesToResolve = overwriteFiles.size() + errorFiles.size();
			for (FileCopyListener listener : listeners) {
				listener.copyComplete(filesToResolve > 0, cancelled);
			}
		}
	}
	
	private void fireCopyProgress(File from, File to, long bytesCopied, boolean copyComplete) {
		if (progressListeners != null) {
			for (FileCopyProgressListener listener : progressListeners) {
				listener.fileCopying(from, to, bytesCopied, copyComplete);
			}
		}
	}
	
	/**
	 * Cancel the current copy operation; since the copy method is blocking, this call must be 
	 * made by a thread that did not initiate the copy.
	 */
	public void cancel() {
		this.cancelled = true;
	}
	
	/**
	 * Copy the files stored within the file copier.
	 */
	public void copy() {
		boolean cancelled = false;
		for (File file : files) {
			if (this.cancelled) {
				cancelled = true;
				break;
			}
			copyFile(file, false);
		}
		fireCopyComplete(cancelled);
	}
	
	/**
	 * Copy a new list of files from the given source directory to the given destination directory.
	 * Use this method when you wish to perform multiple copies using the same file copier.
	 * 
	 * @param files				files to copy
	 * @param destination		destination directory
	 * @param source			source directory
	 */
	public void copy(List<File> files, File destination, File source) {
		initialize(files, destination, source);
		copy();
	}
	
	/**
	 * Inform the file copier whether or not to overwrite a file that is in the overwrite queue.
	 * 
	 * @param file			a file (with source path) in the overwrite queue
	 * @param overwrite		whether or not to overwrite the file of same name at the destination path
	 *  
	 * @return				whether or not the request completed successfully.
	 */
	public boolean resolveOverwrite(File file, boolean overwrite) {
		if (!overwriteFiles.contains(file)) {
			return false;
		}
		overwriteFiles.remove(file);
		if (overwrite) {
			return copyFile(file, true);
		}
		return true;
	}
	
	/**
	 * Inform the file copier whether or not to retry copying a file that is in the copy error queue.
	 * 
	 * @param file			a file (with source path) in the copy error queue
	 * @param retry			whether or not to retry copying the file
	 * 
	 * @return				whether or not the request completed successfully.
	 */
	public boolean resolveError(File file, boolean retry) {
		FileCopyError fileCopyError = null;
		for (FileCopyError fce : errorFiles) {
			if (fce.getFile().equals(file)) {
				fileCopyError = fce;
				break;
			}
		}
		if (fileCopyError == null) {
			return false;
		}
		errorFiles.remove(fileCopyError);
		if (retry) {
			return copyFile(file, false);			
		}
		return true;
	}

	private boolean copyFile(File file, boolean overwrite) {
		File destFile = getDestinationFile(file);
		if (testMode) {
			log.info("Simulating copy of file " + file.getAbsolutePath() + " to " + destFile.getAbsolutePath());
		}
		fireFileCopying(file, destFile);
		boolean copied = false;
		//note: overwrite flag is only true after the first pass; for that reason, it is not necessary
		//      to check for a skipped file when overwrite is true as it would have been caught in the first pass.
		if (destFile.exists() && !overwrite) {
			if (destFile.isDirectory() ||
					(destFile.length() == file.length() && destFile.lastModified() == file.lastModified())) {
				skippedFiles.add(file);
				fireFileCopied(file, destFile, CopyResult.SKIPPED);
			} else {
				overwriteFiles.add(file);
				fireFileCopied(file, destFile, CopyResult.ALREADY_EXISTS);
			}
		} else {
			try {
				if (testMode) {
					copied = simulateCopyFileInternal(file, destFile);
				} else {
					copied = copyFileInternal(file, destFile, overwrite, true);
				}
				if (copied) {
					copiedFiles.add(destFile);
					fireFileCopied(file, destFile, CopyResult.COPIED);
					try {
						if (!testMode) {
							destFile.setLastModified(file.lastModified());
						}
					} catch (Exception e) {
						log.error("Unable to set last modified time on copied file " + destFile.getAbsolutePath());
					}
				}
			} catch (Exception e) {
				log.info("File copy error", e);
				errorFiles.add(new FileCopyError(file, e));
				fireFileCopied(file, destFile, CopyResult.ERROR);
			}
		}		
		return copied;
	}

	/**
	 * Set the size of the buffer used when copying files.
	 * 
	 * @param bufferSize		buffer size in bytes
	 */
	public void setChannelBufferSize(long bufferSize) {
		this.channelBufferSize = bufferSize;
	}
	
	/**
	 * Get the size of the buffer used when copying files.
	 * 
	 * @return					buffer size in bytes
	 */
	public long getChannelBufferSize() {
		return channelBufferSize;
	}
	
	/**
	 * Get the list of files in the overwrite queue.  These files were not copied because  
	 * non-identical files with the same names already exist at the destination location.
	 * 
	 * @return		list of files in the overwrite queue
	 */
	public List<File> getOverwriteFiles() {
		return overwriteFiles;
	}

	/**
	 * Get the list of copy errors in the copy error queue.  These copy errors are for files
	 * that were not successfully copied.  Each copy error contains the file that was to be 
	 * copied and the exception that occurred when the copy was attempted.  
	 * 
	 * @return		list of copy errors in the copy error queue
	 */
	public List<FileCopyError> getErrorFiles() {
		return errorFiles;
	}

	/**
	 * Get the list of files that were skipped.  Skipped files already exist at the destination
	 * location and are identical (to the best the file copier can tell) to the files to be copied.
	 * 
	 * @return		list of skipped files
	 */
	public List<File> getSkippedFiles() {
		return skippedFiles;
	}
	
	/**
	 * Get the list of copied files.  These are the files at their destination (they are not the
	 * same files that were originally passed into the copy operation).
	 * 
	 * @return		list of copied files
	 */
	public List<File> getCopiedFiles() {
		return copiedFiles;
	}
}

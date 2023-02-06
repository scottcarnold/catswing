package org.xandercat.swing.util;

import java.io.File;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.file.BinaryPrefix;

/**
 * FileUtil provides various utility methods specific to working with files.
 *
 * 	Some oddball stuff:
 * 	// for Mac "/", name = "" and parent is either null or blank, abs path is "/"
 *	// for PC "C:\", name = "" and parent is either null or blank, abs path is "C:\"
 *
 * @author Scott C Arnold
 */
public class FileUtil {

	private static final Logger log = LogManager.getLogger(FileUtil.class);
	private static final Map<File,Long> unresponsiveFileMap = Collections.synchronizedMap(new HashMap<File,Long>());
	private static final long FILE_ACCESS_TIMEOUT = 1000L;		// in milliseconds
	private static final long UNRESPONSIVE_FILE_RETRY_INTERVAL = 120000L;	// in milliseconds
	
	private static class DirectoryChecker implements Callable<Boolean> {

		private File file;
		
		public DirectoryChecker(File file) {
			this.file = file;
		}

		public Boolean call() throws Exception {
			boolean dir = file.isDirectory();
			return Boolean.valueOf(dir);
		}
	}
	
	private static class FileExistsChecker implements Callable<Boolean> {
	
		private File file;
		
		public FileExistsChecker(File file) {
			this.file = file;
		}
		
		public Boolean call() throws Exception {
			boolean exists = file.exists();
			return Boolean.valueOf(exists);
		}
	}
	
	/**
	 * Get the name of the given file without the full absolute path.  Directory names will
	 * be proceeded by a File.separator character.
	 * 
	 * @param file		file to get name for
	 * 
	 * @return			short name for file
	 */
	public static String getShortName(File file) {
		return getShortName(file, 0);
	}

	/**
	 * Get the name of the given file without the full absolute path.  Directory names will
	 * be proceeded by a File.separator character.  Length of returned string will not exceed
	 * the given maxLength (names that would be longer than maxLength are truncated from the 
	 * front and proceeded with "...").
	 * 
	 * @param file		file to get name for
	 * @param maxLength	maximum length of returned string
	 * 
	 * @return			short name for file
	 */
	public static String getShortName(File file, int maxLength) {
		if (file == null) {
			return "";
		}
		String name = null;
		if (Boolean.TRUE.equals(isDirectory(file))) {
			if ((file.getAbsolutePath().indexOf(File.separator) >= 0) && !isDirectoryRootPath(file)) {
				String path = file.getAbsolutePath();
				int i = path.lastIndexOf(File.separator);
				name = (i >= 0)? path.substring(path.lastIndexOf(File.separator)) : path;
			} 
		} else {
			if (file.getName() != null && file.getName().length() > 0) {
				name = file.getName();
			}
		}
		if (name == null) {
			name = file.getAbsolutePath();
		}
		if (maxLength > 0 && name.length() > maxLength) {
			name = "..." + name.substring(Math.min(name.length(), name.length() - maxLength + 3)); 
		}
		return name;
	}
	
	/**
	 * Return whether or not the given file is a root path.  May return null if the file cannot be
	 * properly accessed within a given time.
	 * 
	 * @param file			the file to test
	 * 
	 * @return				whether or not given file is a root file
	 */
	public static Boolean isRootPath(File file) {
		if (!file.getAbsolutePath().endsWith(File.separator)) {
			return Boolean.FALSE;
		}
		return isDirectory(file);
	}
	
	/**
	 * Return whether or not the given directory is a root path.  No checking is performed so ensure
	 * that the given file is actually a directory.
	 * 
	 * @param file			the directory to test
	 * 
	 * @return				whether or not the given directory is a root file
	 */
	public static boolean isDirectoryRootPath(File file) {
		return file.getAbsolutePath().endsWith(File.separator);
	}
	
	/**
	 * Return whether or not the given file is a directory, or null if the file does not respond within
	 * a given timeout (can happen with misbehaving files).  
	 *
	 * Files that do not respond are remembered for a certain retry period of time; repeated attempts
	 * to check the same file before the retry period has elapsed will result in null being returned
	 * without the file actually being checked.  
	 * 
	 * @param file
	 * @return
	 */
	public static Boolean isDirectory(File file) {
		if (unresponsiveFileMap.size() > 0) {
			synchronized(unresponsiveFileMap) {
				Long time = unresponsiveFileMap.get(file);
				if (time != null) {
					long timeSinceLastAttempt = System.currentTimeMillis() - time.longValue();
					if (timeSinceLastAttempt > UNRESPONSIVE_FILE_RETRY_INTERVAL) {
						// time to try again
						unresponsiveFileMap.remove(file);
					} else {
						// hasn't been long, assume file is still naughty
						return null;
					}
				}
			}
		}
		FutureTask<?> checkDirectoryTask = null;
		DirectoryChecker directoryChecker = new DirectoryChecker(file);
		Boolean isDirectory = null;
		try {
			checkDirectoryTask = new FutureTask<Boolean>(directoryChecker);
			new Thread(checkDirectoryTask).start();
			isDirectory = (Boolean) checkDirectoryTask.get(FILE_ACCESS_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (TimeoutException te) {
			log.warn("Timeout attempting to check if file " + file.getAbsolutePath() + " is a directory");
		} catch (ExecutionException ee) {
			log.warn("Unknown error attempting to check if file " + file.getAbsolutePath() + " is a directory", ee);
		} catch (InterruptedException ie) {
			log.warn("Interruption while attempting to check if file " + file.getAbsolutePath() + " is a directory", ie);
		}
		if (isDirectory == null) {
			synchronized(unresponsiveFileMap) {
				unresponsiveFileMap.put(file, Long.valueOf(System.currentTimeMillis()));
			}
		}
		return isDirectory;
	}
	
	/**
	 * Return whether or not the given file exists, or null if the file does not respond within
	 * a given timeout (can happen with misbehaving files).
	 * 
	 * Files that do not respond are remembered for a certain retry period of time; repeated attempts
	 * to check the same file before the retry period has elapsed will result in null being returned
	 * without the file actually being checked.  
	 * 
	 * @param file		file to test
	 * 
	 * @return			whether or not file exists (or null if not responding)
	 */
	public static Boolean exists(File file) {
		if (unresponsiveFileMap.size() > 0) {
			synchronized(unresponsiveFileMap) {
				Long time = unresponsiveFileMap.get(file);
				if (time != null) {
					long timeSinceLastAttempt = System.currentTimeMillis() - time.longValue();
					if (timeSinceLastAttempt > UNRESPONSIVE_FILE_RETRY_INTERVAL) {
						// time to try again
						unresponsiveFileMap.remove(file);
					} else {
						// hasn't been long, assume file is still naughty
						return null;
					}
				}
			}
		}
		FutureTask<?> fileExistsTask = null;
		FileExistsChecker checker = new FileExistsChecker(file);
		Boolean exists = null;
		try {
			fileExistsTask = new FutureTask<Boolean>(checker);
			new Thread(fileExistsTask).start();
			exists = (Boolean) fileExistsTask.get(FILE_ACCESS_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (TimeoutException te) {
			log.warn("Timeout attempting to check if file " + file.getAbsolutePath() + " exists");
		} catch (ExecutionException ee) {
			log.warn("Unknown error attempting to check if file " + file.getAbsolutePath() + " exists", ee);
		} catch (InterruptedException ie) {
			log.warn("Interruption while attempting to check if file " + file.getAbsolutePath() + " exists", ie);
		}
		if (exists == null) {
			synchronized(unresponsiveFileMap) {
				unresponsiveFileMap.put(file, Long.valueOf(System.currentTimeMillis()));
			}
		}
		return exists;
	}
	
	/**
	 * Get the parent directory of the given file.  This is similar to calling file.getParentFile(),
	 * but also works when dealing with relative file names.
	 * 
	 * @param file			the file to get parent directory for
	 * 
	 * @return				parent of file
	 */
	public static File getParentDirectory(File file) {
		if (file.getParent() != null) {
			return file.getParentFile();
		} else {
			return (new File(file.getAbsolutePath())).getParentFile();
		}
	}

	/**
	 * Get the filename extension of the given file.
	 * 
	 * @param file			file to get extension of
	 * 
	 * @return				filename extension
	 */
	public static String getExtension(String fileName) {
		if (fileName == null) {
			return null;
		}
		int i = fileName.lastIndexOf('.');
		return (i >= 0)? fileName.substring(i+1) : null; 
	}
	
	/**
	 * Get the filename extension of the given file.
	 * 
	 * @param file			file to get extension of
	 * 
	 * @return				filename extension
	 */
	public static String getExtension(File file) {
		if (file == null) {
			return null;
		}
		return getExtension(file.getName());
	}

	/**
	 * Get the filename extension of the given file in lower case.
	 * 
	 * @param file			file to get extension of
	 * 
	 * @return				filename extension in lower case
	 */
	public static String getExtensionLowerCase(String fileName) {
		String extension = getExtension(fileName);
		return (extension == null)? null : extension.toLowerCase();
	}
	
	/**
	 * Get the filename extension of the given file in lower case.
	 * 
	 * @param file			file to get extension of
	 * 
	 * @return				filename extension in lower case
	 */
	public static String getExtensionLowerCase(File file) {
		if (file == null) {
			return null;
		}
		return getExtensionLowerCase(file.getName());
	}
	
	/**
	 * Get the name of a file less it's extension.
	 * 
	 * @param fileName		file name
	 * 
	 * @return				file name without extension
	 */
	public static String getFileNameLessExtension(String fileName) {
		String extension = getExtension(fileName);
		if (extension == null) {
			return fileName;
		} else {
			return fileName.substring(0, fileName.length() - extension.length() - 1);
		}
	}

	/**
	 * Format the size (length) of the given file as a String with the given max binary prefix units.
	 * The returned number will be rounded to a variable number of fraction digits based
	 * on the binary prefix.
	 * 
	 * @param file					file to get file size of
	 * @param maxBinaryPrefix		max binary prefix unit to use
	 * 
	 * @return						file size formatted as string
	 */
	public static String formatFileSize(File file, BinaryPrefix maxBinaryPrefix) {
		return formatFileSize(file.length(), maxBinaryPrefix, null, null);
	}
	
	/**
	 * Format the size (length) of the given file as a String with the given max binary prefix units.
	 * 
	 * @param file					file to get file size of
	 * @param maxBinaryPrefix		max binary prefix unit to use
	 * @param minFractionDigits		min fraction digits, or null for automatic
	 * @param maxFractionDigits		max fraction digits, or null for automatic
	 * 
	 * @return						file size formatted as string
	 */
	public static String formatFileSize(File file, BinaryPrefix maxBinaryPrefix, Integer minFractionDigits, Integer maxFractionDigits) {
		return formatFileSize(file.length(), maxBinaryPrefix, minFractionDigits, maxFractionDigits);
	}

	/**
	 * Format the size (length in bytes) as a String with the given max binary prefix units.
	 * The returned number will be rounded to a variable number of fraction digits based
	 * on the binary prefix.
	 * 
	 * @param size					size in bytes
	 * @param maxBinaryPrefix		max binary prefix unit to use
	 * 
	 * @return						file size formatted as string
	 */
	public static String formatFileSize(long size, BinaryPrefix maxBinaryPrefix) {
		return formatFileSize(size, maxBinaryPrefix, null, null);
	}
	
	/**
	 * Format the size (length in bytes) as a String with the given max binary prefix units.
	 * 
	 * @param size					size in bytes
	 * @param maxBinaryPrefix		max binary prefix unit to use
	 * @param minFractionDigits		min fraction digits, or null for automatic
	 * @param maxFractionDigits		max fraction digits, or null for automatic
	 * 
	 * @return						file size formatted as string
	 */
	public static String formatFileSize(long size, BinaryPrefix maxBinaryPrefix, Integer minFractionDigits, Integer maxFractionDigits) {
		return formatFileSizeValue(size, maxBinaryPrefix, minFractionDigits, maxFractionDigits) 
			+ " " 
			+ formatFileSizePrefix(size, maxBinaryPrefix);
	}

	/**
	 * Format the size (length in bytes) as a String with the given max binary prefix units.
	 * The returned number will be rounded to a variable number of decimal places depending
	 * on the resulting binary prefix.  Units will be excluded from the returned string.
	 * 
	 * @param size					size in bytes
	 * @param maxBinaryPrefix		max binary prefix unit to use
	 * 
	 * @return						file size formatted as string
	 */
	public static String formatFileSizeValue(long size, BinaryPrefix maxBinaryPrefix) {
		return formatFileSizeValue(size, maxBinaryPrefix, null, null);
	}
	
	/**
	 * Format the size (length in bytes) as a String with the given max binary prefix units.
	 * The returned number will be rounded to a variable number of decimal places depending
	 * on the resulting binary prefix.  Units will be excluded from the returned string.
	 * 
	 * @param size					size in bytes
	 * @param maxBinaryPrefix		max binary prefix unit to use
	 * @param minFractionDigits		min fraction digits, or null for automatic
	 * @param maxFractionDigits		max fraction digits, or null for automatic
	 * 
	 * @return						file size formatted as string
	 */
	public static String formatFileSizeValue(long size, BinaryPrefix maxBinaryPrefix, Integer minFractionDigits, Integer maxFractionDigits) {
		BinaryPrefix prefix = BinaryPrefix.bytes;
		double doubleSize = (double) size;
		while (doubleSize > 1024d && prefix.ordinal() < maxBinaryPrefix.ordinal()) {
			doubleSize /= 1024d;
			prefix = BinaryPrefix.values()[prefix.ordinal() + 1];
		}
		NumberFormat nf = NumberFormat.getInstance();
		if (minFractionDigits != null) {
			nf.setMinimumFractionDigits(minFractionDigits.intValue());
		}
		int minfd = (minFractionDigits == null)? 0 : minFractionDigits.intValue();
		int preferredmax = Math.max(0, prefix.ordinal()-1);  // don't bother with fractions until we get to MiB; show 1 for MiB, 2 for GiB, 3 for TiB
		int automax = (maxFractionDigits == null)? preferredmax : Math.min(preferredmax, maxFractionDigits.intValue());
		nf.setMaximumFractionDigits(Math.max(automax, minfd));
		return nf.format(doubleSize);		
	}

	/**
	 * Format the size (length in bytes) as a String with the given max binary prefix units.
	 * The returned value will be the binary prefix unit only.  
	 * 
	 * @param size				size in bytes
	 * @param maxBinaryPrefix	max binary prefix unit to use
	 * 
	 * @return					binary prefix unit as String
	 */
	public static String formatFileSizePrefix(long size, BinaryPrefix maxBinaryPrefix) {
		return getFileSizePrefix(size, maxBinaryPrefix).toString();		
	}
	
	/**
	 * Get the preferred BinaryPrefix for the given size in bytes.  BinaryPrefix will 
	 * not exceed the provided maxBinaryPrefix.
	 * 
	 * @param size				size in bytes
	 * @param maxBinaryPrefix	max binary prefix to return
	 * 
	 * @return					preferred binary prefix
	 */
	public static BinaryPrefix getFileSizePrefix(long size, BinaryPrefix maxBinaryPrefix) {
		BinaryPrefix prefix = BinaryPrefix.bytes;
		double doubleSize = (double) size;
		while (doubleSize > 1024d && prefix.ordinal() < maxBinaryPrefix.ordinal()) {
			doubleSize /= 1024d;
			prefix = BinaryPrefix.values()[prefix.ordinal() + 1];
		}
		return prefix;			
	}
	
	/**
	 * Convert the given size from the "fromUnit" to the "toUnit".
	 * 
	 * @param size			the size
	 * @param fromUnit		unit size is currently in
	 * @param toUnit		unit desired
	 * 
	 * @return				size in new unit
	 */
	public static double convertFileSize(double size, BinaryPrefix fromUnit, BinaryPrefix toUnit) {
		return (double) (size * fromUnit.getByteMultiplier()) / (double) toUnit.getByteMultiplier();
	}
	
	/**
	 * Returns whether or not file appears to be an image.  Existence of file is not checked, nor
	 * is the integrity of the image itself.
	 *  
	 * @param file
	 * @return
	 */
	public static boolean isImage(File file) {
		String extension = getExtensionLowerCase(file);
		return ImageUtil.VALID_IMAGE_FORMATS.contains(extension);
	}
	
	/**
	 * Return a String array of path components of the absolute path of the given file.  
	 * This method serves as a shortcut when wanting to split a file path on the File.separator;
	 * the File.separator needs to be escaped on some platforms.
	 * 
	 * @param file		file whose path you wish to split
	 * 
	 * @return			path components as a String array
	 */
	public static String[] splitOnFileSeparator(File file) {
		// double escaping is necessary -- once for Java, once for the regex pattern
		return file.getAbsolutePath().split(File.separator.replaceAll("\\\\", "\\\\\\\\"));
	}
	
	/**
	 * Delete a directory, including all files and subdirectories within.
	 * 
	 * @param directory	directory to delete
	 */
	public static void delete(File directory) {
		if (directory.isDirectory()) {
			File[] subfiles = directory.listFiles();
			for (File subfile : subfiles) {
				delete(subfile);
			}
		} 
		directory.delete();
	}
	
	/**
	 * Generates a regular expression to match file names based on the file name pattern.
	 * File name pattern takes the form of that used by typical operating systems, with  
	 * periods and asterisks as special characters.  
	 * 
	 * @param fileNamePattern		O/S style file name pattern
	 * 
	 * @return						regular expression
	 */
	public static String generateRegularExpression(String fileNamePattern) {
		while (fileNamePattern.endsWith("*.*")) {
			fileNamePattern = fileNamePattern.substring(0, fileNamePattern.length()-2);
		}
		StringBuilder rxPattern = new StringBuilder();
		rxPattern.append("^");
		for (int i=0; i<fileNamePattern.length(); i++) {
			switch (fileNamePattern.charAt(i)) {
			case '.':
				rxPattern.append("\\.");
				break;
			case '*':
				rxPattern.append(".*");
				break;
			default:
				rxPattern.append(fileNamePattern.charAt(i));
				break;
			}
		}
		rxPattern.append("$");
		return rxPattern.toString();
	}
	
	/**
	 * Recurse directories within a list of files and return a set of all individual files.
	 * Use this method with caution.
	 * 
	 * @param files		list of files and directories
	 * 
	 * @return			list of all files with directories recursed
	 */
	public static Set<File> getAllFiles(Collection<File> files) {
		Set<File> allFiles = new HashSet<File>();
		for (File file : files) {
			if (file.isDirectory()) {
				File[] subFiles = file.listFiles();
				if (subFiles != null) {
					allFiles.addAll(getAllFiles(Arrays.asList(subFiles)));
				}
			} else if (!allFiles.contains(file)){
				allFiles.add(file);
			}
		}
		return allFiles;
	}
}

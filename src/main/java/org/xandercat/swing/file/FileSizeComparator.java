package org.xandercat.swing.file;

import java.io.File;
import java.util.Comparator;

public class FileSizeComparator implements Comparator<File> {

	private boolean ascending;
	private boolean recurseDirectories;
	
	public FileSizeComparator(boolean ascending, boolean recurseDirectories) {
		this.ascending = ascending;
		this.recurseDirectories = recurseDirectories;
	}
	
	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public boolean isRecurseDirectories() {
		return recurseDirectories;
	}

	public void setRecurseDirectories(boolean recurseDirectories) {
		this.recurseDirectories = recurseDirectories;
	}

	private long getSize(File file) {
		long size = file.length();
		if (file.isDirectory() && recurseDirectories) {
			DirectorySizeCache directorySizeCache = DirectorySizeCache.getInstance();
			size += directorySizeCache.getBytes(file).longValue();
		}
		return size;
	}
	
	public int compare(File file1, File file2) {
		if (file1 == null) {
			return ascending? 1 : -1;
		}
		if (file2 == null) {
			return ascending? -1 : 1;
		}
		long l1 = getSize(file1);
		long l2 = getSize(file2);
		if (l1 == l2) {
			return file1.getName().compareToIgnoreCase(file2.getName());
		} else if (l1 > l2) {
			return ascending? 1 : -1;
		} else {
			return ascending? -1 : 1;
		}
	}
}

package org.xandercat.swing.file;

/**
 * FilesSize stores information on the size of a set of files, including directory count,
 * file count, and size in bytes.  This class stores the results generated by a FilesSizeCalculator.
 * 
 * @author Scott C Arnold
 */
public class FilesSize implements Cloneable, Comparable<FilesSize> {

	private long bytes;
	private int directories;
	private int files;
	
	public FilesSize() {
	}
	
	public FilesSize(long bytes, int directories, int files) {
		this.bytes = bytes;
		this.directories = directories;
		this.files = files;
	}
	
	public long getBytes() {
		return bytes;
	}
	
	public int getDirectories() {
		return directories;
	}
	
	public int getFiles() {
		return files;
	}
	
	public void addBytes(long bytes) {
		this.bytes += bytes;
	}
	
	public void removeBytes(long bytes) {
		this.bytes -= bytes;
	}
	
	public void addDirectories(int directories) {
		this.directories += directories;
	}
	
	public void removeDirectories(int directories) {
		this.directories -= directories;
	}
	
	public void addFiles(int files) {
		this.files += files;
	}
	
	public void removeFiles(int files) {
		this.files -= files;
	}
	
	public void add(FilesSize size) {
		this.bytes += size.getBytes();
		this.directories += size.getDirectories();
		this.files += size.getFiles();
	}
	
	public void remove(FilesSize size) {
		this.bytes -= size.getBytes();
		this.directories -= size.getDirectories();
		this.files -= size.getFiles();
	}
	
	public int compareTo(FilesSize filesSize) {
		long dif = bytes - filesSize.getBytes();
		return (dif < 0)? 1 : (dif > 0)? -1 : 0;
	}
	
	public FilesSize clone() {
		return new FilesSize(this.bytes, this.directories, this.files);
	}

	@Override
	public String toString() {
		return getClass().getName() + "[bytes=" + bytes + ";directories=" + directories + ";files=" + files + "]";
	}
}

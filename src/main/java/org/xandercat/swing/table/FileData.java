package org.xandercat.swing.table;

import java.io.File;

import org.xandercat.swing.util.FileUtil;

/**
 * FileData stores information on a file to be used by a file table.  A FileData is 
 * considered equal to another if their File objects are equal.
 * 
 * @author Scott C Arnold
 */
public class FileData {

	private File file;
	private String shortName;
	private Long length;
	private String fileType;
	
	public FileData(File file) {
		this.file = file;
		this.shortName = FileUtil.getShortName(file);
		this.fileType = file.isDirectory()? "Directory" : FileUtil.getExtensionLowerCase(file);
		this.length = Long.valueOf(file.length());
	}
	public File getFile() {
		return file;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public Long getLength() {
		return length;
	}	
	public void setLength(Long length) {
		this.length = length;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FileData) {
			return file.equals(((FileData) obj).file);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return file.hashCode();
	}
	@Override
	public String toString() {
		return "FileData[file=" + ((file == null)? "null" : file.getAbsolutePath()) + "]";
	}
}

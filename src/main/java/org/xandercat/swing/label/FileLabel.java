package org.xandercat.swing.label;

import java.io.File;

import javax.swing.JLabel;

import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.util.ResourceManager;

/**
 * A JLabel intended to display a file absolute path.  The icon is automatically set
 * via a FileIconCache which can be explicitly set for the label.  If a FileIconCache
 * is not explicitly set for the label, it is retrieved from ResourceManager.  If a
 * FileIconCache cannot be retrieved from ResourceManager, no icon is set.
 * 
 * @author Scott Arnold
 */
public class FileLabel extends JLabel {

	private static final long serialVersionUID = 2010073001L;
	
	private FileIconCache fileIconCache;
	private int maxDisplayChars = -1;
	private boolean displayFullPath = true;
	private File file;
	
	public FileLabel() {
		this((File) null);
	}

	public FileLabel(int maxDisplayChars) {
		this(null, maxDisplayChars);
	}
	
	public FileLabel(boolean displayFullPath) {
		this(null, displayFullPath);
	}
	
	public FileLabel(File file) {
		this(file, -1);
	}
	
	public FileLabel(File file, boolean displayFullPath) {
		this(file, -1, displayFullPath);
	}
	
	public FileLabel(File file, int maxDisplayChars) {
		this(file, maxDisplayChars, true);
	}

	public FileLabel(File file, int maxDisplayChars, boolean displayFullPath) {
		super();
		this.file = file;
		this.displayFullPath = displayFullPath;
		this.maxDisplayChars = maxDisplayChars;
		setTextFromFile(file);
		setIconByFile(file);		
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
		setTextFromFile(file);
		setIconByFile(file);
	}
	
	public String getAbsolutePath() {
		return (file == null)? null : file.getAbsolutePath();
	}
	
	public void setAbsolutePath(String absolutePath) {
		if (absolutePath == null || absolutePath.trim().length() == 0) {
			setFile(null);
		} else {
			setFile(new File(absolutePath));
		}
	}
	
	public void setFilePath(String absolutePath) {
		setFile(new File(absolutePath));
	}
	
	private void setTextFromFile(File file) {
		String absPath = (file == null)? null : file.getAbsolutePath();
		String fileName = (file == null)? null : file.getName();
		String displayText = this.displayFullPath? absPath : fileName;
		if (this.maxDisplayChars > 0 && displayText != null && displayText.length() > this.maxDisplayChars) {
			displayText = "..." + displayText.substring(displayText.length() - this.maxDisplayChars);
		} 
		super.setText(displayText);
		if (displayText == null || displayText.equals(absPath)) {
			super.setToolTipText(null);
		} else {
			super.setToolTipText(absPath);
		}
	}
	
	public void setFileIconCache(FileIconCache fileIconCache) {
		this.fileIconCache = fileIconCache;
	}
	
	@Override
	public void setText(String text) {
		setFile(null);
		super.setText(text);
	}

	private void setIconByFile(File file) {
		if (file == null) {
			setIcon(null);
		} else {
			FileIconCache fileIconCache = this.fileIconCache;
			if (fileIconCache == null) {
				fileIconCache = ResourceManager.getInstance().getResource(FileIconCache.class);
			}
			if (fileIconCache != null) {
				setIcon(fileIconCache.get(file));
			}
		}
	}
}

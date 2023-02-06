package org.xandercat.swing.tree;

import java.io.File;

import javax.swing.Icon;

import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.util.FileUtil;

public class FileTreeItem {

	private File file;
	private String text;
	private Icon icon;
	private boolean invalid;	// true if file did not exist when FileTreeItem was created
	
	public FileTreeItem(File file, Icon icon) {
		this.file = file;
		this.icon = icon;
		if (file != null) {
			this.text = FileUtil.getShortName(file) + "    ";	// added spaces are gimp solution to renderer cutoff problem
		}
		this.invalid = (file == null || !Boolean.TRUE.equals(FileUtil.exists(file)));
	}
	
	public FileTreeItem(File file, FileIconCache fileIconCache) {
		this(file, fileIconCache.get(file));
	}
	
	public FileTreeItem(String text) {
		this.text = text;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getText() {
		return text;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public boolean isInvalid() {
		return invalid;
	}
}

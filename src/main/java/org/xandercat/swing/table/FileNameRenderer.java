package org.xandercat.swing.table;

import java.io.File;

import javax.swing.table.DefaultTableCellRenderer;

import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.util.FileUtil;

/**
 * FileNameRenderer renders a file using an icon and a short file name.
 * 
 * @author Scott C Arnold
 */
public class FileNameRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 2009022101L;
	
	private FileIconCache fileIconCache;
	private boolean showFullPath = false;
	private boolean showIcon = true;

	public FileNameRenderer(FileIconCache fileIconCache) {
		super();
		this.fileIconCache = fileIconCache;
	}
	
	public FileNameRenderer(FileIconCache fileIconCache, boolean showFullPath, boolean showIcon) {
		this(fileIconCache);
		this.showFullPath = showFullPath;
		this.showIcon = showIcon;
	}
	
	public boolean isShowFullPath() {
		return showFullPath;
	}

	public void setShowFullPath(boolean showFullPath) {
		this.showFullPath = showFullPath;
	}

	public boolean isShowIcon() {
		return showIcon;
	}

	public void setShowIcon(boolean showIcon) {
		this.showIcon = showIcon;
	}

	@Override
	protected void setValue(Object value) {
		File file = (File) value;
		if (showFullPath) {
			setText((file == null)? "" : file.getAbsolutePath());
		} else {
			setText(FileUtil.getShortName(file));
		}
		if (showIcon) {
			setIcon(fileIconCache.get(file));
		}
	}
}

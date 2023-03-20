package org.xandercat.swing.tree;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.file.icon.FileIconOverlayType;
import org.xandercat.swing.util.FileUtil;

/**
 * Renderer for file trees.  Rendered component is a JLabel.  Rendered values are
 * expected to be FileTreeNode objects.
 * 
 * @author Scott Arnold
 */
public class FileTreeCellRenderer implements TreeCellRenderer {

	private DefaultTreeCellRenderer defaultRenderer;
	private FileIconCache fileIconCache;
	
	public FileTreeCellRenderer(FileIconCache fileIconCache) {
		this.defaultRenderer = new DefaultTreeCellRenderer();
		this.fileIconCache = fileIconCache;
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		FileTreeNode node = (FileTreeNode) value;
		JLabel label = (JLabel) defaultRenderer.getTreeCellRendererComponent(
				tree, node.getText(), selected, expanded, leaf, row, hasFocus);
		if (expanded && node.getFile() != null && !FileUtil.isDirectoryRootPath(node.getFile())) {	// only folders can be expanded
			if (node.isInvalid()) {
				label.setIcon(fileIconCache.getFolderOpenIcon(FileIconOverlayType.ERROR));
			} else if (node.getInvalidDescendantsCount() > 0) {
				label.setIcon(fileIconCache.getFolderOpenIcon(FileIconOverlayType.WARNING));
			} else {
				label.setIcon(fileIconCache.getFolderOpenIcon());
			}			
		} else {
			if (node.isInvalid()) {
				if (leaf) {
					label.setIcon(fileIconCache.get(node.getFile(), FileIconOverlayType.ERROR));
				} else if (FileUtil.isDirectoryRootPath(node.getFile())) {
					label.setIcon(fileIconCache.getDriveIcon(FileIconOverlayType.ERROR));
				} else {
					label.setIcon(fileIconCache.getFolderIcon(FileIconOverlayType.ERROR));
				}
			} else if (node.getInvalidDescendantsCount() > 0) {
				label.setIcon(fileIconCache.get(node.getFile(), FileIconOverlayType.WARNING));
			} else {
				label.setIcon(node.getIcon(fileIconCache));
			}
		}		
		return label;
	}
}

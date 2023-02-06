package org.xandercat.swing.tree;

import java.io.File;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.file.icon.FileIconOverlayType;
import org.xandercat.swing.util.FileUtil;

/**
 * FileTreeNode represents a file in a file system tree and keeps track of how many invalid
 * descendants it has and if it's children are loaded.
 * 
 * @author Scott C Arnold
 */
public class FileTreeNode extends DefaultMutableTreeNode implements TreeStateSaveableNode {

	private static final long serialVersionUID = 2009022001L; 
	
	private FileTreeItem item;
	private volatile boolean childrenLoaded;
	private int invalidDescendants = 0;
	
	public FileTreeNode(FileTreeItem item) {
		super(item);
		this.item = item;
	}
	
	public FileTreeNode(File file, FileIconCache fileIconCache) {
		this(new FileTreeItem(file, fileIconCache));
	}
	
	public FileTreeNode(String rootTitle) {
		this(new FileTreeItem(rootTitle));
		//TODO: Why did I do it this way?  the children aren't really loaded yet
		childrenLoaded = true;
	}
	
	public Serializable getUniqueId() {
		if (item == null || item.getFile() == null) {
			return "";
		}
		return item.getFile().getAbsolutePath();
	}
	
	public boolean isChildrenLoaded() {
		return childrenLoaded;
	}
	
	public void setChildrenLoaded(boolean childrenLoaded) {
		this.childrenLoaded = childrenLoaded;
	}
	
	public FileTreeItem getItem() {
		return item;
	}
	
	public String getText() {
		return item.getText();
	}
	
	public Icon getIcon(FileIconCache fileIconCache) {
		if (item.getFile() != null && !isChildrenLoaded()) {
			Boolean isDirectory = FileUtil.isDirectory(item.getFile());
			if (isDirectory == null || isDirectory.booleanValue()) {
				return fileIconCache.get(item.getFile(), FileIconOverlayType.LOADING);
			}
		} 
		return item.getIcon();
	}
	
	public File getFile() {
		return item.getFile();
	}
	
	public boolean isInvalid() {
		return item.isInvalid();
	}
	
	public int getInvalidDescendantsCount() {
		return invalidDescendants;
	}

	private void addInvalidDescendant() {
		invalidDescendants++;
		if (getParent() != null) {
			((FileTreeNode) getParent()).addInvalidDescendant();
		}
	}
	
	private void removeInvalidDescendants(int invalidDescendants) {
		this.invalidDescendants -= invalidDescendants;
		if (getParent() != null) {
			((FileTreeNode) getParent()).removeInvalidDescendants(invalidDescendants);
		}
	}
	
	@Override
	public void add(MutableTreeNode node) {
		super.add(node);
		if (((FileTreeNode) node).isInvalid()) {
			addInvalidDescendant();
		}
	}

	@Override
	public void insert(MutableTreeNode node, int childIndex) {
		super.insert(node, childIndex);
		if (((FileTreeNode) node).isInvalid()) {
			addInvalidDescendant();
		}
	}

	@Override
	public void remove(int childIndex) {
		FileTreeNode child = (FileTreeNode) getChildAt(childIndex);
		if (child.getInvalidDescendantsCount() > 0) {
			child.removeInvalidDescendants(child.getInvalidDescendantsCount());
		}
		super.remove(childIndex);
	}

	@Override
	public void remove(MutableTreeNode child) {
		if (child.getParent() == this) {
			FileTreeNode childNode = (FileTreeNode) child;
			if (childNode.getInvalidDescendantsCount() > 0) {
				childNode.removeInvalidDescendants(childNode.getInvalidDescendantsCount());
			}
		}
		super.remove(child);
	}

	@Override
	public void removeAllChildren() {
		if (invalidDescendants > 0) {
			// we take a bit of a shortcut here so that we don't have to call removeInvalidDescendants for each child
			for (int i=0,j=getChildCount(); i<j; i++) {
				FileTreeNode child = (FileTreeNode) getChildAt(i);
				child.invalidDescendants = 0;
			}
			removeInvalidDescendants(invalidDescendants);
		}
		super.removeAllChildren();
	}

	@Override
	public void removeFromParent() {
		FileTreeNode parent = (FileTreeNode) getParent();
		if (parent != null && invalidDescendants > 0) {
			removeInvalidDescendants(invalidDescendants);
		}
		super.removeFromParent();
	}
}

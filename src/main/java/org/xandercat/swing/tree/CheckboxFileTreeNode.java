package org.xandercat.swing.tree;

import java.io.File;

import javax.swing.Icon;
import javax.swing.tree.MutableTreeNode;

import org.xandercat.swing.file.icon.FileIconCache;

/**
 * CheckboxFileTreeNode represents a node of a CheckboxFileTree.  Counts are kept of the 
 * number of selected children (immediate children only) and selected descendants (children,
 * grandchildren, etc).
 * 
 * @author Scott C Arnold
 */
public class CheckboxFileTreeNode extends FileTreeNode implements CheckboxTreeNodable {

	private static final long serialVersionUID = 2009030301L;
	
	private CheckboxFileTree tree;
	private int selectedChildren = 0;
	private int selectedDescendants = 0;
	private boolean selected;

	public CheckboxFileTreeNode(CheckboxFileTree tree, File file, FileIconCache fileIconCache, boolean selected) {
		super(file, fileIconCache);
		this.tree = tree;
		this.selected = selected;
	}
	
	public CheckboxFileTreeNode(String rootTitle) {
		super(rootTitle);
	}
	
	public int getSelectedChildCount() {
		return selectedChildren;
	}

	public int getSelectedDescendantsCount() {
		return selectedDescendants;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		if (this.selected != selected) {
			setSelectedInternal(selected);
			if (tree != null) {
				tree.userNodeSelectionChange(this);
			}
		}
	}
	
	protected void setSelectedInternal(boolean selected) {
		if (this.selected == selected) {
			return;
		}
		this.selected = selected;
		for (int i=0,j=getChildCount(); i<j; i++) {
			CheckboxFileTreeNode childNode = (CheckboxFileTreeNode) getChildAt(i);
			childNode.setSelectedInternal(selected);
		}
		if (getParent() != null) {
			CheckboxFileTreeNode parent = (CheckboxFileTreeNode) getParent();
			parent.childSelectionChange(parent, selected);
		}
	}
	
	protected void childSelectionChange(CheckboxFileTreeNode parent, boolean selected) {
		if (selected) {
			selectedDescendants++;
			if (this == parent) {
				selectedChildren++;
			}
		} else {
			selectedDescendants--;
			if (this == parent) {
				selectedChildren--;
			}
			if (this.selected) {
				this.selected = false;	// item can only be considered selected when all of it's children are selected
				if (getParent() != null) {
					((CheckboxFileTreeNode) getParent()).childSelectionChange(parent, selected);
				}
			}
		}
		if (getParent() != null) {
			((CheckboxFileTreeNode) getParent()).childSelectionChange(parent, selected);
		}
	}
	
	protected void childSelectionAdd(CheckboxFileTreeNode parent, boolean selected) {
		if (selected) {
			selectedDescendants++;
			if (this == parent) {
				selectedChildren++;
			}
		} else {
			if (this.selected) {
				this.selected = false;	// item can only be considered selected when all of it's children are selected
				if (getParent() != null) {
					((CheckboxFileTreeNode) getParent()).childSelectionChange(parent, selected);
				}
			}
		}
		if (getParent() != null) {
			((CheckboxFileTreeNode) getParent()).childSelectionAdd(parent, selected);
		}		
	}
	
	protected void childSelectionRemove(CheckboxFileTreeNode parent, boolean selected) {
		if (selected) {
			selectedDescendants--;
			if (this == parent) {
				selectedChildren--;
			}
		} 
		if (getParent() != null) {
			((CheckboxFileTreeNode) getParent()).childSelectionRemove(parent, selected);
		}
	}
	
	protected void childSelectionRemoveAll(CheckboxFileTreeNode parent, int selectedDescendants) {
		this.selectedDescendants -= selectedDescendants;
		if (getParent() != null) {
			((CheckboxFileTreeNode) getParent()).childSelectionRemoveAll(parent, selectedDescendants);
		}
	}
	
	@Override
	public void add(MutableTreeNode newChild) {
		super.add(newChild);
		CheckboxFileTreeNode node = (CheckboxFileTreeNode) newChild;
		childSelectionAdd(this, node.isSelected());
	}

	@Override
	public void insert(MutableTreeNode node, int childIndex) {
		super.insert(node, childIndex);
		CheckboxFileTreeNode checkboxNode = (CheckboxFileTreeNode) node;
		childSelectionAdd(this, checkboxNode.isSelected());
	}

	@Override
	public void remove(int childIndex) {
		CheckboxFileTreeNode checkboxNode = (CheckboxFileTreeNode) getChildAt(childIndex);
		super.remove(childIndex);
		childSelectionRemove(this, checkboxNode.isSelected());
	}

	@Override
	public void remove(MutableTreeNode child) {
		super.remove(child);
		CheckboxFileTreeNode checkboxNode = (CheckboxFileTreeNode) child;
		childSelectionRemove(this, checkboxNode.isSelected());
	}

	@Override
	public void removeAllChildren() {
		super.removeAllChildren();
		this.selectedChildren = 0;
		childSelectionRemoveAll(this, this.selectedDescendants);
	}

	@Override
	public void removeFromParent() {
		throw new UnsupportedOperationException("Unfinished operation");
	}

	public Icon getIcon() {
		return (tree == null)? null : getIcon(tree.getFactory().fileIconCache);
	}
}

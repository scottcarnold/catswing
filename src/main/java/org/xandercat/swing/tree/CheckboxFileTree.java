package org.xandercat.swing.tree;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.util.FileUtil;

/**
 * CheckboxFileTree provides a file system file tree with checkboxes for each node of the tree
 * that allow a user to select one or more files/directories from the tree.
 * 
 * @author Scott C Arnold
 */
public class CheckboxFileTree extends FileTree implements MouseListener {
	
	private static final long serialVersionUID = 2009030301L;
	private static final Logger log = LogManager.getLogger(CheckboxFileTree.class);
		
	private List<CheckboxFileTreeListener> listeners;
	private List<File> checkedFiles = new ArrayList<File>();
	
	public CheckboxFileTree(CheckboxFileTreeFactory factory, CheckboxFileTreeNode root, FileIconCache fileIconCache) {
		super(factory, root, fileIconCache);
		// note:  the same FileTreeCellRenderer cannot be reused in both the renderer and editor
		setCellRenderer(new CheckboxTreeCellRenderer(new FileTreeCellRenderer(fileIconCache)));
		setCellEditor(new CheckboxTreeCellEditor(new FileTreeCellRenderer(fileIconCache)));	
		setEditable(true);
		setExpandSelectedPaths(true);
		addMouseListener(this);
	}
	
	public void addCheckboxFileTreeListener(CheckboxFileTreeListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<CheckboxFileTreeListener>();
		}
		listeners.add(listener);
	}
	
	public void removeCheckboxFileTreeListener(CheckboxFileTreeListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Return whether or not a particular file is checked in the tree.
	 * 
	 * @param file		file to test
	 * 
	 * @return			whether or not file is checked in the tree
	 */
	public boolean isChecked(File file) {
		if (file.getAbsolutePath().startsWith("\\\\")) {
			//TODO: Keep an eye on this; for the moment, checkbox file trees do not list network drives
			return false;
		}
		CheckboxFileTreeNode node = (CheckboxFileTreeNode) getModel().getRoot();
		while (!node.isSelected() && node.getSelectedDescendantsCount() > 0) {
			node = (CheckboxFileTreeNode) findNextNode(node, file);
			if (node == null) {
				log.warn("Node for file " + file.getAbsolutePath() + " not found.");
				return false;
			}
			if (node.getFile().equals(file)) {
				return node.isSelected();
			}
		}
		return node.isSelected();
	}
	
	/**
	 * Return whether or not any descendants to the given file are checked in the tree.
	 * 
	 * @param file		file to test
	 * 
	 * @return			whether or not any descendants of the file are checked in the tree
	 */
	public boolean isDescendantChecked(File file) {
		CheckboxFileTreeNode node = (CheckboxFileTreeNode) getModel().getRoot();
		while (node.getSelectedDescendantsCount() > 0) {
			node = (CheckboxFileTreeNode) findNextNode(node, file);
			if (node == null) {
				log.warn("Node for file " + file.getAbsolutePath() + " not found.");
				return false;
			}
			if (node.getFile().equals(file)) {
				return node.getSelectedDescendantsCount() > 0;
			}
		}
		return node.getSelectedDescendantsCount() > 0;
	}
	
	/**
	 * Return a list of files that are checked beneath the given node.
	 * 
	 * @param node		node to get descendant files for
	 * 
	 * @return			list of files that are checked beneath the given node
	 */
	public List<File> getCheckedDescendantFiles(CheckboxFileTreeNode node) {
		List<File> checkedDescendants = new ArrayList<File>();
		if (node != null) {
			Enumeration<CheckboxFileTreeNode> childEnumeration = node.children();
			while (childEnumeration.hasMoreElements()) {
				CheckboxFileTreeNode childNode = childEnumeration.nextElement();
				if (childNode.isSelected()) {
					checkedDescendants.add(childNode.getFile());
				} else if (childNode.getSelectedDescendantsCount() > 0) {
					checkedDescendants.addAll(getCheckedDescendantFiles(childNode));
				}
			}
		}
		return checkedDescendants;		
	}
	
	/**
	 * Return a list of files that are checked beneath the node for the given file.
	 * 
	 * @param file		file to get checked descendants of
	 * 
	 * @return			checked descendants
	 */
	public List<File> getCheckedDescendantFiles(File file) {
		CheckboxFileTreeNode node = (CheckboxFileTreeNode) findNodeForFile(file);
		return getCheckedDescendantFiles(node);
	}
	
	/**
	 * Get the files selected by the user.  This is a minimal set of files; to get a complete
	 * list of every file, any directories among the selected files would need to be recursed
	 * for all files within it.
	 * 
	 * @return			list of selected files in the tree
	 */
	public List<File> getCheckedFiles() {
		List<File> checkedFilesCopy = new ArrayList<File>();
		checkedFilesCopy.addAll(checkedFiles);
		return checkedFilesCopy;
	}
	
	@SuppressWarnings("rawtypes")
	private List<File> getCheckedFiles(CheckboxFileTreeNode node) {
		List<File> files = new ArrayList<File>();
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			CheckboxFileTreeNode child = (CheckboxFileTreeNode) e.nextElement();
			if (child.isSelected()) {
				files.add(child.getFile());
			} else if (child.getSelectedDescendantsCount() > 0) {
				files.addAll(getCheckedFiles(child));
			}
		}
		return files;
	}
	
	/**
	 * This method is called by CheckboxFileTreeNodes whose selected attribute was changed
	 * by the user.  Method is package private to restrict access as much as possible without
	 * preventing nodes access.
	 * 
	 * @param node		node user changed selected status on
	 */
	void userNodeSelectionChange(final CheckboxFileTreeNode node) {
		// some versions of Java seem to update parent node appearance, some do not; for those
		// that do not, we call node changed on the model for all parent nodes
		CheckboxFileTreeNode parent = (CheckboxFileTreeNode) node.getParent();
		while (parent != null) {
			((DefaultTreeModel) getModel()).nodeChanged(parent);
			parent = (CheckboxFileTreeNode) parent.getParent();
		}
		
		CheckboxFileTreeNode root = (CheckboxFileTreeNode) getModel().getRoot();
		List<File> newCheckedFiles = getCheckedFiles(root);
		List<File> removedFiles = new ArrayList<File>();
		removedFiles.addAll(this.checkedFiles);
		removedFiles.removeAll(newCheckedFiles);
		List<File> addedFiles = new ArrayList<File>();
		addedFiles.addAll(newCheckedFiles);
		addedFiles.removeAll(this.checkedFiles);
		this.checkedFiles.removeAll(removedFiles);
		this.checkedFiles.addAll(addedFiles);
		fireFilesChanged(addedFiles, removedFiles);
		log.debug("user node selection change on " + node.getFile().getName() + "; added " + addedFiles.size() + "; removed " + removedFiles.size());
	}
	
	private void fireFilesChanged(List<File> addedFiles, List<File> removedFiles) {
		if (listeners != null) {
			for (CheckboxFileTreeListener listener : listeners) {
				listener.filesChanged(this, addedFiles, removedFiles);
			}
		}		
	}
	
	/**
	 * Select (or unselect) the given file in the checkbox tree; if the file is not in the tree, it will be added
	 * to the tree.
	 * 
	 * @param file				file to select in the tree
	 * @param isDirectory		whether or not the file is a directory (needed if file is not in the tree)
	 */
	public void selectAddFile(File file, boolean isDirectory, boolean selected) {
		String[] pathComponents = FileUtil.splitOnFileSeparator(file);
		CheckboxFileTreeNode node = (CheckboxFileTreeNode) getModel().getRoot();
		
		// find node for the given file
		int pci = 0;
		CheckboxFileTreeNode nextNode = (CheckboxFileTreeNode) findNextNode(node, pathComponents[pci]);
		while (nextNode != null && pci < pathComponents.length) {
			pci++;
			node = nextNode;
			if (pci < pathComponents.length) {
				nextNode = (CheckboxFileTreeNode) findNextNode(node, pathComponents[pci]);
			}
		}
//		log.debug("Closest node: " + node.getFile().getAbsolutePath());
		
		// if node not in current tree, build it in
		if (!file.equals(node.getFile())) {
//			log.debug("Building remaining path...");
			CheckboxFileTreeFactory factory = (CheckboxFileTreeFactory) getFactory();
			DefaultTreeModel model = (DefaultTreeModel) getModel();
			int index = 0; 
			CheckboxFileTreeNode childNode = (CheckboxFileTreeNode) node.getFirstChild();
			while (childNode != null && childNode.getFile().getName().compareTo(file.getName()) < 0) {
				childNode = (CheckboxFileTreeNode) childNode.getNextSibling();
				index++;
			}
			while (pci < pathComponents.length) {
				StringBuilder filePath = new StringBuilder();
				if (node.getFile() != null) {
					filePath.append(node.getFile().getAbsolutePath()).append(File.separator);
				}
				filePath.append(pathComponents[pci]);
				if (pci == 0) {
					// indicates root path which uniquely ends with file separator
					filePath.append(File.separator);
				}
				File newFile = new File(filePath.toString());
				CheckboxFileTreeNode newNode = (CheckboxFileTreeNode) factory.createNode(node, newFile);
				model.insertNodeInto(newNode, node, index);
				node = newNode;
				pci++;
				index = 0;
			}
		}
		
		// select or deselect matching node
		if (node.isSelected() != selected) {
			node.setSelected(selected);
			List<File> addedFiles = new ArrayList<File>();
			List<File> removedFiles = new ArrayList<File>();
			if (selected) {
				addedFiles.add(node.getFile());
			} else {
				removedFiles.add(node.getFile());
			}
			fireFilesChanged(addedFiles, removedFiles);
		}
	}
	


	public void mouseClicked(MouseEvent e) {		
	}

	public void mouseEntered(MouseEvent e) {	
	}

	public void mouseExited(MouseEvent e) {	
	}

	public void mousePressed(MouseEvent e) {	
	}

	public void mouseReleased(MouseEvent e) {
		// if tree is editing, a click on a checkbox on another node in the tree will result only
		// in node selection.  we want the checkbox value to change as well; we can accomplish
		// this by stopping the editing whenever the mouse button is released.
		stopEditing();
	}
}

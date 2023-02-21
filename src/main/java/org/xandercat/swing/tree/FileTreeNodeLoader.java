package org.xandercat.swing.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

/**
 * FileTreeNodeLoader will load two levels of children (children and grandchildren) from
 * the given node in the given tree.
 * 
 * @author Scott C Arnold
 */
public class FileTreeNodeLoader extends SwingWorker<FileTreeNode, FileTreeNodeLoader.ParentChildSet> {
			
	static class ParentChildSet {
		private FileTreeNode parent;
		private List<FileTreeNode> children;
		public ParentChildSet(FileTreeNode parent, List<FileTreeNode> children) {
			this.parent = parent;
			this.children = children;
		}
	}
	
	private FileTree tree;
	private FileTreeNode node;
	private boolean expand;
	
	/**
	 * Construct a new file tree node loader to load the children of the given node of 
	 * the given tree.  If the expand flag is set, the node will be expanded once it
	 * is loaded.  
	 * 
	 * @param tree		tree node belongs to
	 * @param node		node to load children for
	 * @param expand	whether or not to expand node once children are loaded
	 */
	public FileTreeNodeLoader(FileTree tree, FileTreeNode node, boolean expand) {
		this.tree = tree;
		this.node = node;
		this.expand = expand;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected FileTreeNode doInBackground() throws Exception {
		FileTreeFactory factory = tree.getFactory();
		List<FileTreeNode> children = null;
		if (node.isChildrenLoaded()) {
			// publish to ensure path is expanded
			publish(new ParentChildSet(node, null));
			children = new ArrayList<FileTreeNode>();
			Enumeration childEnum = node.children();
			while (childEnum.hasMoreElements()) {
				children.add((FileTreeNode) childEnum.nextElement());
			}
		} else {
			children = factory.createChildren(node);
			publish(new ParentChildSet(node, children));
		}
		for (FileTreeNode subNode : children) {
			if (!isCancelled() && !subNode.isChildrenLoaded()) {
				List<FileTreeNode> subChildren = factory.createChildren(subNode);
				publish(new ParentChildSet(subNode, subChildren));
			}
		}
		return node;
	}

	@Override
	protected void process(List<ParentChildSet> nodeSets) {
		FileTreeFactory factory = tree.getFactory();
		for (ParentChildSet nodeSet : nodeSets) {
			tree.lock();
			// see if node still needs loaded
			if (!nodeSet.parent.isChildrenLoaded() && nodeSet.children != null) {
				factory.loadChildren(tree, nodeSet.parent, nodeSet.children);
			}
			tree.unlock();
			if (expand && nodeSet.parent == node) {
				TreePath path = new TreePath(node.getPath());
				tree.expandPath(path);
			}
		}
	}
}

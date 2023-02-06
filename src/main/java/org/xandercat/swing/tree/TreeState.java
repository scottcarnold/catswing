package org.xandercat.swing.tree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TreeState implements Serializable {
	
	private static final long serialVersionUID = 2009103101L;
	private static final Logger log = LogManager.getLogger(TreeState.class);
			
	private Set<Object> openNodeIds = new HashSet<Object>();
	private int verticalScrollValue = -1;
	private int horizontalScrollValue = -1;
	
	public TreeState() {
	}
	
	public TreeState(JTree tree) {
		store(tree);
	}
	
	/**
	 * Stores the state of the given tree, including what paths are expanded and where any
	 * parent scroll pane is scrolled to.
	 * 
	 * @param tree
	 */
	public void store(JTree tree) {
		JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tree);
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		Object rootObject = model.getRoot();
		if (rootObject instanceof DefaultMutableTreeNode && rootObject instanceof TreeStateSaveableNode) {
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObject;
			TreePath rootPath = new TreePath(rootNode.getPath());
			if (tree.isExpanded(rootPath)) {
				this.openNodeIds.add(((TreeStateSaveableNode) rootNode).getUniqueId());
			}
			Enumeration<TreePath> nodeEnum = tree.getExpandedDescendants(rootPath);
			while (nodeEnum != null && nodeEnum.hasMoreElements()) {
				TreePath path = nodeEnum.nextElement();
				this.openNodeIds.add(((TreeStateSaveableNode) path.getLastPathComponent()).getUniqueId());
			}
		} else {
			throw new IllegalArgumentException("Nodes must be instances of TreeStateSaveable and DefaultMutableTreeNode");
		}
		if (scrollPane != null) {
			JScrollBar vScrollBar = scrollPane.getVerticalScrollBar();
			JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
			if (vScrollBar != null) {
				this.verticalScrollValue = vScrollBar.getValue();
			}
			if (hScrollBar != null) {
				this.horizontalScrollValue = hScrollBar.getValue();
			}
		}
	}
	
	/**
	 * Apply this state to the given tree and it's scroll pane.  The scroll pane can be null if
	 * there isn't one.  This action will attempt to expand all previously expanded paths.  It will
	 * not, however, collapse paths that were previously collapsed; this method should therefore
	 * typically be called on a fully collapsed tree.
	 * 
	 * This method is more efficient for JTrees that implement TreeStateSaveableTree.  If the 
	 * tree does not implement TreeStateSaveableTree, this method will do a breadth-first 
	 * enumeration through all nodes of the tree in order to find the nodes to be expanded.
	 * 
	 * @param tree				tree to apply this state to
	 */
	public void applyTo(JTree tree) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		Object rootObject = model.getRoot();
		if (rootObject instanceof DefaultMutableTreeNode && rootObject instanceof TreeStateSaveableNode) {
			if (tree instanceof TreeStateSaveableTree) {
				for (Object openNodeId : this.openNodeIds) {
					((TreeStateSaveableTree) tree).expandPathForId(openNodeId);
				}
			} else {
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObject;
				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> nodeEnum = rootNode.breadthFirstEnumeration();
				while (nodeEnum.hasMoreElements()) {
					DefaultMutableTreeNode node = nodeEnum.nextElement();
					TreePath path = new TreePath(node.getPath());
					boolean isExpanded = tree.isExpanded(path);
					boolean shouldBeExpanded = this.openNodeIds.contains(((TreeStateSaveableNode) node).getUniqueId());
					if (isExpanded != shouldBeExpanded) {
						if (shouldBeExpanded) {
							tree.expandPath(path);
						} 
					}
				}
			}
		} else {
			throw new IllegalArgumentException("Nodes must be instances of TreeStateSaveable and DefaultMutableTreeNode");
		}
	}
	
	/**
	 * Applies any previously saved scroll.
	 * 
	 * @param scrollPane
	 */
	public void applyTo(final JScrollPane scrollPane) {
		// now we can set the scroll bar values
		log.debug("Scroll values to restore: " + this.verticalScrollValue + ", " + this.horizontalScrollValue);
		if (scrollPane != null && (this.horizontalScrollValue > 0 || this.verticalScrollValue > 0)) {
			
			final JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
			if (this.verticalScrollValue > 0) {
				log.debug("Firing off runnable to set vertical scroll to " + verticalScrollValue);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JScrollBar vScrollBar = scrollPane.getVerticalScrollBar();
						vScrollBar.setValue(verticalScrollValue);
					}
				});
			}
			if (hScrollBar != null && this.horizontalScrollValue > 0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hScrollBar.setValue(horizontalScrollValue);
					}
				});	
			}
		}
	}
}

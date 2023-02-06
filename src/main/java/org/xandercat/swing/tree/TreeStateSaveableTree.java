package org.xandercat.swing.tree;

import javax.swing.SwingWorker;

/**
 * This interface can be implemented by trees that can have their state saved.  It is not
 * required for a tree to implement this interface in order for it's state to be saveable.
 * However, it is recommended as it makes the process of restoring state more efficient.
 * 
 * @author Scott C Arnold
 */
public interface TreeStateSaveableTree {

	public SwingWorker<?,?> expandPathForId(Object id);
}

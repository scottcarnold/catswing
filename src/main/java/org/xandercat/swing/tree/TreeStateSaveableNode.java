package org.xandercat.swing.tree;

import java.io.Serializable;

/**
 * This interface must be implemented by the nodes of a tree that can have 
 * it's state saved.
 * 
 * @author Scott C Arnold
 */
public interface TreeStateSaveableNode {

	public Serializable getUniqueId();
}

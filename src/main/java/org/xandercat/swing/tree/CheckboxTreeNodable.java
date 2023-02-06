package org.xandercat.swing.tree;

import javax.swing.Icon;

/**
 * CheckboxTreeNodable can be implemented by mutable tree node that wishes to represent itself
 * using a checkbox tree.  This is represented using an interface so that that such nodes can
 * extend other types of nodes.
 * 
 * @author Scott C Arnold
 */
public interface CheckboxTreeNodable {

	public void setSelected(boolean selected);
	
	public boolean isSelected();
	
	public int getSelectedChildCount();
	
	public int getSelectedDescendantsCount();
	
	public String getText();
	
	public Icon getIcon();
}

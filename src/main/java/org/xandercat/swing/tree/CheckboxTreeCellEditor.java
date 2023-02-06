package org.xandercat.swing.tree;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

/**
 * Tree cell editor for checkbox trees.  This renderer acts as a decorator
 * for another renderer, adding checkbox handling.
 * 
 * The renderer used in construction should be a new instance of the same type 
 * of renderer used in the CheckboxTreeCellRenderer.  Tree nodes are expected to 
 * implement CheckboxTreeNodable.  
 * 
 * @author Scott Arnold
 */
public class CheckboxTreeCellEditor extends AbstractCellEditor implements TreeCellEditor, ItemListener {

	private static final long serialVersionUID = 2010081201L;
	
	private CheckboxTreeCellRenderer renderer;
	private CheckboxTreeNodable node;
	private Component renderedComponent;
	
	public CheckboxTreeCellEditor(TreeCellRenderer renderer) {
		this.renderer = new CheckboxTreeCellRenderer(renderer);
		this.renderer.multiCheckBox.addItemListener(this);
	}
	
	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) {
		this.node = (CheckboxTreeNodable) value;
		this.renderedComponent = renderer.getTreeCellRendererComponent(
				tree, value, isSelected, expanded, leaf, row, true);
		return renderedComponent;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		if (anEvent instanceof MouseEvent && anEvent.getSource() instanceof JTree) {
			// allow selection when clicking on label or icon but not when clicking on checkbox
			MouseEvent event = (MouseEvent) anEvent;
			return ((event.getX() - renderedComponent.getX()) > renderer.multiCheckBox.getWidth());
		}
		return true;
	}
	
	@Override
	public Object getCellEditorValue() {
		return node;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		node.setSelected(renderer.multiCheckBox.isSelected());	
	}
}

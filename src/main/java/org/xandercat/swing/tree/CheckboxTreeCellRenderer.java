package org.xandercat.swing.tree;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.xandercat.swing.component.MultiCheckBox;

/**
 * Tree cell renderer for checkbox trees.  This renderer acts as a decorator
 * for another renderer, adding checkbox handling.
 * 
 * Tree nodes are expected to implement CheckboxTreeNodable.
 *   
 * @author Scott Arnold
 */
public class CheckboxTreeCellRenderer implements TreeCellRenderer {

	protected JPanel panel;
	protected MultiCheckBox multiCheckBox;
	protected TreeCellRenderer renderer;
	protected Component rendererComponent;
	
	public CheckboxTreeCellRenderer(TreeCellRenderer renderer) {
		this.renderer = renderer;
		this.multiCheckBox = new MultiCheckBox();
		this.multiCheckBox.setContentAreaFilled(false);
		this.panel = new JPanel();
		this.panel.setOpaque(false);
		this.panel.setBackground(new Color(0, 0, 0, 0));
		this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.X_AXIS));
		this.panel.add(this.multiCheckBox);
		this.panel.add(Box.createHorizontalStrut(4));
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (rendererComponent != null) {
			this.panel.remove(rendererComponent);
		}
		rendererComponent = renderer.getTreeCellRendererComponent(
				tree, value, selected, expanded, leaf, row, hasFocus);
		this.panel.add(rendererComponent);
		CheckboxTreeNodable node = (CheckboxTreeNodable) value;
		if (node.isSelected()) {
			this.multiCheckBox.setSelected(MultiCheckBox.SELECTED);
		} else if (node.getSelectedDescendantsCount() > 0) {
			this.multiCheckBox.setSelected(MultiCheckBox.PARTIAL_SELECTION);
		} else {
			this.multiCheckBox.setSelected(MultiCheckBox.UNSELECTED);
		}
		this.panel.revalidate();
		this.panel.repaint();
		return panel;
	}

}

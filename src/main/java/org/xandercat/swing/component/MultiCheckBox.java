package org.xandercat.swing.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JCheckBox;

/**
 * MultiCheckBox is a 3-state checkbox with possible values of SELECTED, UNSELECTED, and 
 * PARTIAL_SELECTION.  The boolean selected value of the underlying JCheckBox is true only 
 * when the state is set to SELECTED.
 * 
 * This checkbox is designed such that the PARTIAL_SELECTION state can only be set programmatically.
 * If the user clicks on a multi-checkbox when it is in the PARTIAL_SELECTION state, it transitions
 * to the selected state.
 * 
 * @author Scott C Arnold
 */
public class MultiCheckBox extends JCheckBox {

	private static final long serialVersionUID = 2009021801L;
	
	public static State SELECTED = State.SELECTED;
	public static State UNSELECTED = State.UNSELECTED;
	public static State PARTIAL_SELECTION = State.PARTIAL_SELECTION;
	
	public static enum State {
		SELECTED, UNSELECTED, PARTIAL_SELECTION;
	}

	private State selected = UNSELECTED;
	private boolean haltItemStateChanged = false;
	
	public State getSelected() {
		return selected;
	}
	
	public void setSelected(State selected) {
		if (selected == this.selected) {
			return;
		}
		haltItemStateChanged = true;
		super.setSelected(selected == SELECTED);
		haltItemStateChanged = false;
		this.selected = selected;
		int stateChange = isSelected()? ItemEvent.SELECTED : ItemEvent.DESELECTED;
		super.fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, stateChange));
		repaint();
	}
	
	public boolean isFullySelected() {
		return selected == SELECTED;
	}
	
	public boolean isPartiallySelected() {
		return selected == PARTIAL_SELECTION;
	}
	
	public boolean isUnselected() {
		return selected == UNSELECTED;
	}
	
	@Override
	public void setSelected(boolean selected) {
		if (selected) {
			setSelected(SELECTED);
		} else {
			setSelected(UNSELECTED);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (selected == PARTIAL_SELECTION) {
			Graphics2D g2d = (Graphics2D) g;
			final double w = 10;
			final double h = 4;
			final Color c = new Color(255, 255, 100);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			RoundRectangle2D rectangle = new RoundRectangle2D.Double(
					getWidth()/2d - w/2d, getHeight()/2d - h/2d, w, h, 5, 2);
			g2d.setColor(c);
			g2d.fill(rectangle);
			g2d.setColor(Color.BLACK);
			g2d.draw(rectangle);
		}
	}

	@Override
	protected void fireItemStateChanged(ItemEvent event) {
		if (haltItemStateChanged) {
			return;
		}
		// catch boolean selected value changing and update State selected value
		if (super.isSelected()) {
			selected = SELECTED;
		} else {
			selected = UNSELECTED;
		}
		super.fireItemStateChanged(event);
	}	
}

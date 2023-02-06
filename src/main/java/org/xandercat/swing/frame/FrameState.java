package org.xandercat.swing.frame;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FrameState saves the size, location, and state of a Frame in a serializable form.
 * 
 * @author Scott C Arnold
 */
public class FrameState implements Serializable {

	private static final long serialVersionUID = 2009080601L;
	private static final Logger log = LogManager.getLogger(FrameState.class);
	
	private Point location;
	private Dimension size;
	private Integer state;
	
	public FrameState() {
	}
	
	public FrameState(Frame frame) {
		store(frame);
	}
	
	public void applyTo(Frame frame) {
		if (location != null) {
			frame.setLocation(location);
		}
		if (size != null) {
			frame.setSize(size);
		}
		if (state != null && state.intValue() != Frame.ICONIFIED) {
			frame.setExtendedState(state.intValue());
		}
	}
	
	public void store(Frame frame) {
		int state = frame.getExtendedState();
		this.state = Integer.valueOf(state);
		if (state == Frame.MAXIMIZED_BOTH || state == Frame.MAXIMIZED_HORIZ || state == Frame.MAXIMIZED_VERT) {
			this.location = null;
			this.size = null;
		} else {
			this.location = frame.getLocation();
			this.size = frame.getSize();
		}
		log.debug("Frame state stored: " + toString());
	}
	
	public boolean isEmpty() {
		return state == null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("FrameState[state=");
		sb.append((state == null)? "null,size=" : state.toString() + ",size=");
		sb.append((size == null)? "null,location=" : size.toString() + ",location=");
		sb.append((location == null)? "null" : location.toString());
		sb.append("]");
		return sb.toString();
	}
}

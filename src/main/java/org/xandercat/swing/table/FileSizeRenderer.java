package org.xandercat.swing.table;

import javax.swing.table.DefaultTableCellRenderer;

import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.util.FileUtil;

/**
 * FileSizeRenderer is for rendering the size (length) of a file in a table cell.  The rendered
 * value can include the computed size, the binary prefix unit, or both.  By default, maximum
 * fraction digits is set to 0.
 * 
 * @author Scott C Arnold
 */
public class FileSizeRenderer extends DefaultTableCellRenderer {

	public static enum Render {
		ALL, VALUE_ONLY, BINARY_PREFIX_ONLY;
	}
	
	private static final long serialVersionUID = 2009022101L;
	
	private Render render = Render.ALL;
	private BinaryPrefix maxBinaryPrefix = BinaryPrefix.KiB;
	private boolean showZero;
	private Integer maxFractionDigits;
	
	public FileSizeRenderer() {
		super();
	}
	
	public FileSizeRenderer(int horizontalAlignment) {
		this();
		setHorizontalAlignment(horizontalAlignment);
	}
	
	public FileSizeRenderer(Render render, int horizontalAlignment) {
		this(horizontalAlignment);
		this.render = render;
	}
	
	public FileSizeRenderer(int horizontalAlignment, BinaryPrefix maxBinaryPrefix) {
		this(horizontalAlignment);
		setMaxBinaryPrefix(maxBinaryPrefix);
	}
	
	public void setMaxBinaryPrefix(BinaryPrefix maxBinaryPrefix) {
		this.maxBinaryPrefix = maxBinaryPrefix;
	}
	
	public void setRender(Render render) {
		this.render = render;
	}
	
	public void setShowZero(boolean showZero) {
		this.showZero = showZero;
	}
	
	public void setMaxFractionDigits(Integer maxFractionDigits) {
		this.maxFractionDigits = maxFractionDigits;
	}
	
	@Override
	protected void setValue(Object value) {
		if (value == null || (((Long) value).longValue() == 0 && !showZero)) {
			setText("");
		} else {
			switch(render) {
			case ALL: 
				setText(FileUtil.formatFileSize(((Long) value).longValue(), 
						maxBinaryPrefix, null, maxFractionDigits));
				break;
			case VALUE_ONLY:
				setText(FileUtil.formatFileSizeValue(((Long) value).longValue(), 
						maxBinaryPrefix, null, maxFractionDigits));
				break;
			case BINARY_PREFIX_ONLY:
				setText(FileUtil.formatFileSizePrefix(((Long) value).longValue(), maxBinaryPrefix));
				break;			
			}
		}
	}	
}

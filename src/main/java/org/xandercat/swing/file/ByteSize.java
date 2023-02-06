package org.xandercat.swing.file;

import java.io.Serializable;

import org.xandercat.swing.util.FileUtil;

/**
 * Class for storing a byte size along with a preferred binary unit.
 * 
 * @author Scott Arnold
 */
public class ByteSize implements Serializable {

	private static final long serialVersionUID = 2010092501L;
	
	private double value;
	private BinaryPrefix unit;
	private long bytes;
	
	public ByteSize(long bytes) {
		this.bytes = bytes;
		this.value = bytes;
		this.unit = BinaryPrefix.bytes;
		setUnit(FileUtil.getFileSizePrefix(bytes, BinaryPrefix.TiB));
	}
	
	public ByteSize(double value, BinaryPrefix unit) {
		this.unit = unit;
		this.value = value;
		this.bytes = Math.round(value * unit.getByteMultiplier());
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
		this.bytes = Math.round(value * unit.getByteMultiplier());
	}
	
	public BinaryPrefix getUnit() {
		return unit;
	}
	
	public void setUnit(BinaryPrefix unit) {
		this.value = FileUtil.convertFileSize(this.value, this.unit, unit);
		this.unit = unit;
	}
	
	public long getBytes() {
		return bytes;
	}
	
	public void setBytes(long bytes) {
		this.bytes = bytes;
		this.value = (double) bytes / (double) unit.getByteMultiplier();
	}
	
	public String toString() {
		return FileUtil.formatFileSize(bytes, unit);
	}
}

package org.xandercat.swing.file;

public enum BinaryPrefix {
	
	bytes(1L), KiB(1024L), MiB(1024L*1024), GiB(1024L*1024*1024), TiB(1024L*1024*1024*1024);
	
	private long byteMultiplier;
	
	private BinaryPrefix(long byteMultiplier) {
		this.byteMultiplier = byteMultiplier;
	}
	
	public long getByteMultiplier() {
		return byteMultiplier;
	}
}

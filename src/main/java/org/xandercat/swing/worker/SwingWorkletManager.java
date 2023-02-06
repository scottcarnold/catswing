package org.xandercat.swing.worker;

public interface SwingWorkletManager<V> {

	public boolean isCancelled();
	
	public void workletPublish(V v);
	
	public void workletProgress(long progress, long progressMaximum);

}

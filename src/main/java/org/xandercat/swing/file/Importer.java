package org.xandercat.swing.file;

import java.util.Set;

public interface Importer<T> {
	
	public Set<Class<?>> getFromObjectClasses();
	
	public String getImportConfirmationMessage(Object o);
	
	public T importObject(Object fromObject);
	
}

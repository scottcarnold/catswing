package org.xandercat.swing.util;

import java.io.InputStream;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resource serves as a holder for a resource path within the application (for images or other files).
 * Resource will evaluate as equal to another Resource whenever the paths ultimately point to the
 * same file.
 * 
 * This is useful, for example, as a key type in a resource cache, such that when more than one class 
 * in more than one package request the same resource using different resource paths, they are seen
 * as the same resource.
 * 
 * @author Scott C Arnold
 */
public class Resource {

	private static final Logger log = LogManager.getLogger(Resource.class);
	
	private String absolutePath;
	private String idString;
	
	/**
	 * Create a new Resource using a relative path to the resource, with the path being relative 
	 * to the given relative class.  In other words, the resource that would be obtained by 
	 * calling relativeClass.getClass().getResource(relativePath).
	 * 
	 * @param relativePath		relative path to the resource
	 * @param relativeClass		class path is relative to
	 */
	public Resource(String relativePath, Class<?> relativeClass) {
		if (relativePath.startsWith("/")) {
			this.absolutePath = relativePath;
			log.warn("Absolute path passed into relative path constructor.");
		} else {
			Package pkg = relativeClass.getPackage();
			if (pkg == null) {
				this.absolutePath = "/" + relativePath;
			} else {
				this.absolutePath = "/" + pkg.getName().replaceAll("\\.", "/") + "/" + relativePath;
			}
		}
	}
	
	/**
	 * Create a new Resource using an absolute path to the resource.
	 * 
	 * @param absolutePath		absolute path to the resource
	 */
	public Resource(String absolutePath) {
		if (!absolutePath.startsWith("/")) {
			throw new IllegalArgumentException("Path is not absolute unless it begins with a backslash.");
		}
		this.absolutePath = absolutePath;
	}
	
	public URL getResource() {
		return getClass().getResource(this.absolutePath);
	}
	
	public InputStream getResourceAsStream() {
		return getClass().getResourceAsStream(this.absolutePath);
	}
	
	public String getAbsoluteResourcePath() {
		return absolutePath;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Resource) {
			return toString().equals(obj.toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		if (this.idString == null) {
			URL resource = getResource();
			if (resource == null) {
				this.idString = absolutePath;
			} else {
				this.idString = resource.toString();
			}
		}
		return this.idString;
	}
}

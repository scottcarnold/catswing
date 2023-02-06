package org.xandercat.swing.file.icon;

import java.io.InputStream;
import java.net.URL;

import org.xandercat.swing.util.Resource;

/**
 * FileIconOverlayResource is a fake Resource for icons generated by overlaying one icon image on top
 * of another.  Since there is no real resource (as the icon was generated on the fly) the getResource
 * and getResourceAsStream methods will return null.  The purpose of this class is to serve as a key
 * value for generated icons in the FileIconCache.
 * 
 * @author Scott C Arnold
 */
public class FileIconOverlayResource extends Resource {

	private String idString;
	
	public FileIconOverlayResource(Resource baseIconResource, FileIconOverlayType overlayType) {
		super(baseIconResource.getAbsoluteResourcePath());
		this.idString = baseIconResource.toString() + ":" + overlayType.toString();
	}
	
	@Override
	public URL getResource() {
		return null;
	}

	@Override
	public InputStream getResourceAsStream() {
		return null;
	}

	@Override
	public String toString() {
		return idString;
	}

}
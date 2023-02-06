package org.xandercat.swing.file.icon;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.cache.SoftReferenceCache;
import org.xandercat.swing.util.Resource;


/**
 * FileIconCache caches image icons that represent files and directories using soft references.
 * The icons to use are defined by a FileIconSet.
 * 
 * @author Scott C Arnold
 */
public class FileIconCache extends SoftReferenceCache<Resource, ImageIcon> {

	private static final Logger log = LogManager.getLogger(FileIconCache.class);
	
	private FileIconSet fileIconSet;
	
	public FileIconCache(FileIconSet fileIconSet) {
		this.fileIconSet = fileIconSet;
	}
	
	public ImageIcon get(File file) {		
		return get(fileIconSet.getResource(file));
	}
	
	public ImageIcon getFolderOpenIcon() {
		return get(fileIconSet.getFolderOpenResource());
	}
	
	private ImageIcon get(Resource baseResource, FileIconOverlayType overlayType) {
		FileIconOverlayResource resource = new FileIconOverlayResource(baseResource, overlayType);
		ImageIcon icon = peek(resource);
		if (icon == null) {
			// create the icon and store in the cache
			ImageIcon baseIcon = get(baseResource);
			ImageIcon overlayIcon = get(fileIconSet.getOverlayResource(overlayType));
			if (baseIcon == null) {
				return overlayIcon;
			}
			BufferedImage workImage = new BufferedImage(baseIcon.getIconWidth(), baseIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = workImage.createGraphics();
			baseIcon.paintIcon(null, g2d, 0, 0);
			overlayIcon.paintIcon(null, g2d, 
					baseIcon.getIconWidth() - overlayIcon.getIconWidth(), 
					baseIcon.getIconHeight() - overlayIcon.getIconHeight());
			g2d.dispose();
			icon = new ImageIcon(workImage);
			log.debug("Caching icon for " + resource.toString());
			set(resource, icon);
		}
		return icon;		
	}
	
	public ImageIcon getFolderOpenIcon(FileIconOverlayType overlayType) {
		return get(fileIconSet.getFolderOpenResource(), overlayType);
	}
	
	public ImageIcon get(File file, FileIconOverlayType overlayType) {
		return get(fileIconSet.getResource(file), overlayType);
	}

	@Override
	protected ImageIcon loadValue(Resource key) {
		URL url = key.getResource();
		if (url == null) {
			log.warn("Resource not available: " + key.getClass().getName() + "->" + key.getAbsoluteResourcePath());
			url = FileIconSet.DEFAULT_RESOURCE.getResource();
		}
		log.debug("Caching icon for " + key.toString());
		return new ImageIcon(url);
	}
}

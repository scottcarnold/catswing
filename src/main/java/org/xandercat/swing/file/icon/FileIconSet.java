package org.xandercat.swing.file.icon;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.util.Resource;

public class FileIconSet {

	public static final Resource DEFAULT_RESOURCE = new Resource("/icon/file/question-24x24.png");
	private static final Resource DEFAULT_WARNING_OVERLAY_RESOURCE = new Resource("/icon/file/Symbols-Warning-16x16.png");
	private static final Resource DEFAULT_ERROR_OVERLAY_RESOURCE = new Resource("/icon/file/Symbols-Forbidden-16x16.png");
	private static final Resource DEFAULT_LOADING_OVERLAY_RESOURCE = new Resource("/icon/file/help-16x16.png");
	
	private String name;
	private String resourcePathRoot;
	private Resource unknownFileResource = DEFAULT_RESOURCE;
	private Resource badFileResource = DEFAULT_RESOURCE;
	private Resource driveResource = DEFAULT_RESOURCE;
	private Resource folderResource = DEFAULT_RESOURCE;
	private Resource folderOpenResource = null;
	
	private Map<String,Resource> extensionMap = new HashMap<String,Resource>();
	private Map<FileIconOverlayType,Resource> overlayMap = new HashMap<FileIconOverlayType,Resource>();
	
	public FileIconSet(String name, String resourcePathRoot) {
		this.name = name;
		this.resourcePathRoot = resourcePathRoot;
		this.overlayMap.put(FileIconOverlayType.WARNING, DEFAULT_WARNING_OVERLAY_RESOURCE);
		this.overlayMap.put(FileIconOverlayType.ERROR, DEFAULT_ERROR_OVERLAY_RESOURCE);
		this.overlayMap.put(FileIconOverlayType.LOADING, DEFAULT_LOADING_OVERLAY_RESOURCE);
	}
	
	public String getName() {
		return name;
	}
	
	public void setUnknownFileImage(String imageFilename) {
		this.unknownFileResource = new Resource(resourcePathRoot + imageFilename);
	}
	
	public void setBadFileImage(String imageFilename) {
		this.badFileResource = new Resource(resourcePathRoot + imageFilename);
	}

	public void setDriveImage(String imageFilename) {
		this.driveResource = new Resource(resourcePathRoot + imageFilename);
	}

	public void setFolderImage(String imageFilename) {
		this.folderResource = new Resource(resourcePathRoot + imageFilename);
	}

	public void setFolderOpenImage(String imageFilename) {
		this.folderOpenResource = new Resource(resourcePathRoot + imageFilename);
	}
	
	public Resource getFolderOpenResource() {
		return (this.folderOpenResource == null)? this.folderResource : this.folderOpenResource;
	}
	
	public Resource getFolderResource() {
		return this.folderResource;
	}
	
	public Resource getDriveResource() {
		return this.driveResource;
	}
	
	public void setOverlayImage(FileIconOverlayType overlayType, String imageFilename) {
		this.overlayMap.put(overlayType, new Resource(resourcePathRoot + imageFilename));
	}
	
	public void addExtensionImage(String imageFilename, String... extensions) {
		Resource resource = new Resource(resourcePathRoot + imageFilename);
		Collection<Resource> existingResources = extensionMap.values();
		// use the already existing Resource object, if there is one
		if (existingResources.contains(resource)) {
			// can't directly get it, just have to iterate the collection
			for (Resource existingResource : existingResources) {
				if (resource.equals(existingResource)) {
					resource = existingResource;
					break;
				}
			}
		}
		for (String extension : extensions) {
			extensionMap.put(extension.toLowerCase(), resource);
		}
	}
	
	public Resource getOverlayResource(FileIconOverlayType overlayType) {
		return this.overlayMap.get(overlayType);
	}
	
	public Resource getResource(File file) {
		if (file == null) {
			return DEFAULT_RESOURCE;
		} else {
			Boolean isDirectory = FileUtil.isDirectory(file);
			if (isDirectory == null) {
				return badFileResource;	
			} else if (isDirectory.booleanValue()) {
				if (Boolean.TRUE.equals(FileUtil.isDirectoryRootPath(file))) {
					return driveResource;
				} else {
					return folderResource;
				}
			} else {
				Resource resource = extensionMap.get(FileUtil.getExtensionLowerCase(file));
				if (resource == null) {
					return unknownFileResource;
				} else {
					return resource;
				}
			}	
		}
	}
}

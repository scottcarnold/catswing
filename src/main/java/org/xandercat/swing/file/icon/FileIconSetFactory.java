package org.xandercat.swing.file.icon;

/**
 * FileIconSetFactory builds file icon sets.
 * 
 * @author Scott C Arnold
 */
public class FileIconSetFactory {

	public static final Name SQUID = Name.SQUID;
	public static final Name GLAZE = Name.GLAZE;
	
	private static enum Name {
		GLAZE, SQUID;
	}
	
	public static FileIconSet buildIconSet(Name iconSetName) {
		switch (iconSetName) {
		case GLAZE:
			return buildGlaze();
		case SQUID:
			return buildSquid();
		default:
			throw new IllegalArgumentException("Icon set name " + iconSetName.toString() + " is invalid.");
		}
	}
	
	/**
	 * Build Squid icon set.  This is an outdated mish mash of icons without background transparency.
	 * 
	 * @return		Squid icon set
	 */
	private static FileIconSet buildSquid() {
		FileIconSet squid = new FileIconSet("Squid", "/icon/file/squid/");
		squid.setBadFileImage("invalid.jpg");
		squid.setDriveImage("drive.jpg");
		squid.setFolderImage("folder-blue.jpg");
		squid.setUnknownFileImage("file.jpg");
		squid.addExtensionImage("image.jpg", "jpg", "jpeg", "gif", "png", "bmp", "tiff");
		squid.addExtensionImage("video.jpg", "wmv", "avi", "mpg", "mpeg");
		squid.addExtensionImage("sound.jpg", "mp3", "mp4", "m4p", "m4a", "mid", "midi", "wav", "mod", "xm", "it", "s3m");
		squid.addExtensionImage("pdf.jpg", "pdf");
		squid.addExtensionImage("html.jpg", "html", "htm");
		squid.addExtensionImage("txt.jpg", "txt");
		squid.addExtensionImage("java.jpg", "java");
		squid.addExtensionImage("shell.jpg", "bat", "sh", "cmd");
		squid.addExtensionImage("compress.jpg", "zip", "tar", "gz", "jar", "war", "ear");
		squid.addExtensionImage("spread-sheet.jpg", "xls");
		squid.addExtensionImage("doc.jpg", "doc", "docx");
		return squid;
	}
	
	/**
	 * Build Glaze icon set.  Partial set of Glaze from http://www.notmart.org, available
	 * under GNU Lesser General Public License.
	 * 
	 * @return		Glaze icon set
	 */
	private static FileIconSet buildGlaze() {
		FileIconSet glaze = new FileIconSet("Glaze", "/icon/file/glaze/");
		glaze.setBadFileImage("empty-24x24.png");
		glaze.setDriveImage("hdd-unmount-24x24.png");
		glaze.setFolderImage("folder-blue-24x24.png");
		glaze.setFolderOpenImage("folder-blue-open-24x24.png");
		glaze.setUnknownFileImage("empty-24x24.png");
		glaze.addExtensionImage("image-24x24.png", "jpg", "jpeg", "gif", "png", "bmp", "tiff");
		glaze.addExtensionImage("video-24x24.png", "wmv", "avi", "mpg", "mpeg");
		glaze.addExtensionImage("quicktime-24x24.png", "mov");
		glaze.addExtensionImage("sound-24x24.png", "mp3", "mp4", "m4p", "m4a", "mid", "midi", "wav", "mod", "xm", "it", "s3m");
		glaze.addExtensionImage("pdf-24x24.png", "pdf");
		glaze.addExtensionImage("html-24x24.png", "html", "htm", "xhtml");
		glaze.addExtensionImage("ascii-24x24.png", "txt");
		glaze.addExtensionImage("source-java-24x24.png", "java");
		glaze.addExtensionImage("shellscript-24x24.png", "bat", "sh", "cmd");
		glaze.addExtensionImage("tar-24x24.png", "zip", "tar", "gz", "gzip", "jar", "war", "ear", "rar");
		glaze.addExtensionImage("spreadsheet-24x24.png", "xls");
		glaze.addExtensionImage("wordprocessing-24x24.png", "doc", "docx");
		glaze.addExtensionImage("log-24x24.png", "log");
		glaze.addExtensionImage("exec-24x24.png", "exe", "com");
		return glaze;
	}
}

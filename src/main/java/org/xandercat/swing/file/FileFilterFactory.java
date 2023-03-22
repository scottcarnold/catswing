package org.xandercat.swing.file;

import java.io.FileFilter;

public class FileFilterFactory {

	public static FileFilter hiddenFilesFilter() {
		return (file) -> { return file != null && !file.isHidden(); };
	}
	
	public static FileFilter macDsStoreFilter() {
		return (file) -> { return file != null && file.getName() != null && !file.getName().equals(".DS_Store"); };
	}
	
	public static FileFilter winDollarFilter() {
		return (file) -> { return file != null && file.getName() != null && !file.getName().toLowerCase().startsWith("$win") && !file.getName().toLowerCase().startsWith("$recycle"); };
	}
	
	public static FileFilter winPagefileFilter() {
		return (file) -> { return file != null && file.getName() != null && !file.getName().equals("pagefile.sys"); };
	}
	
	public static FileFilter winNTUserDatFilter() {
		return (file) -> { return file != null && file.getName() != null && !file.getName().toLowerCase().startsWith("ntuser.dat"); };
	}
	
	public static FileFilter filter(FilterType type) {
		switch (type) {
		case HIDDEN:
			return hiddenFilesFilter();
		case MAC_DS_STORE:
			return macDsStoreFilter();
		case WIN_DOLLAR:
			return winDollarFilter();
		case WIN_PAGEFILE:
			return winPagefileFilter();
		case WIN_NTUSER_DAT:
			return winNTUserDatFilter();
		}
		return null;
	}
}

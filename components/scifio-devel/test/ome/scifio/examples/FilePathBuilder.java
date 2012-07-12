package ome.scifio.examples;

import java.util.StringTokenizer;

/**
 * Finds the absolute path to the target file on this computer.
 * Requires that bio-formats is not a child of a "scifio" directory.
 * 
 * @author Mark Hiner
 */
public class FilePathBuilder {

	public String buildPath(String s) {
		String found = "/";
		String loc = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
		
		StringTokenizer stk = new StringTokenizer(loc, "/");
		String tkn = null;
		while(stk.hasMoreTokens() && !(tkn = stk.nextToken()).equals("scifio")) {
			found += tkn + "/";
		}
		found += tkn + "/test/ome/scifio/examples/" + s;
		
		return found;
	}
}

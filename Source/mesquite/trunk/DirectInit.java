/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.trunk;

import mesquite.Mesquite;
import mesquite.lib.*;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* called by Mesquite trunk early (after directories found, before modules loaded) for any init activities other than by modules */
public class DirectInit {
	
	public DirectInit(MesquiteTrunk mesquite){
		/* This will be used to load jar files at runtime*/
//		loadJars(mesquite.getRootPath(), mesquite.jarFilesLoaded);
//		loadJarsInDirectories(mesquite.getRootPath() + MesquiteFile.fileSeparator + "mesquite", mesquite.jarFilesLoaded);
	}
	static void loadJars(String directoryPath, StringBuffer buffer){
		try {
			String jarsPath = directoryPath;
			if (!jarsPath.endsWith(MesquiteFile.fileSeparator) && !jarsPath.endsWith("/"))
				jarsPath += MesquiteFile.fileSeparator;
			jarsPath += "jars";
			File f = new File(jarsPath);
			if (f.exists() && f.isDirectory()){
				String[] jars = f.list();
				if (jars.length>0)
					buffer.append("Incorporated from " + directoryPath +" ");
				for (int i = 0; i< jars.length; i++) {
					if (jars[i] != null && !jars[i].startsWith(".")){
						String path = jarsPath + "/" + jars[i];
						buffer.append(" " + jars[i]);
//					ClassPathHacker.addFile(path);
					loadJarModules(new File(path));
					System.out.println("Jar file added to classpath: " + path);
					}
				}
				buffer.append("\n\n");
			}
		}
		catch (Throwable t){
			System.out.println("DirectInit error " + t);
		}
	}

	public static void loadJarModules(File classFile) {
		try {
			JarFile classJar = new JarFile(classFile);
			ArrayList<String> mesquiteJarEntries = new ArrayList<>();
			for (Enumeration<JarEntry> entries = classJar.entries(); entries.hasMoreElements(); ) {
				JarEntry entry = entries.nextElement();
//				System.out.println("looking at entry " + entry.getName());
				if (entry.getName().contains("mesquite/")) {
					mesquiteJarEntries.add(entry.getName());
				}
			}
			for (String entry : mesquiteJarEntries) {
				Pattern modulePackagePattern = Pattern.compile("(mesquite/.+?)/(.*)");
				Matcher modulePackageMatcher = modulePackagePattern.matcher(entry);
				if (modulePackageMatcher.matches()) {
					String packageName = modulePackageMatcher.group(1).replace("/",".");
					if (!Mesquite.getMesquiteJarModules().containsKey(packageName)) {
						System.out.println("adding module " + packageName);
						Mesquite.getMesquiteJarModules().put(packageName, new ArrayList<String>());
					}
					if (!modulePackageMatcher.group(2).isEmpty()) {
						Mesquite.getMesquiteJarModules().get(packageName).add(modulePackageMatcher.group(0));
					}
				}
			}
//			System.out.println("hello");
//			Enumeration<JarEntry> e = classJar.entries();
//
//			URL[] urls = { new URL("jar:file:" + classFile.getAbsolutePath() +"!/") };
//			System.out.println("URL is " + urls[0].getPath());
//			URLClassLoader cl = URLClassLoader.newInstance(urls);
//
//			while (e.hasMoreElements()) {
//				JarEntry je = e.nextElement();
//				if(je.isDirectory() || !je.getName().endsWith(".class")){
//					continue;
//				}
//				// -6 because of .class
//				String className = je.getName().substring(0,je.getName().length()-6);
//				className = className.replace('/', '.');
//				System.out.println("className = " + className);
//				Class c = cl.loadClass(className);
//				System.out.println("class = " + c.getName());
//			}
		} catch (Exception e) {
			System.out.println("exception " + e.toString());
		}
	}
	public static void loadJarsInDirectories(String path, StringBuffer buffer){ //path has no slash at the end of it
		File f = new File(path);  //  
		if (!f.exists())
			return;
		else if (f.isDirectory()){  // is a directory; hence look inside at each item
			String[] fileList = f.list();
			for (int i=0; i<fileList.length; i++)
				if (fileList[i]!=null) {
					if (fileList[i].equalsIgnoreCase("jars"))
						loadJars(path, buffer);
					else 
						loadJarsInDirectories(path + MesquiteFile.fileSeparator + fileList[i], buffer);
				}
		}
}


}


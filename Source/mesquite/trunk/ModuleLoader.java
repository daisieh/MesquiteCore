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

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.lang.reflect.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mesquite.*;
import mesquite.lib.*;
import org.apache.commons.io.FilenameUtils;

/*======================================================================== */
public class ModuleLoader {
	Mesquite mesquite;
	int directoryTotal = 0;
	ListableVector configurations;
	boolean[] packagesFound;
	Parser parser;
	String configurationString;
	boolean verboseStartup = false;
	public ModuleLoader(Mesquite mesquite){
		this.mesquite = mesquite;
		parser = new Parser();
	}
	
MesquiteTimer loadTimer, fileTimer, listTimer,instantiateTime,compTime,mmiTime,otherTime, classTime;
	/*.................................................................................................................*/
	public void init(String configFile, ListableVector configurations, boolean useMinimal) {
	this.configurations = configurations;
		mesquite.mesquiteModulesInfoVector = new ModulesInfoVector();
		MesquiteModuleInfo mBI = new MesquiteModuleInfo(Mesquite.class, mesquite, new CommandChecker(), mesquite.getPath());
		mesquite.mesquiteModulesInfoVector.addElement(mBI, false);
		mesquite.mesquiteModulesInfoVector.recordDuty(mesquite);
		loadTimer= new MesquiteTimer();
		fileTimer= new MesquiteTimer();
		listTimer= new MesquiteTimer();
		instantiateTime= new MesquiteTimer();
		compTime= new MesquiteTimer();
		mmiTime= new MesquiteTimer();
		otherTime= new MesquiteTimer();
		classTime = new MesquiteTimer();
		verboseStartup = MesquiteFile.fileExists(MesquiteModule.getRootPath() + "verbose");
		if (MesquiteTrunk.isApplet()) {
			System.out.println("Error: attempt to use applet as application");
		}
		else {
			StringArray targetDirectories;

			
			MesquiteTimer countTimer = new MesquiteTimer();
			MesquiteTimer timer = new MesquiteTimer();
			/*--------------------------------------*/
			String[] configs = null;
			if (useMinimal){
				configs = new String[2];
				configs[0] = "Minimal";
				configs[1] = "mesquite.minimal";
			}
			else if (configFile != null && !("all".equalsIgnoreCase(configFile))){
				String configPath =configFile; //MesquiteModule.getRootPath() + "mesquite" + MesquiteFile.fileSeparator + "configs" + MesquiteFile.fileSeparator + configFile;
				if (MesquiteFile.fileExists(configPath)) {
					configs = MesquiteFile.getFileContentsAsStrings(configPath);
					mesquite.logln("Preferences request startup configuration file.  Mesquite will start with a selected set of modules.");
				}
			}

			StringBuffer buffer = new StringBuffer();
			DirectInit.loadJars(MesquiteModule.getRootPath(), buffer);
			mesquite.logln("found modules in jars: " + Mesquite.getMesquiteJarModules().keySet().toString());

			numDirectoriesCurrent = 0;
			if (!MesquiteInteger.isCombinable(mesquite.numDirectories))
				mesquite.numDirectories = 0;
			if (mesquite.numDirectories==0)
				directoryTotal = 10000;
			else
				directoryTotal =  mesquite.numDirectories;
			showMessage(true, "Looking for modules", directoryTotal, 0);
			mesquite.logln("Modules loading from directory " + MesquiteModule.getRootPath() + "mesquite/");
			targetDirectories = new StringArray(1);
			targetDirectories.setValue(0, "mesquite.minimal");

			StringBuffer report =  new StringBuffer(5000);
			MesquiteModule.mesquiteTrunk.logln(report.toString());
		//timer.start();
			if (configs!=null){
				targetDirectories = new StringArray(configs.length);
				packagesFound = new boolean[configs.length];
				for (int i=0; i<configs.length; i++) packagesFound[i]=false;
				mesquite.setConfiguration(parser.getFirstToken(configs[0]));
				mesquite.log("Using module set configuration file: Only modules in the module set \"" + mesquite.getConfiguration() + "\" are being loaded.  Packages loaded: ");
				configurationString = "Configuration: " + mesquite.getConfiguration();
				targetDirectories.setValue(0, "mesquite.minimal");
				for (int i=1; i< configs.length; i++) { //first String is title & explanation
					targetDirectories.setValue(i, configs[i]);
				}
				getModulesWrapper(Paths.get(MesquiteModule.getRootPath(), "mesquite"), 0, targetDirectories, true, false);  //do the directories in config
				int count = 0;
				String notFound = "";
				for (int i=0; i<configs.length; i++)
					if (!packagesFound[i] && !("mesquite.minimal".equalsIgnoreCase(targetDirectories.getValue(i)))){
						count++;
						notFound += "   " + targetDirectories.getValue(i) + StringUtil.lineEnding();
					}
				if (count>0)
					mesquite.alert("Some packages requested in the configuration file were not found.  These are:\n" + notFound);
				
				/* added by Paul Lewis */
				String classPathsFileMF = null; 
				if (MesquiteFile.fileExists(MesquiteModule.getRootPath() + "classpaths.xml")){
					classPathsFileMF = MesquiteFile.getFileContentsAsString(MesquiteModule.getRootPath() + "classpaths.xml");
					addModulesAtPaths(MesquiteModule.getRootPath(), classPathsFileMF);
				}
				
			}
			else {
				int numStandard = MesquiteTrunk.standardPackages.length; 
				int numStandardExtra = MesquiteTrunk.standardExtras.length; 
				packagesFound = new boolean[numStandard + numStandardExtra +6];
				targetDirectories = new StringArray(numStandard);
				for (int i=0; i<numStandard; i++)
					targetDirectories.setValue(i, "mesquite." + MesquiteTrunk.standardPackages[i]); //getting standard mesquite directories
				getModulesWrapper(Paths.get(MesquiteModule.getRootPath(), "mesquite"), 0, targetDirectories, true, true);  //first, only do the target directories
				targetDirectories = new StringArray(numStandardExtra);
				for (int i=0; i<numStandardExtra; i++)
					targetDirectories.setValue(i, "mesquite." + MesquiteTrunk.standardExtras[i]); //getting standard mesquite directories
				getModulesWrapper(Paths.get(MesquiteModule.getRootPath(), "mesquite"), 0, targetDirectories, true, true);  //first, only do the target directories

				targetDirectories = new StringArray(numStandard+ numStandardExtra+ 6);
				for (int i=0; i<numStandard; i++)
					targetDirectories.setValue(i, "mesquite." + MesquiteTrunk.standardPackages[i]); //getting standard mesquite directories
				for (int i=0; i<numStandardExtra; i++)
					targetDirectories.setValue(numStandard + i, "mesquite." + MesquiteTrunk.standardExtras[i]); //getting standard mesquite directories
				targetDirectories.setValue(numStandard + numStandardExtra, "mesquite.lib.duties");//TODO: avoid duties in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+1, "mesquite.lib");//TODO: avoid lib in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+2, "mesquite.documentation");//TODO: avoid documentation in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+3, "mesquite.docs");//TODO: avoid docs in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+4, "mesquite.macros");//TODO: avoid macros in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+5, "mesquite.configs");  //TODO: avoid configs in all contexts!
				getModulesWrapper(Paths.get(MesquiteModule.getRootPath(), "mesquite"), 0, targetDirectories, false, true); //next, add to the target directories and do everything but them
				getModulesWrapper(Paths.get(MesquiteModule.supportFilesDirectory.getAbsolutePath(),"classes", "mesquite"), 0, null, false, true);  //do the directories in config
				getModulesWrapper(Paths.get(MesquiteModule.getRootPath(),"additionalMesquiteModules","mesquite"), 0, null, false, true);  //do the directories in config

				String classPathsFileMF = null;
				if (MesquiteFile.fileExists(MesquiteModule.getRootPath() + "classpaths.xml")){
					classPathsFileMF = MesquiteFile.getFileContentsAsString(MesquiteModule.getRootPath() + "classpaths.xml");

					addModulesAtPaths(MesquiteModule.getRootPath(), classPathsFileMF);

				}
				if (MesquiteFile.fileExists(MesquiteModule.supportFilesDirectory  + MesquiteFile.fileSeparator  + "classpaths.xml")){
					classPathsFileMF = MesquiteFile.getFileContentsAsString(MesquiteModule.supportFilesDirectory +  MesquiteFile.fileSeparator  + "classpaths.xml");
					addModulesAtPaths(MesquiteModule.supportFilesDirectory + MesquiteFile.fileSeparator , classPathsFileMF);
				}
			}
			Mesquite.mesquiteModulesInfoVector.filterAllDutyDefaults();
			Mesquite.mesquiteModulesInfoVector.accumulateAllVersions();
			//timer.end();
			//CommandChecker checker = new CommandChecker();
			//checker.composeDocumentation();

			// load the macros for loaded modules:
			for (String modName : mesquite.getLoadedModules()) {
				mesquite.logln("is module " + modName + " in vector? " + Mesquite.mesquiteModulesInfoVector.nameAlreadyInList(modName));
				MesquiteModuleInfo mesquiteModuleInfo = Mesquite.mesquiteModulesInfoVector.findModule(modName);
				if (mesquiteModuleInfo != null) {
					loadMacros(mesquiteModuleInfo.getDirectoryPath(), false);
				}
			}

			// get special macros from user prefs directory
			loadMacros(MesquiteModule.prefsDirectory.getAbsolutePath(), true);
			loadConfigs(MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator +"configs", true);

			hideMessage(true);
			hideMessage(false);
			mesquite.numDirectories = numDirectoriesCurrent;
			mesquite.logln("");
		}
	}

	private void loadJarModule(String fileName) {
		if (fileName.startsWith("mesquite")) {
			if (fileName.endsWith(".class")) {
				loadModuleClass(fileName.replaceAll(File.separator, "."), null);
			}
		}
	}

	private void addModulesAtPaths(String relativeTo, String xmlPathsFileContents){
		if (xmlPathsFileContents == null)return;

		Parser parser = new Parser();
		parser.setString(xmlPathsFileContents);
		if (!parser.isXMLDocument(false))   // check if XML
			return;
		if (!parser.resetToMesquiteTagContents())   // check if has mesquite tag
			return;

		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		while (!StringUtil.blank(tagContent) || !StringUtil.blank(nextTag.getValue())) {
				if (!StringUtil.blank(tagContent) && "classpath".equalsIgnoreCase(nextTag.getValue())) {
//					try{
						String path = MesquiteFile.composePath(relativeTo, tagContent) ; //here you pass the ith thing in the list
						DirectInit.loadJarsInDirectories(path, mesquite.jarFilesLoaded);
//						ClassPathHacker.addFile(path);

						mesquite.logln("\n\nAdditional modules loaded from " + path);
						getModulesWrapper(Paths.get(path,"mesquite"), 0, null, false, true);  //do the directories in config
//					}
//					catch(IOException e){
//					}
				}
				tagContent = parser.getNextXMLTaggedContent(nextTag);
			}

			
	}

	
	
	
	int directoryNumber = 0;
	int modulesLoaded = 0;
	int numDirectoriesCurrent = 0;
	boolean avoidedDirectory(String name){ //added 11 Mar 02
		return ("macros".equalsIgnoreCase(name) || "documentation".equalsIgnoreCase(name) || "docs".equalsIgnoreCase(name)  || "lib".equalsIgnoreCase(name)  || "duties".equalsIgnoreCase(name)  || "configs".equalsIgnoreCase(name));
	}
	/*.................................................................................................................*/
	/** Put a message in the lower left corner of the About window; integers are for thermometer use; send unassingned for no thermometer  */
	public  void showMessage(boolean upper, String s){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				if (!mp.isVisible())
					mp.setVisible(true);
				if (s!=null && !s.equals(mp.getText()))
					mp.setText(s);
			}
			
		}
	}
	/*.................................................................................................................*/
	/** Put a message in the lower left corner of the About window; integers are for thermometer use; send unassingned for no thermometer  */
	public  void showMessage(boolean upper, String s, int total, int current){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				if (!mp.isVisible())
					mp.setVisible(true);
				if (s!=null && !s.equals(mp.getText()))
					mp.setText(s);
				mp.setTime(total, current);
			}
			
		}
	}
	/*.................................................................................................................*/
	/** Put a message in the lower left corner of the About window; integers are for thermometer use; send unassingned for no thermometer  */
	public  void showMessage(boolean upper, int current){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				mp.setTime(MesquiteLong.unassigned, current);
			}
			
		}
	}
	/*.................................................................................................................*/
	/** Remove message in the lower left corner of the About window  */
	public  void hideMessage (boolean upper){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				mp.setVisible(false);
			}
			
		}
	}
	String[] indent = {" ", "    ", "        ", "            ", "                ", "                   ", "                        "};
	private void getModulesFromJar(StringArray targetDirectories, Boolean targetOn) {
		// get a list of all packages in jar:
		try {
			// next, look for everything in the jar that corresponds to each package in targetDirectories:

			// if loadingAll is false, then we're just loading some specific config's subset of modules.

			// if targetOn is true, we're loading only the modules in the targetDirectories.
			// if targetOn is false, we're loading everything *but* the modules in the targetDirectories.
			ArrayList<String> modulePackagesToLoad = new ArrayList<>();
			if (targetOn) {
				for (int i = 0; i < targetDirectories.getSize(); i++) {
					String packageToLoad = targetDirectories.getValue(i);
					MesquiteMessage.println("loading package " + packageToLoad);
					if (Mesquite.getMesquiteJarModules().containsKey(packageToLoad)) {
						modulePackagesToLoad.add(packageToLoad);
					}
				}
			} else {
				for (String entry : Mesquite.getMesquiteJarModules().keySet()) {
					if (!targetDirectories.exists(entry)) {
						modulePackagesToLoad.add(entry);
					}
				}
			}

//			// load each modulePackage: classes, explanation, macros, configs
//			for (String modulePackage : modulePackagesToLoad) {
//				loadModulePackageFromJar(modulePackage);
//			}

		} catch (Exception e) {

		}
	}

	private void loadModulePackageFromJar(String modulePackage) {
		mesquite.log(" " + modulePackage.replace("mesquite.", ""));
		// load classes
		ArrayList<String> jarEntriesToLoad = Mesquite.getMesquiteJarModules().get(modulePackage);
		for (String module : jarEntriesToLoad) {
			loadJarModule(module);
		}
		// load explanation, if available:
		loadPackageExplanation(modulePackage, true);
	}

	private void getModulesWrapper(Path filePath, int level, StringArray targetDirectories, boolean targetOn, boolean loadingAll) {
		// if a package is in a known jar, use that one first:
		if (targetDirectories != null && targetOn) {
			for (int i=0; i<targetDirectories.getSize(); i++) {
				if (!mesquite.moduleIsLoaded(targetDirectories.getValue(i))) {
					loadModulePackageFromJar(targetDirectories.getValue(i));
					packagesFound[i] = true;
					mesquite.addLoadedModule(targetDirectories.getValue(i));
				}
			}
		}

		getModules("mesquite", filePath, level, targetDirectories, targetOn, loadingAll);
	}
	private void getModules(String packageName, Path filePath, int level, StringArray targetDirectories, boolean targetOn, boolean loadingAll){ //path has no slash at the end of it
		File f = filePath.toFile();
		String fName = FilenameUtils.getBaseName(f.getName());

		String filePathName = filePath.toString();
		level++;  //increment depth into directory structure
		if (verboseStartup) MesquiteMessage.println(">level " + level + " " + filePath.toString());

		//this is a class file, therefore try to load it
		if (loadMesquiteModuleClassFiles(f)) {
			return;
		}

		if (!f.exists())
			return;

		if (f.isFile()) {
			return;
		}
		
		if (verboseStartup) MesquiteMessage.println("    file  " + f + " exists? " + f.exists());

		// look for jars
		if (level == 2) {
			loadPackageExplanation(filePath.toString(), fName, true);
			showMessage(true, "Loading from directory: " + fName, directoryTotal, ++directoryNumber);
			mesquite.log(" " + fName);
			File jarsPath = filePath.resolve("jars").toFile();
			if (jarsPath.exists()){
				StringBuffer buffer =new StringBuffer();
				buffer.append("\n");
				DirectInit.loadJars(jarsPath.getPath(), buffer);
				mesquite.logln(buffer.toString());
			}
		} else {
			showMessage(true, ++directoryNumber);
		}

		numDirectoriesCurrent++;
		String[] modulesList = f.list();
		Arrays.sort(modulesList);

		if (verboseStartup) MesquiteMessage.println("    into directory with  " + modulesList.length + " items" );

		// look at modules inside
		for (String module : modulesList) {
			if (module != null && !avoidedDirectory(module)) {
				String pathFM = packageName + "."+module;
				
				boolean loadModule = false;
				if (targetDirectories != null) {
					int targetNumber = targetDirectories.indexOf(pathFM);
					if (!mesquite.moduleIsLoaded(module)) {
						if (targetNumber>=0 && targetOn) {
							loadModule = true;
						} else if (targetNumber < 0 && !targetOn) {
							loadModule = true;
						}
					} else {
						return;
					}
				}
				if (targetDirectories==null || loadModule) {
					getModules(pathFM, Paths.get(filePath.toString(), module), level, null, targetOn, loadingAll);
				}
				else if (level == 1) {
					String notDonePath = filePathName+ MesquiteFile.fileSeparator + module;
					File notDoneFile = new File(notDonePath);
					if (notDoneFile.exists() && notDoneFile.isDirectory() && !loadingAll) { //if loading all will catch later
						loadConfigs(notDonePath+ MesquiteFile.fileSeparator + "configs", false);
						//loadMacros(filePathName+ MesquiteFile.fileSeparator + module+ MesquiteFile.fileSeparator + "macros");
						loadPackageExplanation(notDonePath, module, false);
					}
				}
				else if (targetDirectories.indexOf(pathFM)<0 && targetOn){
					mesquite.logln("Not loading package \"" + pathFM + "\" because not included in current configuration list");
				}
			}
			else if ("configs".equalsIgnoreCase(module) && (!loadingAll || targetOn)){
				loadConfigs(filePath.resolve("configs").toString(), false);
			}
		}
	}

	private void loadMacrosFromJarResource(String macrosPath, boolean auto) {
		String modulePath = "";
		Pattern modulePackagePattern = Pattern.compile("(mesquite/.*)/macros/");
		Matcher modulePackageMatcher = modulePackagePattern.matcher(macrosPath);
		if (modulePackageMatcher.matches()) {
			modulePath = modulePackageMatcher.group(1).replace("/",".");
			ArrayList<String> bits = Mesquite.getMesquiteJarModules().get(modulePath);
			for (String bit : bits) {
				if (bit.startsWith(macrosPath) && !bit.equals(macrosPath)) {
					String macroName = bit.replace(macrosPath,"");
					loadMacroFile(macroName, bit, auto);
				}
			}
		}
	}
	private void loadMacroFile(String macroName, String fullPath, boolean auto) {
		String firstLine = MesquiteFile.getFileContentsAsString(fullPath);
		parser.getFirstToken(firstLine);  //"telling"
		String target  = parser.getNextToken();
		String name  = parser.getNextToken();
		String explanation  = parser.getNextToken();
		int preferredMenu  = MesquiteInteger.fromString(parser.getNextToken());
		if (explanation== null || ";".equals(explanation))
			explanation = "";
		if (name == null || ";".equals(name))
			name = macroName;
		MesquiteModuleInfo mmi = Mesquite.mesquiteModulesInfoVector.findModule(MesquiteModule.class, "#" + target);
		if (mmi !=null) {
			MesquiteMacro cfr = new MesquiteMacro(name, explanation, fullPath, mmi);
			cfr.setAutoSave(auto);
			if (MesquiteInteger.isCombinable(preferredMenu))
				cfr.setPreferredMenu(preferredMenu);
			mmi.addMacro(cfr);
		}
	}

	private void loadPackageExplanation(String path, String packageName, boolean loaded){
			MesquitePackageRecord pRec = new MesquitePackageRecord();
			pRec.setStored(0, packageName);
			pRec.setLoaded(loaded);
			mesquite.packages.addElement(pRec, false);
			String ePath = path + MesquiteFile.fileSeparator + "explanation.txt";
			if (MesquiteFile.fileExists(ePath)){
				String[] contents = MesquiteFile.getFileContentsAsStrings(ePath);
				if (contents!=null && contents.length>0){
					pRec.setStored(1,contents[0]);
					if (contents.length>1){
						String s = "";
						for (int i=1; i<contents.length; i++)
							s += contents[i] + "\n";
						pRec.setStored(2, s);
					}
				}
			}
							
	}

	private void loadPackageExplanation(String packageName, boolean loaded){
		MesquitePackageRecord pRec = new MesquitePackageRecord();
		pRec.setStored(0, packageName);
		pRec.setLoaded(loaded);
		mesquite.packages.addElement(pRec, false);
		String ePath = packageName.replace(".", "/") + File.separatorChar + "explanation.txt";
		InputStream in = Mesquite.getMesquiteClassLoader().getResourceAsStream(ePath);
		if (in != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			try {
				pRec.setStored(1, reader.readLine());

				String restOfExplanation = "";
				String line = reader.readLine();
				while (line != null) {
					restOfExplanation += line + "\n";
					line = reader.readLine();
				}
				if (!restOfExplanation.isEmpty()) {
					pRec.setStored(2, restOfExplanation);
				}
			} catch (IOException e) {

			}
		}

	}

	private void loadConfigs(String path, boolean userDefined){
		if (configurations == null)
			return;
		File configDir = new File(path);
		if (configDir.exists() && configDir.isDirectory()) {
					String[] configsList = configDir.list();
					for (int i=0; i<configsList.length; i++) {
						if (configsList[i]==null )
							;
						else {
							String cPath = path + MesquiteFile.fileSeparator + configsList[i];
							File cFile = new File(cPath);
							if (cFile.exists() && !cFile.isDirectory() && configsList[i].endsWith("config")) {
								String firstLine = MesquiteFile.getFileFirstContents(cPath);
								ConfigFileRecord cfr = new ConfigFileRecord(userDefined);
								cfr.cStored[0] = cPath;
								cfr.cStored[1] = parser.getFirstToken(firstLine);
								cfr.cStored[2]= parser.getNextToken();
								configurations.addElement(cfr, false);
							}
						}
					}
		}
	}
	private void loadMacros(String path, boolean auto) {
		mesquite.logln("loadMacros from " + path);
		File macrosDirectory = Paths.get(path,"macros").toFile();
		if (!macrosDirectory.exists()) {
			// look in parent?
			macrosDirectory = Paths.get(Paths.get(path).toFile().getParent(),"macros").toFile();
		}
		if (macrosDirectory.exists() && macrosDirectory.isDirectory() && macrosDirectory.list() != null) {
			mesquite.logln("loading macros from " + macrosDirectory.getAbsolutePath());
			for (String v : macrosDirectory.list()) {
				String fullPath = path + MesquiteFile.fileSeparator + v;
				File cFile = new File(fullPath);
				if (cFile.exists() && !cFile.isDirectory()) {
					String firstLine = MesquiteFile.getFileFirstContents(fullPath);
					parser.getFirstToken(firstLine);  //"telling"
					String target  = parser.getNextToken();
					String name  = parser.getNextToken();
					String explanation  = parser.getNextToken();
					int preferredMenu  = MesquiteInteger.fromString(parser.getNextToken());
					if (explanation== null || ";".equals(explanation))
						explanation = "";
					if (name == null || ";".equals(name))
						name = v;
					MesquiteModuleInfo mmi = Mesquite.mesquiteModulesInfoVector.findModule(MesquiteModule.class, "#" + target);
					if (mmi !=null) {
						MesquiteMacro cfr = new MesquiteMacro(name, explanation, fullPath, mmi);
						cfr.setAutoSave(auto);
						if (MesquiteInteger.isCombinable(preferredMenu))
								cfr.setPreferredMenu(preferredMenu);
						mmi.addMacro(cfr);
					}
				}
			}
		}
	}
	private void loadModuleClass(String rawPackageName, String directoryPath) {
		Matcher matcher = Pattern.compile(".*(mesquite\\..+\\.(.+?)\\.)(.+?)\\.class").matcher(rawPackageName);
		// Module is a class file within a directory of the same name:
		if (!matcher.matches() || !matcher.group(2).equals(matcher.group(3))) {
			return;
		}

		String className = matcher.group(2);
		String packageName = matcher.group(1).replaceAll(File.separator, ".") + className;


		try {
			//note: as of  21 april 2000 this simpler "Class.forName" was used instead of the more complex local ClassLoader
			Class c= Class.forName(packageName);
			if (c != null && !c.getName().equals("mesquite.Mesquite")) {
				URL classURL = c.getResource( c.getSimpleName() + ".class");
				if (classURL.toString().startsWith("jar:")) {
					// this is a jar: store the jar's location as directoryPath
					directoryPath = classURL.toString().replaceFirst("\\.jar!.+", ".jar");
					mesquite.logln("this is from a jar: " + directoryPath + ", " + c.getSimpleName());
				} else {
					Path path = Paths.get(classURL.getPath());
					directoryPath = path.getParent().toAbsolutePath().toString();
					mesquite.logln("this is from a file: " + directoryPath + ", " + c.getSimpleName());
				}
				MesquiteModule mb = mesquite.instantiateModule(c);
				if (mb!=null) {
					if (mb.isPrerelease() && mb.isSubstantive() && mb.loadModule()){
						MesquiteModule.mesquiteTrunk.substantivePrereleasesFound();
					}
					String message = checkModuleForCompatibility(c);
					if (message == null && mb.compatibleWithSystem() && mb.loadModule()) {
						MesquiteModuleInfo mBI = new MesquiteModuleInfo(c, mb, moduleChecker, directoryPath);
						if (!mb.getName().equals("Mesquite") && Mesquite.mesquiteModulesInfoVector.nameAlreadyInList(mb.getName()))
							MesquiteTrunk.mesquiteTrunk.alert("Two modules have the same name (" + mb.getName() + ").  This may make one of the modules unavailable for use. (Module class: " + mb.getClass().getName() +
									").\n\nThis problem can arise if a module has been moved, and you update your copy of Mesquite on a Windows machine by replacing folders without deleting the previous folder, or if you are programming and you haven't updated all projects.");
						Mesquite.mesquiteModulesInfoVector.addElement(mBI, false);
						Mesquite.mesquiteModulesInfoVector.recordDuty(mb);
						mBI.setAsDefault(Mesquite.mesquiteModulesInfoVector.isDefault(mb));
						MesquiteTrunk.mesquiteTrunk.addSplash(mBI);
						showMessage(false, configurationString);
						if (mb.getExpectedPath() !=null) {
							// first, look for the expected file. If it doesn't exist, look for the resource in jars.
							File file = new File(mb.getExpectedPath());
							if (!file.exists()) {
								if (Mesquite.getMesquiteClassLoader().getResource(mb.getExpectedPath()) == null) {
									MesquiteMessage.warnProgrammer("...\n**************\nThe module " + mb.getName() + " (" + mb.getClass().getName() + ") expects a file or directory at " + mb.getExpectedPath() + " but it was not found. \n**************\n ...");
								}
							}
						}
						modulesLoaded++;
						//mesquite.logln("Loading: " + mb.getName(), MesquiteLong.unassigned, MesquiteLong.unassigned);
					}
					else if (message !=null)
						MesquiteTrunk.mesquiteTrunk.alert("Incompatible module found: " + mb.getName() + ". The module may be out of date and no longer compatible with the current version of the Mesquite system.   Error message: " + message);
					//else
					//	MesquiteTrunk.mesquiteTrunk.alert("Incompatible module found: " + mb.getName() + ". The module may be out of date and no longer compatible with the current Java VM, the operating system. or the current version of the Mesquite system. ");
					EmployerEmployee.totalDisposed++;
					mb = null;
				}
				c = null;
			}
		}
		catch (NoClassDefFoundError e){
			mesquite.logln("\n\nNoClassDefFoundError while loading: " + packageName);
			MesquiteFile.throwableToLog(this, e);
			warnMissing(packageName, e);
		}
		catch (NoSuchMethodError e){
			mesquite.logln("\n\nNoSuchMethodError while loading: " + packageName);
			MesquiteFile.throwableToLog(this, e);
			warnIncompatible(packageName, e);
		}
		catch (AbstractMethodError e){
			mesquite.logln("\n\nAbstractMethodError while loading: " + packageName);
			MesquiteFile.throwableToLog(this, e);
			warnIncompatible(packageName, e);
		}
		catch (Exception e){
			mesquite.logln("\n\nException while loading: " + packageName + "   exception: " + e.getClass());
			MesquiteFile.throwableToLog(this, e);
		}
		catch (Error e){
			mesquite.logln("\n\nError while loading: " + packageName + "   error: " + e.getClass());
			MesquiteFile.throwableToLog(this, e);
			throw e;
		}
	}
	/*.................................................................................................................*/
	/** Returns whether module compatible with this version of Mesquite.*/
   	 private String checkModuleForCompatibility(Class original){
   	 	if (!MesquiteModule.checkMethodsAtStartup)
   	 		return null;
   	 	Class s = original;
   	 	while (s!=MesquiteModule.class && s!=Object.class) {
   	 		s = s.getSuperclass();
   	 		try {
   	 			String m = checkCompatibility(s, original);
   	 			if (m!=null)
   	 				return m;
   	 		}
   	 		catch (Exception e){
   	 			return "Exception (" + e + ") checking class " + original.getName() + " against superclass " + s.getName();
   	 		}
   	 		catch (Error e){
   	 			if (e instanceof ThreadDeath)
   	 				throw e;
   	 			else
   	 				return "Error (" + e + ") checking class " + original.getName() + " against superclass " + s.getName();
   	 		}
   	 	}
   	 	return null;
   	 	
   	 	//check all superclasses of this until arrive at MesquiteModule, for each checking that abstract methods of superclass are accurately represented in this
   	 }
   	 private String checkCompatibility(Class superClass, Class target){
   	 	//check that abstract methods of superclass are accurately represented in target
   	 	Method[] methods = superClass.getMethods();
   	 	if (methods==null)
   	 		return null;
   	 	for (int i=0; i<methods.length; i++) {
   	 		if (Modifier.isAbstract(methods[i].getModifiers())) {
   	 			//check to find if method matching signature found in target
   	 			try {
   	 				Method inTarget = target.getMethod(methods[i].getName(), methods[i].getParameterTypes());
   	 				if (inTarget==null){
   	 					return "Method " + methods[i].getName() + " which should occur in class " + target.getName() + " is absent ";
 	 				}
   	 				else if (Modifier.isAbstract(inTarget.getModifiers())){
   	 					return "Method " + methods[i].getName() + " which should occur in class " + target.getName() + " is abstract ";
 	 				}
   	 			}
	   	 		catch (Exception e){
   	 				return "Exception (" + e + ") checking method " + methods[i].getName() + " of superclass " + superClass.getName() + " against target class " + target.getName();
	   	 		}
	   	 		catch (Error e){
   	 				if (e instanceof ThreadDeath)
   	 					throw e;
   	 				else
   	 					return "Error (" + e + ") checking method " + methods[i].getName() + " of superclass " + superClass.getName() + " against target class " + target.getName();
	   	 		}
   	 			
   	 		}
   	 	}
   	 	return null;
   	 }
   	 static boolean warnedError = false;
   	 CommandChecker moduleChecker = new CommandChecker();
	/*.................................................................................................................*/
	private boolean loadMesquiteModuleClassFiles(File thisFile) {
		// if this file is a module, it will be a class contained in a directory of the same name:
		String directoryPath = FilenameUtils.getFullPath(thisFile.getAbsolutePath());

		Matcher matcher = Pattern.compile(".*(mesquite\\..+)\\.(.+?)\\.(.+?)\\.class").matcher(thisFile.getPath().replaceAll(File.separator, "."));
		// Module is a class file within a directory of the same name:
		if (!matcher.matches() || !matcher.group(2).equals(matcher.group(3))) {
			return false;
		}

		String packageName = matcher.group(1).replaceAll(File.separator, ".");
		if (mesquite.moduleIsLoaded(packageName)) {
			return true;
		} else {
			mesquite.addLoadedModule(packageName);
		}

		// if it's not a file, don't load:
		if (!thisFile.isFile()) {
			return false;
		}

		loadModuleClass(thisFile.toString().replaceAll(File.separator, "."), directoryPath);
		return true;
	}
	void warnIncompatible(String lastTried, Throwable e){
		if (!warnedError)
			mesquite.discreetAlert("Error while loading "  + lastTried + ".  This probably means that you have installed a package that is old or otherwise incompatible with this version of Mesquite.  Please ensure that any extra packages you have installed are up to date.  \n\nDetails: " +  e);
		warnedError = true;
	}
	void warnMissing(String lastTried, Throwable e){
		if (!warnedError)
			mesquite.discreetAlert("Error while loading "  + lastTried + ".    It appears that a component of Mesquite or a required library is missing.  We recommend that you install Mesquite again. \n\nDetails: " +  e);
		warnedError = true;
	}
}




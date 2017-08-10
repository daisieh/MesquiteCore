/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.tol.lib;

/*~~  */

import java.net.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import org.apache.http.client.utils.URIBuilder;
import org.dom4j.*;

public class TWProjectOpener  {

	public static final String GROUP_SEARCH_SRV_URL = "/onlinecontributors/app?service=external&page=xml/GroupSearchService&group=";
	public static final String TREE_STRUCTURE_SRV_URL = "/onlinecontributors/app?service=external&page=xml/TreeStructureService";
	private static URL taxonWorksURL;
	private String token = "";
	private int projectID = 1;
	
	/*.................................................................................................................*/
	public String getToolModule() {
		return "mesquite.tol.SearchToLTaxon.SearchToLTaxon";
	}
	/*.................................................................................................................*/
	public String getTreeDrawingModule() {
		return "mesquite.trees.SquareTree.SquareTree";
	}

	/*.................................................................................................................*/
	public String getSetToolScript() {
		return "setTool mesquite.tol.SearchToLTaxon.SearchToLTaxonToolExtra.goToToLTaxon";
	}
	
	/*.................................................................................................................*/
	public MesquiteProject establishProject(MesquiteModule ownerModule, String taxonName, String token, String twURL) {
		FileCoordinator fileCoord = ownerModule.getFileCoordinator();
		MesquiteFile thisFile = new MesquiteFile();		

		try {
			taxonWorksURL = new URL(twURL);
		} catch (MalformedURLException e) {
			MesquiteMessage.println("URL for TaxonWorks not valid");
		}
		// the tol web services requires us to look up the nodeId via the Group Search before getting the tree structure
		int nodeId = retrieveNodeIdFromGroupSearchResults(taxonName, ownerModule);
		
		// if a nodeId was not found, nodeId < 0 is true
		// TODO handle the case when we fail to retrieve the nodeId from the group search service 
		
		String treeServiceURL = taxonWorksURL + TREE_STRUCTURE_SRV_URL;
		treeServiceURL += "&node_id=" +  nodeId;
		MesquiteMessage.println("Request to the Tree of Life Web Project for the following URL:\n"+ treeServiceURL + "\n");
		
//		Element root = XMLUtil.getRootXMLElementFromURL(treeServiceURL);
//		// if call fails, the exception will likely be reported twice
//		if (root == null) {
//			ownerModule.discreetAlert( "Sorry, no tree was obtained from the database");
//			return null;
//		}
//
//		int numTaxa = ToLUtil.countTerminals(root, "  ");
//		if (numTaxa == 0) {
//			ownerModule.discreetAlert( "Sorry, no tree was obtained from the database");
//			return null;
//		}

		//looks as if tree was recovered properly; prepare project
		MesquiteProject p = fileCoord.initiateProject(thisFile.getFileName(), thisFile);
//		MesquiteFile sf = CommandRecord.getScriptingFileS();
//		if (MesquiteThread.isScripting())
//			CommandRecord.setScriptingFileS(thisFile);

//		//getting taxon names & building Taxa block
//		String[] names= new String[numTaxa];
//		boolean[] leaves = new boolean[numTaxa];
//		boolean[] hasChildren = new boolean[numTaxa];
//		ToLUtil.getTerminals(root, names, leaves, hasChildren, new MesquiteString(), new MesquiteInteger(0));
//		TaxaManager taxaTask = (TaxaManager)ownerModule.findElementManager(Taxa.class);
//		Taxa taxa = taxaTask.makeNewTaxa("Taxa from ToL", numTaxa, false);
//		for (int i = 0; i<numTaxa; i++){
//			Taxon t = taxa.getTaxon(i);
//			t.setName(names[i]);
//			taxa.setAssociatedObject(NameReference.getNameReference("ToLLeaves"), i, new MesquiteBoolean(leaves[i]));
//			taxa.setAssociatedObject(NameReference.getNameReference("ToLHasChildren"), i, new MesquiteBoolean(hasChildren[i]));
//		}
//		taxa.addToFile(thisFile, p, taxaTask);
//
//		//getting tree structure
//		MesquiteTree tree = new MesquiteTree(taxa);
//		ToLUtil.buildTree(true,root, tree, tree.getRoot(), names, new MesquiteInteger(0));
//		tree.setName("Tree for " + taxonName);
//		TreeVector trees = new TreeVector(taxa);
//		trees.addElement(tree, false);
//		trees.addToFile(thisFile,p,ownerModule.findElementManager(TreeVector.class));
//		trees.setName("Trees for " + taxonName);
//
//		//cleaning up and scripting the windows to show the tree
//		CommandRecord.setScriptingFileS(sf);

//		MesquiteModule treeWindowCoord = ownerModule.getFileCoordinator().findEmployeeWithName("#BasicTreeWindowCoord");
//		if (treeWindowCoord!=null){
//			String commands = "makeTreeWindow " + p.getTaxaReference(taxa) + "  #BasicTreeWindowMaker; tell It; ";
//			commands += "getEmployee #" + getToolModule() + "; tell It; enableTools; endTell;";
//			commands += "setTreeSource  #StoredTrees; tell It; setTaxa " + p.getTaxaReference(taxa) + " ;  setTreeBlock 1; endTell; ";
//			commands += "getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;";
//			commands += "tell It; suppress; setTreeDrawer  #" + getTreeDrawingModule()+"; tell It; orientRight; endTell; desuppress; endTell;";
//			commands += "getWindow; tell It; setActive; setSize 600 600; getToolPalette; tell It; " + getSetToolScript()+ "; endTell; endTell;";
//			commands += "  showWindow; endTell; ";
//			MesquiteInteger pos = new MesquiteInteger(0);
//			Puppeteer pup = new Puppeteer(ownerModule);
//			CommandRecord oldCR = MesquiteThread.getCurrentCommandRecord();
//			MesquiteThread.setCurrentCommandRecord(new CommandRecord(true));
//			pup.execute(treeWindowCoord, commands, pos, null, false);
//			MesquiteThread.setCurrentCommandRecord(oldCR);
//		}
		return p;
	}

	/*--------------------------*/
	private int retrieveNodeIdFromGroupSearchResults(String groupName, MesquiteModule ownerModule) {
		// GET http://127.0.0.1:3000/api/v1/taxon_names/autocomplete?term=Coleorrhyncha&token=Ad3Hx0c4oCMgS_GIBFO7ew&project_id=1
		String serviceURL = "";
		try {
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost(taxonWorksURL.toString())
					.setPath("/api/v1/taxon_names/autocomplete")
					.setParameter("term", groupName)
					.setParameter("btnG", "Google Search")
					.setParameter("aq", "f")
					.setParameter("oq", "")
					.build();

		} catch (Exception e) {

		}
		Element root = XMLUtil.getRootXMLElementFromURL(serviceURL);
		try {
			int count = Integer.parseInt(root.attributeValue("COUNT"));
			if (count == 1) {
				Element element = (Element)root.elements().get(0);
				return Integer.parseInt(element.attributeValue("ID"));
				//return Integer.parseInt(((Element)root.getChildren().get(0)).getAttributeValue("ID"));
			} else {
				return Integer.MIN_VALUE;
			}
		} catch (Exception e) {
			ownerModule.discreetAlert("Sorry, the Group ID Service appears to have failed");
			MesquiteMessage.println("Exception " + e);
			return Integer.MIN_VALUE;
		}
	}
}


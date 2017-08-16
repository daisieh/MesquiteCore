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

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TWProjectOpener  {

	private static URIBuilder taxonWorksURIBuilder;
	private String token = "";
	private int projectID = 1;
	
	/*.................................................................................................................*/
	public String getToolModule() {
		return "mesquite.tol.SearchToLTaxon.SearchToLTaxon";
	}
	/*.................................................................................................................*/
	public String getTreeDrawingModule() {
		return "mesquite.trees.BallsNSticks.BallsNSticks";
	}

	/*.................................................................................................................*/
	public String getSetToolScript() {
		return "setTool mesquite.tol.SearchToLTaxon.SearchToLTaxonToolExtra.goToToLTaxon";
	}
	
	/*.................................................................................................................*/
	public MesquiteProject establishProject(MesquiteModule ownerModule, String taxonName, String this_token, String twURL, int pageDepth) {
		FileCoordinator fileCoord = ownerModule.getFileCoordinator();
		MesquiteFile thisFile = new MesquiteFile();
		token = this_token;

		try {
			URL taxonWorksURL = new URL(twURL);
			int port = taxonWorksURL.getPort();
			if (port == -1) {
				port = taxonWorksURL.getDefaultPort();
			}
			taxonWorksURIBuilder = new URIBuilder()
					.setScheme(taxonWorksURL.getProtocol())
					.setHost(taxonWorksURL.getHost())
					.setPort(port)
					.setParameter("token", token)
					.setParameter("project_id", String.valueOf(projectID));

		} catch (Exception e) {
			MesquiteMessage.println("URL for TaxonWorks not valid");
		}

		// the tol web services requires us to look up the nodeId via the Group Search before getting the tree structure
		int nodeId = -1;

		try {
			nodeId = getNodeIDforTaxonName(taxonName, ownerModule);
		} catch (IOException e) {
			MesquiteMessage.println("no taxon named " + taxonName);
		}
		if (nodeId == -1) {
			MesquiteMessage.println("no taxon named " + taxonName);
		}

		TaxonNode namedNode = getTaxonNodeForNodeID(nodeId);
		if (namedNode == null) {
			MesquiteMessage.println("Couldn't find taxon in TaxonWorks");
			return null;
		}

		int numTaxa = populateChildNodes(namedNode, pageDepth, 0);
		MesquiteMessage.println(numTaxa + ":" + namedNode.toNewickString());


		//looks as if tree was recovered properly; prepare project
		MesquiteProject p = fileCoord.initiateProject(thisFile.getFileName(), thisFile);
//		MesquiteFile sf = CommandRecord.getScriptingFileS();
//		if (MesquiteThread.isScripting())
//			CommandRecord.setScriptingFileS(thisFile);

		//getting taxon names & building Taxa block
		String[] names= new String[numTaxa];
		getTerminals(namedNode, names, new MesquiteString(), new MesquiteInteger(0));

		TaxaManager taxaTask = (TaxaManager)ownerModule.findElementManager(Taxa.class);
		Taxa taxa = taxaTask.makeNewTaxa("Taxa from ToL", numTaxa, false);
		for (int i = 0; i<numTaxa; i++){
			Taxon t = taxa.getTaxon(i);
			t.setName(names[i]);
		}
		taxa.addToFile(thisFile, p, taxaTask);

		//getting tree structure
		MesquiteTree tree = buildTreeFromRoot(namedNode, taxa);
		tree.setName("Tree for " + taxonName);
		TreeVector trees = new TreeVector(taxa);
		trees.addElement(tree, false);
		trees.addToFile(thisFile,p,ownerModule.findElementManager(TreeVector.class));
		trees.setName("Trees for " + taxonName);
//
//		//cleaning up and scripting the windows to show the tree
//		CommandRecord.setScriptingFileS(sf);

		MesquiteModule treeWindowCoord = ownerModule.getFileCoordinator().findEmployeeWithName("#BasicTreeWindowCoord");
		if (treeWindowCoord!=null){
			String commands = "makeTreeWindow " + p.getTaxaReference(taxa) + "  #BasicTreeWindowMaker; tell It; ";
			commands += "getEmployee #" + getToolModule() + "; tell It; enableTools; endTell;";
			commands += "setTreeSource  #StoredTrees; tell It; setTaxa " + p.getTaxaReference(taxa) + " ;  setTreeBlock 1; endTell; ";
			commands += "getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;";
			commands += "tell It; suppress; setTreeDrawer  #" + getTreeDrawingModule()+"; tell It; orientRight; endTell; desuppress; endTell;";
			commands += "getWindow; tell It; setActive; setSize 600 600; getToolPalette; tell It; " + getSetToolScript()+ "; endTell; endTell;";
			commands += "  showWindow; endTell; ";
			MesquiteInteger pos = new MesquiteInteger(0);
			Puppeteer pup = new Puppeteer(ownerModule);
			CommandRecord oldCR = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(new CommandRecord(true));
			pup.execute(treeWindowCoord, commands, pos, null, false);
			MesquiteThread.setCurrentCommandRecord(oldCR);
		}
		return p;
	}

	private int getTerminals(TaxonNode taxonNode, String[] names, MesquiteString termName, MesquiteInteger c) {
		termName.setValue(taxonNode.name);
		int terms = 0;
		for (TaxonNode o : taxonNode.childNodes) {
			terms += getTerminals(o, names, termName, c);
		}
		if (terms == 0) {
			names[c.getValue()] =  termName.getValue(); //element.getAttributeValue("NAME");
			c.increment();
			return 1;
		}
		else
			return terms;
	}

	private boolean populateChildNodes(TaxonNode node, int levels) {
		return populateChildNodes(node, levels, 0) > 0;
	}

	private int populateChildNodes(TaxonNode node, int levels, int result) {
		if (node.isLeaf()) {
			result++;
		}
		if (node.isLeaf()) {
			for (Integer nodeID : node.children) {
				TaxonNode childNode = getTaxonNodeForNodeID(nodeID);
				if (childNode != null) {
					node.childNodes.add(childNode);
					if (levels > 0) {
						result = populateChildNodes(childNode, levels - 1, result);
					}
				}
			}
		}
		return result;
	}

	private MesquiteTree buildTreeFromRoot(TaxonNode rootNode, Taxa taxa) {
		MesquiteTree tree = new MesquiteTree(taxa);
		buildTree(rootNode, tree, tree.getRoot(), new MesquiteInteger(0));
		return tree;
	}

	private void buildTree(TaxonNode taxonNode, MesquiteTree tree, int node, MesquiteInteger taxonNumber) {
		if (taxonNode.isLeaf()) {
			tree.setTaxonNumber(node, taxonNumber.getValue(), false);
			taxonNumber.increment();
		} else {
			tree.setTaxonNumber(node, -1, false);
			tree.setNodeLabel(taxonNode.name, node);
			for (TaxonNode childNode : taxonNode.childNodes) {
				int childNodeNum = tree.sproutDaughter(node, false);
				buildTree(childNode, tree, childNodeNum, taxonNumber);
			}
		}
	}

	private TaxonNode getTaxonNodeForNodeID(int nodeID) {
		CloseableHttpClient httpclient;
		CloseableHttpResponse response = null;
		try {
			httpclient = HttpClients.createDefault();
			URI uri = taxonWorksURIBuilder
					.setPath("/api/v1/taxon_names/" + nodeID)
					.build();
			HttpGet httpget = new HttpGet(uri);
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String content = EntityUtils.toString(entity);
				ObjectMapper m = new ObjectMapper();
				JsonNode rootNode = m.readTree(content);
				return new TaxonNode(rootNode);
			}
			response.close();

		} catch (Exception e) {
			MesquiteMessage.println("  exception " + e.getClass().getName() + ": " + e.getMessage());
		}
		return null;
	}

	/*--------------------------*/
	private int getNodeIDforTaxonName(String groupName, MesquiteModule ownerModule) throws IOException {
		// GET http://127.0.0.1:3000/api/v1/taxon_names/autocomplete?term=Coleorrhyncha&token=Ad3Hx0c4oCMgS_GIBFO7ew&project_id=1
		CloseableHttpClient httpclient;
		CloseableHttpResponse response = null;
		try {
			httpclient = HttpClients.createDefault();
			URI uri = taxonWorksURIBuilder
					.setPath("/api/v1/taxon_names/autocomplete")
					.setParameter("term", groupName)
					.build();
			HttpGet httpget = new HttpGet(uri);
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String content = EntityUtils.toString(entity);
				ObjectMapper m = new ObjectMapper();
				JsonNode rootNode = m.readTree(content);
				if (rootNode.isArray()) {
					return rootNode.get(0).get("id").asInt();
				}
			}
			response.close();
		} catch (Exception e) {
			MesquiteMessage.println("caught an exception " + e.getClass().getName() + ": " + e.getMessage());
		}
		return 0;
	}
}

class TaxonNode {
	String name;
	int node_id;
	int parent_id;
	ArrayList<Integer> children;
	ArrayList<TaxonNode> childNodes;

	TaxonNode(JsonNode rootNode) {
		name = rootNode.get("name").textValue();
		node_id = rootNode.get("id").asInt();
		parent_id = rootNode.get("parent").get("id").asInt();
		children = new ArrayList<>();
		childNodes = new ArrayList<>();
		if (rootNode.has("children")) {
			Iterator<JsonNode> elements = rootNode.get("children").elements();
			while (elements.hasNext()) {
				JsonNode node = elements.next();
				children.add(node.asInt());
			}
		}
	}

	boolean hasChildren() {
		return (this.childNodes.size() > 0);
	}

	boolean isLeaf() {
		return (this.childNodes.size() == 0);
	}

	public String toString() {
		return String.valueOf(node_id) + ": " + children.toString();
	}

	String toNewickString() {
		StringBuilder result = new StringBuilder();
		if (isLeaf()) {
			return String.valueOf(node_id);
		} else {
			result.append("(");
			for (TaxonNode child : childNodes) {
				result.append(child.toNewickString());
				result.append(",");
			}
			// remove the last comma
			result.deleteCharAt(result.length()-1);
			result.append(")");
		}
		return result.toString();
	}
}
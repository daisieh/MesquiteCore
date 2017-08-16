/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.tol.GetTWTree;
/*~~  */


import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.tol.lib.*;


public class GetTWTree extends GeneralFileMaker  {
	protected int pageDepth = 5;
	protected String cladeName = "Coleorrhyncha";
	protected String tokenString = "Ad3Hx0c4oCMgS_GIBFO7ew";
	protected String taxonWorksURL = "http://127.0.0.1:3000";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public String getDialogLabel() {
		return "Clade in TaxonWorks Project";
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Tree from TaxonWorks Project...";
	}
	public String getExplanation() {
		return "Gets the tree for the page of the TaxonWorks Project for the group specified.";
	}
	public boolean isSubstantive(){
		return true;
	} 	
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public String getExtraArguments() {
		return null;
	}

	/** make a new  MesquiteProject.*/
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Obtain Tree from ToLWeb",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel(getDialogLabel());
		String helpString = "Enter the name of the clade in the Tree of Life Web Project you wish to examine. Enter into \"Number of descendent pages\" the size of the tree to be acquired, "
			+ "measured in terms of extent of descendent pages. If this number is 1, then only the single page's tree will be acquired; if the number is 2, then the single pages's tree plus all nodes on the "
			+ " immediate descendent pages will be acquired, and so on.  If the number is 0, or very large, then all descendent nodes will be acquired. \nNOTE: currently only values of 1 or 0 are supported!!";

		dialog.appendToHelpString(helpString);


		SingleLineTextField cladeNameField = dialog.addTextField(getDialogLabel(), cladeName, 20);
		SingleLineTextField twURL = dialog.addTextField("taxonworks url", taxonWorksURL, 20);
		SingleLineTextField token = dialog.addTextField("token", tokenString, 20);

		IntegerField pageDepthField = dialog.addIntegerField("Number of descendent pages:", pageDepth, 4, 1, 20);
		dialog.setDefaultTextComponent(cladeNameField);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			cladeName = cladeNameField.getText();
			pageDepth = pageDepthField.getValue();
			tokenString = token.getText();
			taxonWorksURL = twURL.getText();

		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments){
		if (arguments ==null) {
			if (queryOptions())
				arguments=cladeName;
		}
		if (arguments == null)
			return null;
		TWProjectOpener po = new TWProjectOpener();
		return po.establishProject(this, cladeName, tokenString, taxonWorksURL, pageDepth);
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 201;  
	}

}


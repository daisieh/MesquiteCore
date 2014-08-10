/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;

import mesquite.lib.*;


/* ======================================================================== */
/**Searchers to find trees that optimize something.*/

public abstract class TreeSearcher extends MesquiteModule  {
   	 public Class getDutyClass() {
   	 	return TreeSearcher.class;
   	 }
 	public String getDutyName() {
 		return "Tree Searcher";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#AddAndRearrange"};
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract boolean initialize(Taxa taxa);

	 public String getExtraTreeWindowCommands (){
   		 return "";
   	 }
	 public String getExtraIntermediateTreeWindowCommands (){
   		 return "";
   	 }

   	 /** Fills the passed tree block with trees. */
  	public abstract void fillTreeBlock(TreeVector treeList);
  	  
	public void setNumberTask(NumberForTree numTask){
	}
  	
   	public boolean isReconnectable(){
   		return false;
   	}
	//TEMPORARY Debugg.println  Should be only in disconnectable tree block fillers
	public void retrieveTreeBlock(TreeVector treeList){
	}
	 public boolean canGiveIntermediateResults(){
   		 return false;
   	 }
 	public Tree getLatestTree(Taxa taxa, MesquiteNumber score, MesquiteString titleForWindow){
   		 if (score != null)
   			 score.setToUnassigned();
   		 return null;
   	 }
	 protected void newResultsAvailable(TaxaSelectionSet outgroupTaxSet){
 		 if (getEmployer() instanceof TreeInferer){
 			 ((TreeInferer)getEmployer()).newResultsAvailable(outgroupTaxSet);
 		 }
 	 }
 	 }



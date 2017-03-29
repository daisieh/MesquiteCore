/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.PublicationCode;

import mesquite.categ.lib.*;
import mesquite.lists.lib.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.table.*;


/* ======================================================================== */
public class PublicationCode extends TaxonListAssistant {
	Taxa taxa;
	MesquiteTable table=null;
	Taxa currentTaxa = null;
	MolecularData data = null;
	MatrixSourceCoord matrixSourceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, DNAState.class, "Source of matrix (for " + getName() + ")"); 
		if (matrixSourceTask==null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		return true;
	}

	/*.................................................................................................................*/
	public void setData() {
			matrixSourceTask.initialize(currentTaxa);
			MCharactersDistribution observedStates = matrixSourceTask.getCurrentMatrix(currentTaxa);
			CharacterData d = observedStates.getParentData();
			if (d instanceof MolecularData)
				data = (MolecularData)d;
			else
				data=null;
		
	}
	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		if (this.taxa != null)
			this.taxa.removeListener(this);
		this.taxa = taxa;
		if (this.taxa != null)
			this.taxa.addListener(this);
		if (taxa != currentTaxa || data == null ) {
			currentTaxa = taxa;
			setData();
		}
		this.table = table;
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == matrixSourceTask)  // character source quit and none rehired automatically
			iQuit();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		setData();
		super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getMatrixSource", matrixSourceTask);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the matrix source", null, commandName, "getMatrixSource")) {
			return matrixSourceTask;
		}
		else return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Publication Code";
	}
	public String getStringForTaxon(int it){
		if (data==null || taxa==null)
			return "-";
		Taxon taxon = data.getTaxa().getTaxon(it);
		Associable tInfo = data.getTaxaInfo(false);
		if (tInfo != null && taxon != null) {
			return (String)tInfo.getAssociatedObject(CharacterData.publicationCodeNameRef, it);
		}
		return "-";
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int row){
		return true;
	}
	/*...............................................................................................................*/
	/** for those permitting editing, indicates user has edited to incoming string.*/
	public void setString(int row, String s){
		if (data==null || taxa==null)
			return;
		Taxon taxon = data.getTaxa().getTaxon(row);
		Associable tInfo = data.getTaxaInfo(true);
		if (tInfo != null && taxon != null) {
			tInfo.setAssociatedObject(CharacterData.publicationCodeNameRef, row, s);
		}
	}
	public boolean useString(int ic){
		return true;
	}

	public String getWidestString(){
		return "8888888888888  ";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Publication Code";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 320;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Lists the publication code for a sequence." ;
	}
}

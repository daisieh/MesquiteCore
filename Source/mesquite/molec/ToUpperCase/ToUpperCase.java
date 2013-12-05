/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.ToUpperCase;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class ToUpperCase extends DNADataAlterer {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
		
 	/*.................................................................................................................*/
  	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference){
 			if (!(data instanceof DNAData)){
				MesquiteMessage.warnProgrammer("Attempt to set non-DNA data to upper case");
				return false;
			}
			return alterContentOfCells(data,table, undoReference);
   	}
	/*.................................................................................................................*/
   	public void alterCell(CharacterData ddata, int ic, int it){
   		DNAData data = (DNAData)ddata;
		long state = data.getStateRaw(ic,it);
		if (!CategoricalState.isInapplicable(state) && !CategoricalState.isUnassigned(state)) {
			if (CategoricalState.isLowerCase(state)){
				state = CategoricalState.setLowerCase(state, false);
				data.setState(ic,it, state);
			}
		}
   	}
	
	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Set to Upper Case";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Alters nucleotide data to upper case coding (e.g, to indicate more certain)." ;
   	 }
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
 	 
}



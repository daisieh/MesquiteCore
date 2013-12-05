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
package mesquite.lists.lib;

import java.awt.*;

import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;



/* ======================================================================== */
public abstract class DatasetsListUtility extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return DatasetsListUtility.class;
   	 }
 	public String getDutyName() {
 		return "Datasets list utility";
   	 }

   	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
   	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
   	public abstract boolean operateOnDatas(ListableVector datas, MesquiteTable table);
}


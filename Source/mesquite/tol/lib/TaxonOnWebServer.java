/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.tol.lib; 

import mesquite.lib.*;



/* ======================================================================== */
/**.*/
public abstract class TaxonOnWebServer extends MesquiteModule  {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	public boolean isSubstantive(){
		return false;
	}   	 
/*.................................................................................................................*/
	public Class getDutyClass() {
		return TaxonOnWebServer.class;
	}
	public String getDutyName() {
		return "Taxon On Web Server";
	}
  	 public String[] getDefaultModule() {
    	 	return new String[] {"#ToLURLServer"};
    	 }

  	 
	/*.................................................................................................................*/
	public abstract  String getURL(String taxonName);
	
	
	
}


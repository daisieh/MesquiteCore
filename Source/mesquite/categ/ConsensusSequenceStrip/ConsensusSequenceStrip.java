package mesquite.categ.ConsensusSequenceStrip;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.charMatrices.ColorByState.ColorByState;
import mesquite.molec.ColorByAA.ColorByAA;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;



public class ConsensusSequenceStrip extends DataColumnNamesAssistant {
	long[] consensusSequence = null;
	MesquiteBoolean showSomeInapplicableAsGray = new MesquiteBoolean(false);
	CategStateForCharacter stateTask = null;
	MesquiteString stateTaskName;
	MesquiteCommand stC;
	MesquiteSubmenuSpec stSubmenu;
	MesquiteMenuItemSpec menuItem1, menuItem2, colorByAAMenuItem, closeMenuItem, lineMenuItem;
	MesquiteBoolean colorByAA = new MesquiteBoolean(false);
	
	boolean suspend = false;

	MesquiteBoolean selectedOnly = new MesquiteBoolean(true);

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		setUseMenubar(false);
		
		stateTask = (CategStateForCharacter)hireEmployee(commandRec, CategStateForCharacter.class, "Calculator of character state for each character");
		if (stateTask == null)
			return sorry(commandRec, getName() + " couldn't start because no calculator of character state obtained");
		stateTaskName = new MesquiteString(stateTask.getName());
		stC = makeCommand("setStateCalculator",  this);
		stateTask.setHiringCommand(stC);
		if (numModulesAvailable(CategStateForCharacter.class)>1) {
			MesquiteSubmenuSpec stSubmenu = addSubmenu(null, "State Calculator", stC, CategStateForCharacter.class);
			stSubmenu.setSelected(stateTaskName);
		}
		
		return true;
  	 }
	/*.................................................................................................................*/
	public void deleteMenuItems() {
		deleteMenuItem(stSubmenu);
		deleteMenuItem(menuItem1);
		deleteMenuItem(menuItem2);
		deleteMenuItem(colorByAAMenuItem);
	}
	/*.................................................................................................................*/
	public void checkMenuItems() {
		colorByAAMenuItem.setEnabled((data instanceof DNAData) && ((DNAData)data).someCoding());
	}
	public void deleteRemoveMenuItem() {
		deleteMenuItem(lineMenuItem);
		deleteMenuItem(closeMenuItem);
	}
	public void addRemoveMenuItem() {
		closeMenuItem= addMenuItem(null,"Remove Consensus Sequence", makeCommand("remove", this));
		lineMenuItem = addMenuLine();
	}
		
	public void setTableAndData(MesquiteTable table, CharacterData data, CommandRecord commandRec) {
		deleteMenuItems();
		deleteRemoveMenuItem();
		addRemoveMenuItem();
		stSubmenu = addSubmenu(null, "State Calculator", stC, CategStateForCharacter.class);
		stSubmenu.setSelected(stateTaskName);
		menuItem1= addCheckMenuItem(null,"Selected Taxa Only", makeCommand("toggleSelectedOnly", this), selectedOnly);
		menuItem2= addCheckMenuItem(null,"Darken if Any Gaps", makeCommand("toggleGrayIfGaps", this), showSomeInapplicableAsGray);
		colorByAAMenuItem= addCheckMenuItem(null,"Color Nucleotide by AA Color", makeCommand("toggleColorByAA", this), colorByAA);
		
		
		if (data != null)
			data.removeListener(this);
		this.data = data;
		this.table = table;
		data.addListener(this);
		
		checkMenuItems();
		calculateSequence();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("suspend");
		temp.addLine("setStateCalculator " , stateTask);
		temp.addLine("toggleSelectedOnly " + selectedOnly.toOffOnString());
		temp.addLine("toggleGrayIfGaps " + showSomeInapplicableAsGray.toOffOnString());
		temp.addLine("toggleColorByAA " + colorByAA.toOffOnString());
		temp.addLine("resume");

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not only selected taxa are included are all taxa.", "[on or off]", commandName, "toggleSelectedOnly")) {
			boolean current = selectedOnly.getValue();
			selectedOnly.toggleValue(parser.getFirstToken(arguments));
			if (stateTask!=null)
				stateTask.setSelectedOnly(selectedOnly.getValue());
			if (current!=selectedOnly.getValue() && !suspend) {
				parametersChanged(null, commandRec);
				calculateSequence();
				if (table !=null) {
						table.repaintAll();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show nucleotides by the color of their amino acid.", "[on or off]", commandName, "toggleColorByAA")) {
			boolean current = colorByAA.getValue();
			colorByAA.toggleValue(parser.getFirstToken(arguments));
			if (current!=colorByAA.getValue() && !suspend) {
				parametersChanged(null, commandRec);
				calculateSequence();
				if (table !=null) {
					table.repaintAll();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to show the consensus cell as gray if there are any gaps in any taxon.", "[on or off]", commandName, "toggleGrayIfGaps")) {
			boolean current = showSomeInapplicableAsGray.getValue();
			showSomeInapplicableAsGray.toggleValue(parser.getFirstToken(arguments));
			if (current!=showSomeInapplicableAsGray.getValue() && !suspend) {
				parametersChanged(null, commandRec);
				calculateSequence();
				if (table !=null) {
					table.repaintAll();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Removes the Info Strip", null, commandName, "remove")) {
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspend = false;
			calculateSequence();
			parametersChanged(null, commandRec);
		}
		else if (checker.compare(this.getClass(), "Sets the calculator of a state for each character", "[name of state calculator module]", commandName, "setStateCalculator")) {
			CategStateForCharacter temp = (CategStateForCharacter)replaceEmployee(commandRec, CategStateForCharacter.class, arguments, "Calculator of character state for each character", stateTask);
			if (temp !=null){
				stateTask = temp;
				stateTask.setHiringCommand(stC);
				stateTaskName.setValue(stateTask.getName());
				stateTask.setSelectedOnly(selectedOnly.getValue());
				if (!suspend) {
					parametersChanged(null, commandRec);
					calculateSequence();
					if (table !=null){
						table.repaintAll();
					}
				}
				return stateTask;
			}
			addRemoveMenuItem();
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
  	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification, CommandRecord commandRec) {
 		calculateSequence();
			if (table !=null)
				table.repaintAll();
  	 }
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}
	/*.................................................................................................................*/
	public void endJob() {
		if (table!=null) {
			((ColumnNamesPanel)table.getColumnNamesPanel()).decrementInfoStrips();
			table.resetTableSize(false);
		}
		super.endJob();
	}

 
	/*.................................................................................................................*/
	 public boolean atLeastOneInapplicable(int ic){
		 if (data==null || table==null)
			 return false;
		 //long s;
		 int numTaxa = data.getNumTaxa();
		 int numTaxaWithData = 0;
		 for (int it=0; it<numTaxa; it++) {
			 if (!selectedOnly.getValue() || table.isRowSelected(it) || !table.anyRowSelected()) {
				 long s= ((CategoricalData)data).getState(ic,it);
				 if (CategoricalState.isInapplicable(s))
					 return true;
			 }
		 }
		 return false;
	 }

		/*.................................................................................................................*/
	 public void calculateSequence() {
		 if (!colorByAA.getValue())
			 return;
		 CategoricalState resultState = new CategoricalState();
		 MesquiteString resultString = new MesquiteString();
		 long[] sequence = new long[data.getNumChars()];
		 for (int i = 0; i<data.getNumChars(); i++) {
			 stateTask.calculateState( (CategoricalData)data,  i,  table,  resultState,  resultString);
			 sequence[i] = resultState.getValue();
		 }
		consensusSequence = sequence;
	 }
		/*.................................................................................................................*/
	 public void drawInCell(int ic, Graphics g, int x, int y, int w, int h, boolean selected) {
		 if (stateTask==null) 
			 return;
		 long s= CategoricalState.inapplicable;
		 if (colorByAA.getValue()) {  // have to use precomputed sequences
			 if (consensusSequence==null)
				 calculateSequence();
			 if (ic>=0 && ic<consensusSequence.length)
				 s =consensusSequence[ic];
		 } else {
			 CategoricalState resultState = new CategoricalState();
			 MesquiteString resultString = new MesquiteString();
			 stateTask.calculateState( (CategoricalData)data,  ic,  table,  resultState,  resultString);
			 s = resultState.getValue();
		 }

		 boolean colorSomeInapp = atLeastOneInapplicable(ic) && showSomeInapplicableAsGray.getValue();
		 if (!CategoricalState.isEmpty(s) && !CategoricalState.isInapplicable(s)){
			 boolean grayText = false;
			 int e =  CategoricalState.getOnlyElement(s);
			 if (colorSomeInapp) {
				 g.setColor(Color.darkGray);
			 }
			 if (data instanceof DNAData){
				 if (e>=0) {
					 if (colorByAA.getValue()){
						 Color color = null;
						 long aa = ((DNAData)data).getAminoAcid(consensusSequence, ic,true);
						 if (!CategoricalState.isImpossible(aa))
							 color = ProteinData.getAminoAcidColor(aa);
						 if (color==null)
							 g.setColor(DNAData.getDNAColorOfState(e));
						 else
							 g.setColor(color);
					 }
					 else
						 g.setColor(DNAData.getDNAColorOfState(e));
				 }
				 else {
					 g.setColor(Color.white);
					 grayText =true;
				 }
			 }
			 else if (data instanceof ProteinData){
				 if (e>=0)
					 g.setColor(ProteinData.getProteinColorOfState(e));
				 else {
					 g.setColor(Color.white);
					 grayText =true;
				 }
			 }
			 else 
				 g.setColor(Color.white);
			 g.fillRect(x,y,w,h);

			 if (colorSomeInapp) 
				 GraphicsUtil.darkenRectangle(g, x, y, w, h,0.6f);
			 if (grayText) {
				 if (colorSomeInapp) 
					 g.setColor(Color.darkGray);
				 else 
					 g.setColor(Color.lightGray);
			 }
			 else
				 g.setColor(Color.black);
			 StringBuffer sb = new StringBuffer();
			 ((CategoricalData)data).statesIntoStringBufferCore(ic,  s,  sb, true,false, false);
			 g.drawString(sb.toString(), x+4, y+11);
		 }
		 else if (CategoricalState.isInapplicable(s)){
			 g.setColor(Color.lightGray);
			 g.fillRect(x,y,w,h);
		}
		else {
			g.setColor(Color.white);
			g.fillRect(x,y,w,h);
		}
	}

	 /*.................................................................................................................*/
	 /** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	 public void changed(Object caller, Object obj, Notification notification, CommandRecord commandRec){
		 int code = Notification.getCode(notification);
		 if (obj instanceof Taxa &&  (Taxa)obj ==data.getTaxa()) {
			 if (code==MesquiteListener.SELECTION_CHANGED && selectedOnly.getValue()) {
				 calculateSequence();
			 }
			 else if (code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_DELETED) {
				 calculateSequence();
			 }
		 }
		 else if (obj instanceof CharacterData && (CharacterData)obj ==data) {
			 if (code==MesquiteListener.PARTS_DELETED || code==AssociableWithSpecs.SPECSSET_CHANGED || code==MesquiteListener.PARTS_ADDED || code==MesquiteListener.PARTS_MOVED || code==MesquiteListener.DATA_CHANGED) {
				 calculateSequence();
			 }
			 else{
				 calculateSequence();
			 }
		 }
		 super.changed(caller, obj, notification, commandRec);
	 }
	 /*.................................................................................................................*/
	 /** returns whether this module is requesting to appear as a primary choice */
	 public boolean requestPrimaryChoice(){
		 return true;  
	 }
	public String getTitle() {
		return "Consensus Sequence";
	}


	public String getName() {
		return "Consensus Sequence";
	}

}
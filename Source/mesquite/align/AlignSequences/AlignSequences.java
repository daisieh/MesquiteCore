/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.align.AlignSequences;/*~~  */import java.util.*;import java.lang.*;import java.io.*;import java.awt.*;import java.awt.event.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.categ.lib.*;import mesquite.lib.table.*;import mesquite.align.lib.*;/* ======================================================================== */public class AlignSequences extends MolecDataEditorInit {	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed		EmployeeNeed e2 = registerEmployeeNeed(MultipleSequenceAligner.class, getName() + " needs a module to calculate alignments.",		"The sequence aligner is chosen in the Align Multiple Sequences submenu of the Matrix menu");	}  	static boolean separateThreadDefault = false;    	boolean separateThread = separateThreadDefault;    	MolecularData data ;    	MultipleSequenceAligner aligner;    	    	MesquiteTable table;	MesquiteSubmenuSpec mss= null;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		mss = addSubmenu(null, "Align Multiple Sequences", makeCommand("doAlign",  this));		mss.setList(MultipleSequenceAligner.class);		return true;	}	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */   	public boolean requestPrimaryChoice(){   		return true;     	}	/*.................................................................................................................*/	public void setTableAndData(MesquiteTable table, CharacterData data, CommandRecord commandRec){		if (!(data instanceof MolecularData)){			mss.setEnabled(false);			resetContainingMenuBar();			return;		}		this.table = table;		this.data = (MolecularData)data;		mss.setCompatibilityCheck(data.getStateClass());		resetContainingMenuBar();			}	/*.................................................................................................................*/   	 public boolean isSubstantive(){   	 	return true;   	 }	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {    	 	if (checker.compare(this.getClass(), "Hires module to align sequences", "[name of module]", commandName, "doAlign")) {   	 		if (table!=null && data !=null){	    	 	if (data.getEditorInhibition()){	    	 		discreetAlert(commandRec, "This matrix is marked as locked against editing.");	    	 		return null;	    	 	}	    	 	aligner= (MultipleSequenceAligner)hireNamedEmployee(commandRec, MultipleSequenceAligner.class, arguments);				if (aligner!=null) {					boolean a = alterData(data, table, commandRec);	 	   			if (a) {	 	   				table.repaintAll();						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED), commandRec);					}					if (!separateThread) {						fireEmployee(aligner);					}				}			}    	 	}    	 	else    	 		return super.doCommand(commandName, arguments, commandRec, checker);	return null;   	 }    	/*.................................................................................................................*/   	public boolean integrateAlignment(long[][] alignedMatrix, MolecularData data, int icStart, int icEnd, int itStart, int itEnd, CommandRecord commandRec){		if (alignedMatrix == null || data == null)			return false;		mesquiteTrunk.incrementProjectBrowserRefreshSuppression();		AlignUtil util = new AlignUtil();		Rectangle problem = null;			//alignedMatrix.setName("Aligned (" + data.getName() + ")");		boolean wasSel;		if (data.anySelected()) {			wasSel = true;		}		else {			wasSel = false;		}		logln("Alignment for " + (icEnd-icStart+1) + " sites; aligned to " + alignedMatrix.length + " sites.");		problem = util.forceAlignment(data, icStart, icEnd, itStart, itEnd, 0, alignedMatrix);		if (wasSel) {			data.deselectAll();			int numCharsOrig = icEnd-icStart+1;			if (alignedMatrix.length>numCharsOrig)				numCharsOrig = alignedMatrix.length;			for (int i = icStart; i<icStart + numCharsOrig; i++)				data.setSelected(i, true);						}		MesquiteModule mb = data.showMatrix(commandRec);		if (problem != null) {			MolecularData alignedData = (MolecularData)data.makeCharacterData(data.getNumTaxa(), alignedMatrix.length);			alignedData.addToFile(data.getFile(), getProject(), null);			for (int ic = 0; ic<alignedMatrix.length; ic++)				for (int it=0; it<data.getNumTaxa(); it++)					alignedData.setState(ic, it, alignedMatrix[ic][it]);			MesquiteModule mbAligned = alignedData.showMatrix(commandRec);			if (mbAligned != null) {				MesquiteTable tableAligned = ((TableWindow)mbAligned.getModuleWindow()).getTable();				tableAligned.deselectAll();				tableAligned.selectCell(problem.width, problem.height);				tableAligned.setFocusedCell(problem.width, problem.height);				MesquiteTable table = ((TableWindow)mb.getModuleWindow()).getTable();				table.deselectAll();				table.selectCell(problem.x, problem.y);				table.setFocusedCell(problem.x, problem.y);			}		}				if (separateThread) {			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED), commandRec);			MesquiteTable table = ((TableWindow)mb.getModuleWindow()).getTable();			table.repaintAll();		}				mesquiteTrunk.decrementProjectBrowserRefreshSuppression();		if (separateThread)			fireEmployee(aligner);		return true;	}			/*.................................................................................................................*/   	/** Called to alter data in those cells selected in table*/   	public boolean alterData(CharacterData data, MesquiteTable table, CommandRecord commandRec){		this.data = (MolecularData)data;		if (!(this.data.contiguousSelection() || !this.data.anySelected())) {			discreetAlert(commandRec, "Data can be aligned only for the whole matrix or for a contiguous set of selected characters");			return false;		}		if (	separateThread= !AlertDialog.query(containerOfModule(), "Separate Thread?", "Run on separate thread? (Beware! Don't close window before done)","No", "Separate")){			AlignThread alignThread = new AlignThread(this, aligner, this.data, this.table, CommandRecord.scriptingRecord);			alignThread.start();   		}   		else {			AlignThread alignThread = new AlignThread(this, aligner, this.data, this.table, commandRec);			alignThread.run();  			return true;		}		return false;   	} 	/*.................................................................................................................*/ 	 public boolean showCitation() {		return false;   	 }	/*.................................................................................................................*/   	 public boolean isPrerelease(){   	 	return false;   	 }	/*.................................................................................................................*/    	 public String getName() {		return "Align Sequences";   	 }	/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Sends the selected sequence to be aligned." ;   	 }   	 }class AlignThread extends Thread {	AlignSequences ownerModule;	MultipleSequenceAligner aligner;	MolecularData data;	CommandRecord commandRec;	MesquiteTable table;		public AlignThread(AlignSequences ownerModule, MultipleSequenceAligner aligner, MolecularData data, MesquiteTable table, CommandRecord commandRec){		this.aligner = aligner;		this.ownerModule = ownerModule;		this.data = data;		this.table = table;		this.commandRec = commandRec;	}	public void run() {						MesquiteInteger firstRow = new MesquiteInteger();			MesquiteInteger lastRow = new MesquiteInteger();			MesquiteInteger firstColumn = new MesquiteInteger();			MesquiteInteger lastColumn = new MesquiteInteger();						boolean entireColumnsSelected = false;			int oldNumChars = data.getNumChars();			if (!table.singleCellBlockSelected(firstRow, lastRow,  firstColumn, lastColumn)) {				firstRow.setValue(0);				lastRow.setValue(data.getNumTaxa()-1);				firstColumn.setValue(0);				lastColumn.setValue(data.getNumChars()-1);			}			else 								entireColumnsSelected =  table.isColumnSelected(firstColumn.getValue());			//NOTE: at present this deals only with whole character selecting, and with all taxa			long[][] m  = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), null, firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue(), commandRec);			ownerModule.integrateAlignment(m, data,  firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue(),commandRec);			if (entireColumnsSelected) {					for (int ic = 0; ic<data.getNumChars(); ic++) 						data.setSelected(ic,ic>=firstColumn.getValue() && ic<=lastColumn.getValue()- (oldNumChars - data.getNumChars()));					table.selectColumns(firstColumn.getValue(),lastColumn.getValue()- (oldNumChars - data.getNumChars()));			}		}}
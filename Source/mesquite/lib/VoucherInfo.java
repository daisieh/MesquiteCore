/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.lib; import mesquite.lib.*;/* ======================================================================== */public class  VoucherInfo {	public static final NameReference voucherCodeRef = NameReference.getNameReference("VoucherCode"); //String: taxa	public static final NameReference voucherDBRef = NameReference.getNameReference("VoucherDB");//String: taxa	protected String voucherID;	protected String species;	protected String organism = "";	protected String latLong;	protected String locality;	protected String note;	protected String collectionDate;	protected String identifiedBy;	StringArray fieldNames, fieldValues;	boolean flexible = true;	public VoucherInfo(){		fieldNames = new StringArray(20);		fieldValues = new StringArray(20);	}	public VoucherInfo(String voucherID, String species, String latLong, String locality, String note, String collectionDate, String identifiedBy){		this.voucherID = voucherID;		this.species = species;		this.latLong = latLong;		this.locality = locality;		this.note = note;		this.collectionDate = collectionDate;		this.identifiedBy = identifiedBy;		flexible=false;	}	public void addElement(String fieldName, String fieldValue){		if (!flexible)			return;		if (StringUtil.blank(fieldName))			return;		if (fieldNames.getFilledSize()>=fieldNames.getSize()){   //add more elements if needed			fieldNames.addParts(fieldNames.getSize(), 1);			fieldValues.addParts(fieldValues.getSize(), 1);		}		fieldNames.setValue(fieldNames.getFilledSize(), StringUtil.cleanseStringOfFancyChars(fieldName));		if (!StringUtil.blank(fieldValue))			fieldValues.setValue(fieldValues.getFilledSize(), StringUtil.cleanseStringOfFancyChars(fieldValue));	}	public void addElement(String fieldName){		addElement(fieldName,null);	}	public void setFieldValue(int i, String fieldValue){		if (!flexible)			return;		if (StringUtil.blank(fieldValue))			return;		if (fieldValues==null || i>=fieldValues.getSize())			return;		String s = StringUtil.cleanseStringOfFancyChars(fieldValue);		if (fieldNames!=null && fieldNames.getValue(i)!=null && fieldNames.getValue(i).equalsIgnoreCase("organism"))			organism = s;		fieldValues.setValue(i, s);	}	public String getVoucherID(){		return voucherID;	}	public String getSpecies(){		return species;	}	public String getLatLong(){		return latLong;	}	public String getLocality(){		return locality;	}	public String getNote(){		return note;	}	public String getCollectionDate(){		return collectionDate;	}	public String getIdentifiedBy() {		return identifiedBy;	}	public String getGenBankFieldValue(String fieldName){		if (StringUtil.blank(fieldName))			return null;		if (flexible) {			for (int i=0; i<fieldNames.getFilledSize(); i++) 				if (fieldName.equalsIgnoreCase(fieldNames.getValue(i))) {					return StringUtil.cleanseStringOfFancyChars(fieldValues.getValue(i));				}		}		return null;	}	public String toString(){		if (flexible) {			String s = "";			for (int i=0; i<fieldNames.getFilledSize(); i++) {				s += fieldNames.getValue(i) + " " ;  						}			return "VoucherInfo: " +s;		}		else			return "VoucherInfo: " + voucherID + " " + species + " " + latLong + " " + locality + " " + note + " " + collectionDate;	}		//lineage???  lat lon	public String toGenBankString(){		if (flexible) {			String s = "";			String value = "";			for (int i=0; i<fieldNames.getFilledSize(); i++) {				if (fieldNames.getValue(i) != null && !fieldNames.getValue(i).startsWith("*")){  //DON;'t use if starts with *				value = StringUtil.cleanseStringOfFancyChars(fieldValues.getValue(i));				if (!StringUtil.blank(value))					s += "[" + fieldNames.getValue(i) + " = " + value + "] ";  					}			}			s += "  " + organism + " ";			return s;		}		else {			String s = "";			if (!StringUtil.blank(species))				s += "[organism = " + species + "] ";			if (!StringUtil.blank(identifiedBy))				s += "[identified-by = " + identifiedBy + "] ";			if (!StringUtil.blank(voucherID))				s += "[specimen-voucher = " + voucherID + "] ";			if (!StringUtil.blank(locality))				s += "[country = " + locality + "] ";			if (!StringUtil.blank(latLong))				s += "[lat-lon = " + latLong + "] ";			if (!StringUtil.blank(collectionDate))				s += "[collection-date = " + collectionDate + "] ";			if (!StringUtil.blank(note))				s += "[note = " + note + "] ";			return s;		}	}}
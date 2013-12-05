// Codons.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for all Codons (including terminators).
 * Accepts the same characters as those given by
 * CodonTranslator.getUniqueCharacter(), 
 * states here are index for CodonTranslator.getCodonIndex()
 * That is. Codons.getCharacter(CondonTranslator.getIndex("codon")) ==
 * CondonTranslator.getUniqueCharacter("codon")
 *
 * @version $Id: Codons.java,v 1.8 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class Codons extends SimpleDataType
{

	// Get number of bases
	public int getNumStates()
	{
		return 64;
	}

	
	public int getState(char c)
	{
  	if(c==UNKNOWN_CHARACTER) {
			return 64;
		}
		return (int)(c - 64);
	}

	/**
	 * Get character corresponding to a given state
	 */
	public char getChar(int state)
	{

		if(state>=64) {
			return UNKNOWN_CHARACTER;
		}
		return (char)(state + 64);
	}

	// String describing the data type
	public String getDescription()
	{
		return "Codon";
	}

	/**
		* @retrun true if this state is an unknown state
		*/
	public boolean isUnknownState(int state) {
		return(state>=64);
	}

	// Get numerical code describing the data type
	public int getTypeID()
	{
		return DataType.CODONS;
	}
}


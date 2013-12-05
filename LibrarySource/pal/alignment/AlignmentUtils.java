// AlignmentUtils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.datatype.*;
import pal.io.*;
import pal.misc.*;
import java.io.*;

/**
 * Helper utilities for alignments.
 *
 * @version $Id: AlignmentUtils.java,v 1.7 2001/07/13 14:39:12 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class AlignmentUtils {

	static FormattedOutput format = FormattedOutput.getInstance();


	/** report number of sequences, sites, and data type */
	public static void report(Alignment a, PrintWriter out)
	{
		if (a.getDataType() == null) {
			a.setDataType(AlignmentUtils.getSuitableInstance(a));
		}
	
		out.println("Number of sequences: " + a.getSequenceCount());
		out.println("Number of sites: " + a.getSiteCount());
		out.println("Data type: " + a.getDataType().getDescription() + " data");
	}

	/** print alignment (default format: INTERLEAVED) */
	public static void print(Alignment a, PrintWriter out)
	{
		printInterleaved(a, out);	
	}

	/** print alignment (in plain format) */
	public static void printPlain(Alignment a, PrintWriter out) {
		printPlain(a, out, false);
	}

	/** print alignment (in plain format) */
	public static void printPlain(Alignment a, PrintWriter out, boolean relaxed)
	{
		// PHYLIP header line
		out.println("  " + a.getSequenceCount() + " " + a.getSiteCount());

		for (int s = 0; s < a.getSequenceCount(); s++)
		{
 			format.displayLabel(out, a.getIdentifier(s).getName(), (relaxed ? 20 : 10));
			out.print("     ");
			printNextSites(a, out, false, s, 0, a.getSiteCount());
			out.println();
		}
	}

	/** print alignment (in PHYLIP SEQUENTIAL format) */
	public static void printSequential(Alignment a, PrintWriter out)
	{
		// PHYLIP header line
		out.println("  " + a.getSequenceCount() + " " + a.getSiteCount() + "  S");

		// Print sequences
		for (int s = 0; s < a.getSequenceCount(); s++)
		{
			int n = 0;
			while (n < a.getSiteCount())
			{
				if (n == 0)
				{
 					format.displayLabel(out, 
						a.getIdentifier(s).getName(), 10);
					out.print("     ");
				}
				else
				{
					out.print("               ");
				}
				printNextSites(a, out, false, s, n, 50);
				out.println();
				n += 50;
			}
		}
	}


	/** print alignment (in PHYLIP 3.4 INTERLEAVED format) */
	public static void printInterleaved(Alignment a, PrintWriter out)
	{
		int n = 0;

		// PHYLIP header line
		out.println("  " + a.getSequenceCount() + " " + a.getSiteCount());

		// Print sequences
		while (n < a.getSiteCount())
		{
			for (int s = 0; s < a.getSequenceCount(); s++)
			{
				if (n == 0)
				{
 					format.displayLabel(out, 
						a.getIdentifier(s).getName(), 10);
					out.print("     ");
				}
				else
				{
					out.print("               ");
				}
				printNextSites(a, out, true, s, n, 50);
				out.println();
			}
			out.println();
			n += 50;
		}
	}

	/** Print alignment (in CLUSTAL W format) */
	public static void printCLUSTALW(Alignment a, PrintWriter out)
	{
		int n = 0;

		// CLUSTAL W header line
		out.println("CLUSTAL W multiple sequence alignment");
		out.println();

		// Print sequences
		while (n < a.getSiteCount())
		{
			out.println();
			for (int s = 0; s < a.getSequenceCount(); s++)
			{
 				format.displayLabel(out, a.getIdentifier(s).getName(), 10);
				out.print("     ");
	
				printNextSites(a, out, false, s, n, 50);
				out.println();
			}
			// Blanks in status line are necessary for some parsers)
			out.println("               ");
			n += 50;
		}
	}	
	
	/**
	 * Returns state indices for a sequence.
	 */
	public static final void getAlignedSequenceIndices(Alignment a, int i, int[] indices, DataType dataType) {

		String sequence = a.getAlignedSequenceString(i);
		
		for (int j = 0; j < a.getSiteCount(); j++) {
			indices[j] = dataType.getState(sequence.charAt(j));	
		}
	}
	
	/**
	 * Returns total sum of pairs alignment penalty using gap creation 
	 * and extension penalties and transition penalties in the 
	 * TransitionPenaltyTable provided. By default this is end-weighted.
	 */
	public static double getAlignmentPenalty(
					Alignment a, 
					TransitionPenaltyTable penalties, 
					double gapCreation, 
					double gapExtension) {
		
		return getAlignmentPenalty(a, a.getDataType(), 
			penalties, gapCreation, gapExtension, false /* end-weighted */);
	}

	/**
	 * Returns total sum of pairs alignment distance using gap creation 
	 * and extension penalties and transition penalties as defined in the 
	 * TransitionPenaltyTable provided. 
	 * @param local true if end gaps ignored, false otherwise
	 */
	public static double getAlignmentPenalty(
					Alignment a, 
					DataType dataType, 
					TransitionPenaltyTable penalties, 
					double gapCreation,
					double gapExtension,
					boolean local) {
	
		int[][] indices = new int[a.getSequenceCount()][a.getSiteCount()];
		for (int i = 0; i < a.getSequenceCount(); i++) {
			getAlignedSequenceIndices(a, i, indices[i], dataType);
		}
	
		CostBag totalBag = new CostBag();
		for (int i = 0; i < a.getSequenceCount(); i++) {
			for (int j = i + 1; j < a.getSequenceCount(); j++) {
				totalBag.add(getAlignmentPenalty(a, penalties, i, j, 
					indices[i], indices[j], local));
			}
		}
		return totalBag.score(gapCreation, gapExtension);
	}

	/**
	 * guess data type suitable for a given sequence data set
	 *
	 * @param alignment alignment
	 *
	 * @return suitable DataType object
	 */
	public static DataType getSuitableInstance(Alignment alignment)
	{	
		// count A, C, G, T, U, N 
		long numNucs = 0;
		long numChars = 0;
		long numBins = 0;
		for (int i = 0; i < alignment.getSequenceCount(); i++)
		{
			for (int j = 0; j < alignment.getSiteCount(); j++)
			{
				char c = alignment.getData(i, j);
			
				if (c == 'A' || c == 'C' || c == 'G' ||
			   		c == 'T' || c == 'U' || c == 'N') numNucs++;
			
				if (c != '-' && c != '?') numChars++;
			
				if (c == '0' || c == '1') numBins++;
			}
		}
	
		if (numChars == 0) numChars = 1;
		
		// more than 85 % frequency advocates nucleotide data
		if ((double) numNucs / (double) numChars > 0.85)
		{
			return new Nucleotides();
		}
		else if ((double) numBins / (double) numChars > 0.2)
		{
			return new TwoStates();
		}
		else
		{
			return new AminoAcids();
		}	
	}

	/** count states (creates dataType if not yet specified) */
	public static double[] estimateFrequencies(Alignment a)
	{
		
		if (a.getDataType() == null)
		{
			a.setDataType(getSuitableInstance(a));
		}
		
		int numStates = a.getDataType().getNumStates();
		
		double[] frequency = new double[numStates];
	
		long[] stateCount = new long[numStates+1];
		
		for (int i = 0; i < numStates+1; i++)
		{
			stateCount[i] = 0;
		}
		
		for (int i = 0; i < a.getSequenceCount(); i++)
		{
			for (int j = 0; j < a.getSiteCount(); j++)
			{
				stateCount[a.getDataType().getState(a.getData(i,j))] += 1;
			}
		}
		
		// Compute frequencies suitable for RateMatrix (sum = 1.0)
		long sumStates = a.getSiteCount()*a.getSequenceCount()-stateCount[numStates];
		for (int i = 0; i < numStates; i++)
		{
			frequency[i] = (double) stateCount[i]/sumStates;
		}

		a.setFrequency(frequency);

		return frequency;
	}


	public static final boolean isSiteRedundant(Alignment a, int site) {
		int numSeq = a.getSequenceCount();
		for(int i = 0 ; i < numSeq ; i++) {
			if (!isGap(a, i,site)) {
				return false;
			}
		}
		return true;
	}

	public static final Alignment removeRedundantSites(Alignment a) {
		boolean[] keep = new boolean[a.getSiteCount()];
		int toKeep = 0;
		for(int i = 0 ; i < keep.length ;i++) {
			keep[i] = !isSiteRedundant(a,i);
			if(keep[i]) {
				toKeep++;
			}
		}
		String[] newSeqs = new String[a.getSequenceCount()];
		int numberOfSites = a.getSiteCount();
		for(int i = 0 ; i < newSeqs.length ; i++) {
			StringBuffer sb = new StringBuffer(toKeep);
			for(int j = 0 ; j < numberOfSites ; j++) {
				if(keep[j]) {
					sb.append(a.getData(i,j));
				}
			}
			newSeqs[i] = sb.toString();
		}
		return new SimpleAlignment(a, newSeqs);
	}

	/**
	 * Returns true if the alignment has a gap at the site in the
	 * sequence specified.
	 */
	public static boolean isGap(Alignment a, int seq, int site) {
		return DataTypeUtils.isGap(a.getDataType(), a.getData(seq, site));
	}

	/** 
	 * @param startingCodonPosition from {0,1,2}, representing codon position 
	 * of first value in sequences...
   	 * @param translator the translator to use for converting codons to 
	 * amino acids.
	 * @param removeIncompleteCodons removes end codons that are not complete 
	 * (due to startingPosition, and sequence length).
	 */
	public static void getPositionMisalignmentInfo(Alignment a, PrintWriter
	out, int startingCodonPosition,  CodonTable translator, boolean removeIncompleteCodons) {
		int leftGaps, rightGaps; //The gaps to the left and right of the center nucleotide
		for(int i = 0 ; i < a.getSequenceCount() ; i++) {
			int codonPosition = startingCodonPosition;
			String codon = "";
			boolean first = true;
			out.print(a.getIdentifier(i)+":");
			leftGaps = 0;
			rightGaps = 0;
			for(int j = 0 ; j < a.getSiteCount() ; j++) {
				char c = a.getData(i, j);
				if(isGap(a, i, j)) {
					switch(codonPosition) {
						case 1: { leftGaps++; break; }
						case 2: { rightGaps++; break; }
						default: { out.print(c); break; }
					}
				} else {
					codon+=c;
					if(codonPosition==2) {
						if(!first||!(first&&codon.length()!=3&&removeIncompleteCodons)) {
							if(!first||(first&&startingCodonPosition==0)) {
								out.print('[');
							}
							outputChar(out,Alignment.GAP,leftGaps);
							out.print(translator.getAminoAcidChar(codon.toCharArray())); //Translator takes care of wrong length codons!S
							outputChar(out,Alignment.GAP,rightGaps); out.print(']');
						}
						first = false; codon = ""; leftGaps = 0; rightGaps = 0;
					}
					codonPosition = (codonPosition+1)%3;
				}
			}
			//If we finish on an incomplete codon (we ignore the case where a sequence is less than 3 nucleotides
			if(!removeIncompleteCodons && codonPosition!=0) {
				out.print('[');
				outputChar(out,Alignment.GAP,leftGaps);
				out.print('?');
				outputChar(out,Alignment.GAP,rightGaps);
			}
			out.print("\n");
		}
	}

	/**
		@param startingCodonPosition - from {0,1,2}, representing codon position of first value in sequences...
		@note uses middle nucelotide of code to display info...
   */
	public static void getPositionMisalignmentInfo(Alignment a, PrintWriter out, int startingCodonPosition) {
		for(int i = 0 ; i < a.getSequenceCount() ; i++) {
			int codonPosition = startingCodonPosition;
			out.print(a.getIdentifier(i)+":");
			for(int j = 0 ; j < a.getSiteCount(); j++) {
				char c = a.getData(i, j);
				if(isGap(a, i, j)) { 
					out.print(c);
				} 
				else {
					switch(codonPosition) {
						case 0 : { out.print('['); 	break; }
						case 1 : { out.print(c); break; }
						case 2 : { out.print(']'); break; }
					}
					codonPosition = (codonPosition+1)%3;
				}

			}
			out.print("\n");
		}
	}

	// PRIVATE METHODS

	private static final void outputChar(PrintWriter out, char c, int number) {
		for(int i = 0 ; i < number ; i++) {
			out.print(c);
		}
	}

	private static void printNextSites(Alignment a, PrintWriter out, boolean chunked, int seq, int start, int num)
	{
		// Print next num characters
		for (int i = 0; (i < num) && (start + i < a.getSiteCount()); i++)
		{
			// Chunks of 10 characters
			if (i % 10 == 0 && i != 0 && chunked)
			{
				out.print(' ');
			}
			out.print(a.getData(seq, start+i));
		}
	}

	/**
	 * Returns the gap creation costs between sequences x and y from site start to site finish.
	 * @param a alignment
	 * @param x first sequence
	 * @param y second sequence
	 * @param start first site to consider (inclusive)
	 * @param finish last site to consider (inclusive)
	 */
	private static int getNaturalGapCost(Alignment a, int x, int y, 
				  int start, int finish) {
	
		int totalCost = 0;
		boolean inGap = false;

		// get gap creation costs
		for (int i = start; i <= finish; i++) {
			// if not a gap in one of them then consider column for x
			if (!(isGap(a, y, i) && isGap(a, x, i))) {
				// if gap in x then its the start of gap or already in gap
				if (isGap(a, x, i)) {
					// if not in gap then new gap
					if (!inGap) {
						totalCost += 1;
						inGap = true;
					} // else in gap and no extra cost
				} else {
					// else not null in x therefore not in gap
					inGap = false;
				}
			}
		}

		return totalCost;
	}

//=================================================================
	private static final boolean isGoodSite(Alignment a, DataType dt, int site) {
		int numberOfSequences = a.getSequenceCount();
		for(int i = 0 ; i < numberOfSequences ; i++) {
			char c = a.getData(i,site);
			if(c!=Alignment.GAP&&dt.isUnknownState(dt.getState(c))) {
				return false;
			}
		}
		return true;
	}

	/** Returns an alignment which follows the pattern of the input alignment
			except that all sites which do not contain states in dt (excluding the
			gap character) are removed. The Datatype of the returned alignment is dt
	*/
	public static final Alignment getChangedDataType(Alignment a, DataType dt) {
		int numberOfSites = a.getSiteCount();
		boolean[] include = new boolean[numberOfSites];
		int goodSiteCount = 0;
		for(int i = 0 ; i < numberOfSites ; i++) {
			include[i] = isGoodSite(a,dt,i);
			if(include[i]) {
				goodSiteCount++;
			} 
		}
		//Yes, I'm aware it may be slightly faster to nest sequence
		// in site but it's easier to program this way
		String[] sequences = new String[a.getSequenceCount()];
		for(int i = 0 ; i < sequences.length ; i++) {
			char[] seq = new char[goodSiteCount];
			int count = 0;
			for(int j = 0 ; j < numberOfSites ; j++) {
				if(include[j]) {
					seq[count] = a.getData(i,j);
					count++;
				}
			}
			sequences[i] = new String(seq);
		}
		SimpleAlignment sa = new SimpleAlignment(new SimpleIdGroup(a), sequences);
		sa.setDataType(dt);
		return sa;
	}
	
	/**
	 * Returns the score of this alignment based on all pairwise distance measures.
	 * @param a alignment to score
	 * @param cdm the character distance matrix
	 * @param x 
	 */
	private static CostBag getAlignmentPenalty(Alignment a, 
		TransitionPenaltyTable penalties, 
		int x, int y, int[] xindices, int[] yindices,
		boolean local) {

		int start = 0;
		int finish = a.getSiteCount() - 1;
		if (local) {
			while (isGap(a, y, start) || isGap(a, x, start)) {
				start += 1;
			}
			while (isGap(a, y, finish) || isGap(a, x, finish)) {
				finish -= 1;
			}
		}

		// get gap costs (creation penalties)
		int gapCost = getNaturalGapCost(a, x, y, start, finish) +
			getNaturalGapCost(a, y, x, start, finish);
	
		int gapExCost = 0;

		double subCosts = 0.0;
		// get substitution costs (including extension penalties)
		for (int i = start; i <= finish; i++) {
			if (isGap(a, x, i) || isGap(a, y, i)) {
				if (a.getData(x, i) != a.getData(y, i)) {
					gapExCost += 1;
				}
			} else {
				subCosts += penalties.penalty(xindices[i], yindices[i]);
			}
		}

		return new CostBag(gapCost, gapExCost, subCosts);
	}

}

class CostBag {

	int gc = 0;
	int ge = 0;
	double substitutions = 0.0;

	public CostBag() {}

	public CostBag(int gc, int ge, double subs) {
		this.gc = gc;
		this.ge = ge;
		substitutions = subs;
	}

	public void add(CostBag bag) {
		gc += bag.gc;
		ge += bag.ge;
		substitutions += bag.substitutions;
	}

	public double score(double x, double y) {
		return substitutions + (gc * (x - y)) + (ge * y);
	}

	public double approximateIntegral(double x1, double x2, 
		double y1, double y2) {
		
		double dx = (x2 - x1) / 100.0;
		double dy = (y2 - y1) / 100.0;

		double total = 0.0;

		int count = 0;
		for (double x = x1; x <= x2; x += dx) {
			for (double y = y1; y <= y2; y += dy) {
				total += score(x, y);
				count += 1;
			}
		}

		total /= (double)count;

		return total * (x2 - x1) * (y2 - y1);
	}

}



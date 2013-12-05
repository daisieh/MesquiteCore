// F84.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;

import java.io.*;


/**
 * Felsenstein 1984 (PHYLIP) model of nucleotide evolution
 *
 * @version $Id: F84.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class F84 extends NucleotideModel implements Serializable
{
	/**
	 * constructor 1
	 *
	 * @param expectedTsTv expected transition-transversion ratio
	 * @param freq nucleotide frequencies
	 */
	public F84(double expectedTsTv, double[] freq)
	{
		super(freq);
		
		this.expectedTsTv = expectedTsTv;
		
		convertToTN();		
		makeTN();
		fromQToR();
		
		showSE = false;
	}
 
	/**
	 * Constructor 2
	 *
	 * @param params parameter list
	 * @param freq nucleotide frequencies
	 */
	public F84(double[] params, double[] freq)
	{
		this(params[0], freq);
	}

	// Get numerical code describing the model type
	public int getModelID()
	{
		return 3;
	}
 
 	// interface Report
 
	public void report(PrintWriter out)
	{
		out.println("Model of substitution: F84 (Felsenstein 1984, PHYLIP)");
		out.print("PHYLIP Transition/transversion parameter: ");
		format.displayDecimal(out, expectedTsTv, 2);
		if (showSE)
		{
			out.print("  (S.E. ");
			format.displayDecimal(out, expectedTsTvSE, 2);
			out.print(")");
		}
		out.println();

		out.println();
		printFrequencies(out);
		printRatios(out);
		out.println();
		out.println("This model corresponds to a Tamura-Nei (1993) model with");
		out.print(" Transition/transversion rate ratio kappa: ");
		format.displayDecimal(out, kappa, 2);
		out.println();
		out.print(" Y/R transition rate ratio: ");
		format.displayDecimal(out, r, 2);
		out.println();
		out.println("and the above nucleotide frequencies.");
		out.println();
	}	

	// interface Parameterized

	public int getNumParameters()
	{
		return 1;
	}
	
	public void setParameter(double param, int n)
	{
		expectedTsTv = param;

		convertToTN();
		makeTN();
		fromQToR();
	}

	public double getParameter(int n)
	{
		return expectedTsTv;
	}

	public void setParameterSE(double paramSE, int n)
	{
		expectedTsTvSE = paramSE;
	
		showSE = true;
	}

	public double getLowerLimit(int n)
	{
		return 0.0001;
	}
	
	public double getUpperLimit(int n)
	{
		return 100.0;
	}
	
	public double getDefaultValue(int n)
	{
		return 2.0;
	}


	//
	// Private stuff
	// 

	private boolean showSE;
	private double kappa, r;
	private double expectedTsTv, expectedTsTvSE;
	
	private void convertToTN()
	{
		double piA = frequency[0];
		double piC = frequency[1];
		double piG = frequency[2];
		double piT = frequency[3];
		double piR = piA + piG;
		double piY = piC + piT;
		
		double rho = (piR*piY*(piR*piY*expectedTsTv - (piA*piG + piC*piT)))/
			(piC*piT*piR + piA*piG*piY);
			
		kappa = 1.0 + 0.5*rho*(1.0/piR + 1.0/piY);
		r = (piY + rho)/piY * piR/(piR + rho);
	}

	// Make TN model
	private void makeTN()
	{
		// Q matrix
		rate[0][1] = 1; rate[0][2] = 2.0*kappa/(r+1.0); rate[0][3] = 1;
		rate[1][2] = 1; rate[1][3] = 2.0*kappa*r/(r+1.0);
		rate[2][3] = 1;
	}
}


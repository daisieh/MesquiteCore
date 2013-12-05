// TwoStateModel.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.substmodel;

import pal.misc.*;
import pal.datatype.*;

import java.io.*;


/**
 * implements the most general reversible rate matrix for two-state data
 *
 * @version $Id: TwoStateModel.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class TwoStateModel extends AbstractRateMatrix implements RateMatrix
{
	/**
	 * constructor
	 *
	 * @param f frequencies
	 *
	 * @return rate matrix
	 */
	public TwoStateModel(double[] f)
	{
		// Dimension = 2
		super(2);
		
		dataType = new TwoStates();
		
		setFrequencies(f);
		// Q matrix
		rate[0][1] = 1;
		fromQToR();
	}

	/**
	 * get numerical code describing the model type
	 *
	 * @return 0 (there is only one model)
	 */
	public int getModelID()
	{
		return 0;
	}
	
	/**
	 * create object using an instance method
	 *
	 * @param freq model frequencies
	 *
	 * @return rate matrix
	 */ 
	public static TwoStateModel getInstance(double[] freq)
	{
 		return new TwoStateModel(freq);
 	}
	
 	// interface Report

	public void report(PrintWriter out)
	{
		out.println("Model of substitution: Proportional Model (F81)");
		out.println();
		out.println("Frequencies of the two states:");
		printFrequencies(out);
	}
	
	// interface Parameterized
	
	public int getNumParameters()
	{
		return 0;
	}
	
	public void setParameter(double param, int n)
	{
		return;
	}

	public double getParameter(int n)
	{
		return 0.0;
	}

	public void setParameterSE(double paramSE, int n)
	{
		return;
	}

	public double getLowerLimit(int n)
	{
		return 0.0;
	}
	
	public double getUpperLimit(int n)
	{
		return 0.0;
	}
	
	public double getDefaultValue(int n)
	{
		return 0.0;
	}
}


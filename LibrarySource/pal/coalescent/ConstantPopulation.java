// ConstantPopulation.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
 
package pal.coalescent;

import pal.math.*;
import pal.misc.*;
import pal.io.*;

import java.io.*;

/**
 * This class models coalescent intervals for a constant population
 * (parameter: N0=present-day population size). <BR>
 * If time units are set to Units.EXPECTED_SUBSTITUTIONS then
 * the N0 parameter will be interpreted as N0 * mu. <BR>
 * Also note that if you are dealing with a diploid population
 * N0 will be out by a factor of 2.
 *
 * @version $Id: ConstantPopulation.java,v 1.12 2001/07/12 12:17:43 korbinian Exp $
 *
 * @author Alexei Drummond
 + @author Korbinian Strimmer
 */
public class ConstantPopulation extends DemographicModel implements Report, Summarizable, Parameterized, Serializable
{

	//
	// private stuff
	//
	/** The summary descriptor stuff for the public values of this
			class
			@see Summarizable, getSummaryDescriptors()
	*/
	private static final String[] CP_SUMMARY_TYPES = {"N0","N0SE"}; //This is still 1.0 compliant...

	//
	// Public stuff
	//

	/** population size */
	public double N0;

	/** standard error of population size */
	public double N0SE = 0.0;


	/**
	 * Construct demographic model with default settings
	 */
	public ConstantPopulation(int units) {
	
		super();
	
		setUnits(units);

		N0 = getDefaultValue(0);
		
		// necessary to conform to Java 1.0 language standard
	 //	SUMMARY_TYPES[0] = "N0";
		//SUMMARY_TYPES[1] = "N0SE";
	}


	/**
	 * Construct demographic model of a constant population size.
	 */
	public ConstantPopulation(double size, int units) {
	
		super();
	
		N0 = size;
		setUnits(units);


	}

	public Object clone()
	{
		return new ConstantPopulation(getN0(), getUnits()); 
	}

	public String[] getSummaryTypes() {
		return CP_SUMMARY_TYPES;
	}

	public double getSummaryValue(int summaryType) {
		switch(summaryType) {
			case 0 : {
				return N0;
			}
			case 1 : {
				return N0SE;
			}
		}
		throw new RuntimeException("Assertion error: unknown summary type :"+summaryType);
	}

	/**
	 * returns initial population size.
	 */
	public double getN0()
	{
		return N0;
	}

		
	// Implementation of abstract methods
	
	public double getDemographic(double t)
	{
		return N0;
	}

	public double getIntensity(double t)
	{
		return t/N0;
	}

	public double getInverseIntensity(double x)
	{
		return N0*x;
	}

	// Parameterized interface

	public int getNumParameters()
	{
		return 1;
	}
	
	public double getParameter(int k)
	{
		return N0;
	}

	public double getUpperLimit(int k)
	{
		return 1e50;
	}

	public double getLowerLimit(int k)
	{
		return 1e-12;
	}

	public double getDefaultValue(int k)
	{
		//arbitrary default values
		if (getUnits() == GENERATIONS) {
			return 1000.0;
		} else {
			return 0.2;
		}
	}

	public void setParameter(double value, int k)
	{
		N0 = value;
	}

	public void setParameterSE(double value, int k)
	{
		N0SE = value;
	}

	public String toString()
	{
		/*
		String s = 
			"Constant Population:\n";

		if (getUnits() == GENERATIONS) {
			s += "Effective Population Size = " + N0 + "\n";
		} else {
			s += "Theta (haploid) = " + (N0 * 2) + "\n";
		}
		return s;
		*/
				
		OutputTarget out = OutputTarget.openString();
		report(out);
		out.close();
		
		return out.getString();
	}
	
	public void report(PrintWriter out)
	{
		out.println("Demographic model: constant population size ");
		out.println("Demographic function: N(t) = N0");
		
		out.print("Unit of time: ");
		if (getUnits() == GENERATIONS)
		{
			out.print("generations");
		}
		else
		{
			out.print("expected substitutions");
		}
		out.println();
		out.println();
		out.println("Parameters of demographic function:");
		if (getUnits() == GENERATIONS)
		{
			out.print(" present day population size N0: ");
			fo.displayDecimal(out, N0, 6);
		}
		else
		{
			out.print(" present day Theta (N0 * mu): ");
			fo.displayDecimal(out, N0, 6);
		}
		if (N0SE != 0.0)
		{
			out.print(" (S.E. ");
			fo.displayDecimal(out, N0SE, 6);
			out.print(")");
		}	
		out.println();
		
		if (getLogL() != 0.0)
		{
			out.println();
			out.print("log L: ");
			fo.displayDecimal(out, getLogL(), 6);
			out.println();
		}
	}
}


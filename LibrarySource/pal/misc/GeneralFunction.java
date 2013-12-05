// GeneralFunction.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * interface for classes which provide general function mechanisms. Essentially mappings from a n-dimensional space to a scalar value
 *
 * @version $Id: GeneralFunction.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Matthew Goode
 */
 
public interface GeneralFunction {
	double compute(double[] parameters);
	GeneralFunction getGeneralFunctionCopy();
}


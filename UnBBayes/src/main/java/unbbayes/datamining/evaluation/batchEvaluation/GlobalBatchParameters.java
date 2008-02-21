/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.evaluation.batchEvaluation;

import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 17/01/2007
 */
public class GlobalBatchParameters {
	
	private static GlobalBatchParameters globalBatchParameters;
	
	
	
	/* ************************************************************************
	 ******* Smote options - Start ********************************************/
	
	/** Number of neighbors utilized in SMOTE */
	private static int numNN = 5;
	
	/**
	 * Set it <code>true</code> to optionDiscretize the synthetic value created for
	 * the new instance. 
	 */
	private static boolean optionDiscretize = false;
	
	/**
	 * Used in SMOTE
	 * 0: copy nominal attributes from the current instance
	 * 1: copy from the nearest neighbors
	 */
	private static byte optionNominal = 0;
	
	/**
	 * The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The optionFixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn for each attribute.
	 */
	private static boolean optionFixedGap = false;
	
	public static byte HAMMING = 0;

	public static byte HVDM = 1;
	
	/** 
	 * Distance function desired.
	 * <ul>
	 * <li> 0: Hamming
	 * <li> 1: HVDM
	 * </ul>
	 */
	private static byte optionDistanceFunction = HVDM;
	
	
	
	/* ***** Smote options - End **********************************************
	 *************************************************************************/
	

	
	
	/** Minimum accepted % change in each iteration in numeric clusterization */
	private static double kError = 1.001f;

	/**
	 * Default constructor. Can only be instanced by the method getInstance.
	 */
	private GlobalBatchParameters() {
	}

	/** 
	 * Returns an instance of this class. If the class was already instanced, this
	 * object is returned, otherwise a new instance is returned
	 *
	 *	@return the only instance of this class 
	 */
	public static GlobalBatchParameters getInstance() {
		if (globalBatchParameters == null) {
			globalBatchParameters = new GlobalBatchParameters();
		}
		
		return globalBatchParameters;
	}
	
	public void initializeSmote(Smote smote) {
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
	}

	public void setOptionDiscretize(boolean optionDiscretize) {
		GlobalBatchParameters.optionDiscretize = optionDiscretize;
	}

	public void setOptionFixedGap(boolean optionFixedGap) {
		GlobalBatchParameters.optionFixedGap = optionFixedGap;
	}

	public void setOptionNominal(byte optionNominal) {
		GlobalBatchParameters.optionNominal = optionNominal;
	}

	public void setKError(double kError) {
		GlobalBatchParameters.kError = kError;
	}

	public double getKError() {
		return kError;
	}

	public void setNumNearestNeighbors(int numNN) {
		GlobalBatchParameters.numNN = numNN;
	}

	public int getNumNearestNeighbors() {
		return numNN;
	}

	public void setOptionDistanceFunction(byte optionDistanceFunction) {
		GlobalBatchParameters.optionDistanceFunction = optionDistanceFunction;
	}

	public byte getOptionDistanceFunction() {
		return optionDistanceFunction;
	}


}
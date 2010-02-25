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

import java.io.Serializable;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 29/09/2007
 */
public class PreprocessorParameters implements Serializable {

	private static final long serialVersionUID = 1L;
	private int preprocessorID;
	private int ratioStart;
	private int ratioEnd;
	private int ratioStep;
	private int clustersStart;
	private int clustersEnd;
	private int clustersStep;
	private int oversamplingThresholdStart;
	private int oversamplingThresholdEnd;
	private int oversamplingThresholdStep;
	private int positiveThresholdStart;
	private int positiveThresholdEnd;
	private int positiveThresholdStep;
	private int negativeThresholdStart;
	private int negativeThresholdEnd;
	private int negativeThresholdStep;
	private int cleanType;
	private boolean active;
	
	public PreprocessorParameters(int preprocessorID) {
		ratioStart = 30;
		ratioEnd = 70;
		ratioStep = 10;
		clustersStart = 3;
		clustersEnd = 30;
		clustersStep = 10;
		oversamplingThresholdStart = 50;
		oversamplingThresholdEnd = 80;
		oversamplingThresholdStep = 10;
		positiveThresholdStart = 50;
		positiveThresholdEnd = 80;
		positiveThresholdStep = 10;
		negativeThresholdStart = 50;
		negativeThresholdEnd = 80;
		negativeThresholdStep = 10;
		cleanType = 2;
	}

	public void setPreprocessorID(int preprocessorID) {
		this.preprocessorID = preprocessorID;
	}

	public int getPreprocessorID() {
		return preprocessorID;
	}

	public void setRatioStart(int ratioStart) {
		this.ratioStart = ratioStart;
	}

	public int getRatioStart() {
		return ratioStart;
	}

	public void setRatioEnd(int ratioEnd) {
		this.ratioEnd = ratioEnd;
	}

	public int getRatioEnd() {
		return ratioEnd;
	}

	public void setRatioStep(int ratioStep) {
		this.ratioStep = ratioStep;
	}

	public int getRatioStep() {
		return ratioStep;
	}

	public void setClusterStart(int clustersStart) {
		this.clustersStart = clustersStart;
	}

	public int getClusterStart() {
		return clustersStart;
	}

	public void setClusterEnd(int clustersEnd) {
		this.clustersEnd = clustersEnd;
	}

	public int getClusterEnd() {
		return clustersEnd;
	}

	public void setClusterStep(int clustersStep) {
		this.clustersStep = clustersStep;
	}

	public int getClusterStep() {
		return clustersStep;
	}

	public void setOverThresholdStart(int oversamplingThresholdStart) {
		this.oversamplingThresholdStart = oversamplingThresholdStart;
	}

	public int getOverThresholdStart() {
		return oversamplingThresholdStart;
	}

	public void setOverThresholdEnd(int oversamplingThresholdEnd) {
		this.oversamplingThresholdEnd = oversamplingThresholdEnd;
	}

	public int getOverThresholdEnd() {
		return oversamplingThresholdEnd;
	}

	public void setOverThresholdStep(int oversamplingThresholdStep) {
		this.oversamplingThresholdStep = oversamplingThresholdStep;
	}

	public int getOverThresholdStep() {
		return oversamplingThresholdStep;
	}

	public void setPosThresholdStart(int positiveThresholdStart) {
		this.positiveThresholdStart = positiveThresholdStart;
	}

	public int getPosThresholdStart() {
		return positiveThresholdStart;
	}

	public void setPosThresholdEnd(int positiveThresholdEnd) {
		this.positiveThresholdEnd = positiveThresholdEnd;
	}

	public int getPosThresholdEnd() {
		return positiveThresholdEnd;
	}

	public void setPosThresholdStep(int positiveThresholdStep) {
		this.positiveThresholdStep = positiveThresholdStep;
	}

	public int getPosThresholdStep() {
		return positiveThresholdStep;
	}

	public void setNegThresholdStart(int negativeThresholdStart) {
		this.negativeThresholdStart = negativeThresholdStart;
	}

	public int getNegThresholdStart() {
		return negativeThresholdStart;
	}

	public void setNegThresholdEnd(int negativeThresholdEnd) {
		this.negativeThresholdEnd = negativeThresholdEnd;
	}

	public int getNegThresholdEnd() {
		return negativeThresholdEnd;
	}

	public void setNegThresholdStep(int negativeThresholdStep) {
		this.negativeThresholdStep = negativeThresholdStep;
	}

	public int getNegThresholdStep() {
		return negativeThresholdStep;
	}

	public void setCleanType(int cleanType) {
		this.cleanType = cleanType;
	}

	public int getCleanType() {
		return cleanType;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}


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
 */
package unbbayes.prs.mebn.ssbn;

import java.util.List;

import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Interface for the class that implements the SSBN Algorithm
 * 
 * @author Shou Matsumoto
 */
public interface ISSBNGenerator {

	/**
	 * Generate the SSBN from the list of queries and the knowledge base
	 * 
	 * @param listQueries List of the queries (don't empty)
	 * @param kb          KnowledgeBase populated with the entities and findings of the 
	 *                    specific situation. 
	 * 
	 * @return The SSBN generated. 
	 * 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 * @throws MEBNException 
	 * @throws OVInstanceFaultException 
	 * @throws InvalidParentException when the parent is the wrong type for the child.
	 */
	public SSBN generateSSBN(List<Query> listQueries, 
			KnowledgeBase kb) throws SSBNNodeGeneralException, 
			                         ImplementationRestrictionException, 
			                         MEBNException, 
			                         OVInstanceFaultException, 
			                         InvalidParentException;
	
	/**
	 * Indicates whether a log using {@link SSBN#getLogManager()} should be used or not.
	 * @param isEnabled
	 */
	public void setLogEnabled(boolean isEnabled);
	
	/**
	 * Indicates whether a log using {@link SSBN#getLogManager()} should be used or not.
	 * @return
	 */
	public boolean isLogEnabled();
	
	/**
	 * @return a number indicating how many iterations the last call of {@link #generateSSBN(List, KnowledgeBase)}
	 * did. This is just an indicator of performance for algorithms based on iterations.
	 */
	public int getLastIterationCount();
}

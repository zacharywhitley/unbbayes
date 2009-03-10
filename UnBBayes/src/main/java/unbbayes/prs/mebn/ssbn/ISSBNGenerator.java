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
	 * 
	 * @param query
	 * @return The SSBN generated. 
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
}

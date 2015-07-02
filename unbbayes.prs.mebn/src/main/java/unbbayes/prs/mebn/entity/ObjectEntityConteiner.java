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
package unbbayes.prs.mebn.entity;


/**
 * Contains the Object entities of a MEBN. 
 * 
 * Note: this container should not be a singleton, since an user might edit
 * two MTheories simultaneously; naturally, using different sets of entities.
 * 
 * @author Laecio Lima dos Santos
 * @author Shou Matsumoto
 * @version 1.1 02/25/2008
 * 
 * @deprecated use {@link ObjectEntityContainer} instead. This was not deleted yet simply to guarantee backward compatibility.
 */
@Deprecated
public class ObjectEntityConteiner extends ObjectEntityContainer {

	/**
	 *  @deprecated use {@link ObjectEntityContainer} instead
	 */
	@Deprecated
	public ObjectEntityConteiner(TypeContainer _typeConteiner) {
		super(_typeConteiner);
	}
}

/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 * OrClause.java
 *
 * Created on 15 February 2006, 13:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.sesat.search.query;


/** A special clause to distinguish between QueryParser's guesses to the specific LeafClause type.
 * By default the name of the hint indicates to what is on the left side, or the first child.
 * 
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface XorClause extends OrClause {

    /** The Hint give the neccesary programmatic hint to how the two branches differ. **/
    Hint getHint();

    enum Hint{
        FULLNAME_ON_LEFT,
        NUMBER_GROUP_ON_LEFT,
        PHRASE_ON_LEFT,
        PHONE_NUMBER_ON_LEFT,
        ROTATION_ALTERNATION
    }

}

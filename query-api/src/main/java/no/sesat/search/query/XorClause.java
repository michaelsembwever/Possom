/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
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

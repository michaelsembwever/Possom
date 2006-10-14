/* Copyright (2005-2006) Schibsted Søk AS
 * OrClause.java
 *
 * Created on 15 February 2006, 13:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.query;


/** A special clause to distinguish between QueryParser's guesses to the specific LeafClause type.
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface XorClause extends OrClause {
    
    /** The Hint give the neccesary programmatic hint to how the two branches differ. **/
    Hint getHint();

    enum Hint{ 
        PHRASE_ON_LEFT,
        PHONE_NUMBER_ON_LEFT,
        NUMBER_GROUP_ON_LEFT,
        ROTATION_ALTERNATION
    }

}

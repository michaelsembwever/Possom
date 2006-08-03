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
    Hint getHint();
    
    Hint PHRASE_ON_LEFT = new Hint(){};
    Hint PHONE_NUMBER_ON_LEFT = new Hint(){};
    Hint ORGANISATION_NUMBER_ON_LEFT = new Hint(){};
    Hint ROTATION_ALTERNATION = new Hint(){};
    
    interface Hint{}
}

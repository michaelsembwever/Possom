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
    /** TODO comment me. **/
    Hint getHint();

    /** TODO comment me. **/
    Hint PHRASE_ON_LEFT = new Hint(){};
    /** TODO comment me. **/
    Hint PHONE_NUMBER_ON_LEFT = new Hint(){};
    /** TODO comment me. **/
    Hint NUMBER_GROUP_ON_LEFT = new Hint(){};
    /** TODO comment me. **/
    Hint ROTATION_ALTERNATION = new Hint(){};

    interface Hint{}
}

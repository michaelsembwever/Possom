/* Copyright (2007) Schibsted Søk AS
 * FullnameAlternation.java
 *
 *
 */

package no.schibstedsok.searchportal.query.parser.alt;


import java.util.LinkedList;
import java.util.Set;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.XorClause.Hint;
import no.schibstedsok.searchportal.query.XorClause.Hint;
import no.schibstedsok.searchportal.query.finder.Counter;
import no.schibstedsok.searchportal.query.finder.PredicateFinder;
import no.schibstedsok.searchportal.query.parser.alt.AbstractAlternation;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;

/** SEARCH-597 Forbedringer av proximity på navn.
 * The results of this alternation depend on the skin's FULLNAME list.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class FullnameAlternation extends AbstractAlternation {
    
    // Constants -----------------------------------------------------  
    
    private static final Logger LOG = Logger.getLogger(FullnameAlternation.class);
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of FullnameAlternation 
     * @param cxt 
     */
    public FullnameAlternation(final Context cxt) {
        super(cxt);
    }

    /** {@inherit} **/
    public Clause alternate(final Clause clause) {
        
        final PredicateFinder finder = new PredicateFinder();
        
        final Set<Clause> fullnames = finder.findClauses(
                clause, 
                TokenPredicate.FULLNAME, 
                context.getTokenEvaluationEngine());
        
        Clause result = clause;
        
        for(Clause fullname : fullnames){
            
            if( fullname instanceof DefaultOperatorClause ){
                
                // find the right-leaning equilivalent of this fullname 
                //  since the presumption is that the surname is on the right hand side
                
                DefaultOperatorClause rightLeaningFullname = null;
                for(Clause fn : fullnames){
                    if( fn.getTerm().replaceAll("\\(|\\)", "").equals(fullname.getTerm().replaceAll("\\(|\\)", "")) ){
                        rightLeaningFullname = (DefaultOperatorClause) fn;
                        //break;
                    }
                }
                
                final DefaultOperatorClause doFullname = (DefaultOperatorClause)fullname;
                
                // determine which children make up da lastname (we'll take da largest & furthest right fullname found)
//                final Set<Clause> lastnames = finder.findClauses(
//                        rightLeaningFullname, 
//                        TokenPredicate.LASTNAME, 
//                        context.getTokenEvaluationEngine());
                
                Clause surname = finder.findFirstClause(
                        rightLeaningFullname, 
                        TokenPredicate.LASTNAME, 
                        context.getTokenEvaluationEngine());
//                for(Clause lastname : lastnames){
//                    if( null != surname ){
//                        surname = new Counter().getTermCount(lastname) == new Counter().getTermCount(surname)
//                            ? lastname
//                            : surname;
//                    }else{
//                        surname = lastname;
//                    }
//                }
                
                if(null == surname){
                    // default to second clause
                    surname = rightLeaningFullname.getSecondClause();
                }
                
                final DefaultOperatorClause surnameParent 
                        = (DefaultOperatorClause)context.getParentFinder().getParent(rightLeaningFullname, surname);
                
                if(rightLeaningFullname.getSecondClause() != surname){
                    // surname must be on the right side
                    surname = rightLeaningFullname.getSecondClause();
                }
                LOG.debug("surname detected as " + surname);
                
                // and the clause that comprises all givennames
                final Clause givennames = rightLeaningFullname.getFirstClause();
                LOG.debug("givennames detected as " + givennames);
                
                // create fullname phrase clause
                final PhraseClause fullnamePhrased = context
                        .createPhraseClause('\"' + givennames.getTerm() + ' ' + surname.getTerm() + '\"', null);
                LOG.debug("fullname phraseClause created " + fullnamePhrased);
                
                // create reversed fullname phrase clause
                final PhraseClause reversedPhrased = context
                        .createPhraseClause('\"' + surname.getTerm() + ' ' + givennames.getTerm() + '\"', null);
                LOG.debug("reversed phraseClause created " + reversedPhrased);
                
                // create the OR emcompassing both fullname versions
                final OrClause orPhrasedClause = context.createOrClause(fullnamePhrased, reversedPhrased);
                LOG.debug("orClause created " + orPhrasedClause);
                
                // create the OR emcompassing the fullname versions and the original unphrased fullnames
                final OrClause orClause = context.createOrClause(orPhrasedClause, doFullname);
                LOG.debug("orClause created " + orClause);
                
                // create xorClause
                final LinkedList<Clause> list = new LinkedList<Clause>();
                list.add(doFullname);
                list.add(orClause);
                final XorClause xorClause = createXorClause(list);
                LOG.debug("XorClause created " + xorClause);
                
                // replace fullname with the new xorClause
                final DoubleOperatorClause fullnameParent = result == fullname
                        ? doFullname
                        : (DoubleOperatorClause) context.getParentFinder().getParent(result, fullname);
                
                result = result == fullname
                        ? xorClause
                        : replaceDescendant((DoubleOperatorClause)result, xorClause, doFullname, fullnameParent);
                LOG.debug("Updated root to " + result);
            }
        }
        
        return result;
    }
    
    // Public --------------------------------------------------------
    
    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    
    /** {@inherit} **/
    @Override
    protected Hint getAlternationHint() {
        return Hint.FULLNAME_ON_LEFT;
    }
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}

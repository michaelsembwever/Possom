/*
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
import no.schibstedsok.searchportal.query.finder.PredicateFinder;
import no.schibstedsok.searchportal.query.parser.alt.AbstractAlternation;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;

/** SEARCH-597 Forbedringer av proximity på navn
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
    
    /** Creates a new instance of FullnameAlternation */
    public FullnameAlternation(final Context cxt) {
        super(cxt);
    }

    public Clause alternate(final Clause clause) {
        
        final PredicateFinder finder = new PredicateFinder();
        
        final Set<Clause> fullnames = finder.findClauses(
                clause, 
                TokenPredicate.FULLNAME, 
                context.getTokenEvaluationEngine());
        
        Clause result = clause;
        
        for(Clause fullname : fullnames){
            
            if( fullname instanceof DefaultOperatorClause ){
                
                final DefaultOperatorClause doFullname = (DefaultOperatorClause)fullname;
                
                final DoubleOperatorClause fullnameParent = result == fullname
                        ? doFullname
                        : (DoubleOperatorClause) context.getParentFinder().getParent(result, fullname);
                
                // determine which children make up the lastname (we'll take the largest fullname found)
                Clause surname 
                        = finder.findFirstClause(fullname, TokenPredicate.LASTNAME, context.getTokenEvaluationEngine());
                if(null == surname){
                    // default to second clause
                    surname = doFullname.getSecondClause();
                }
                
                final DefaultOperatorClause surnameParent 
                        = (DefaultOperatorClause)context.getParentFinder().getParent(fullname, surname);
                
                if(surnameParent.getSecondClause() != surname){
                    // surname must be on the right side
                    surname = surnameParent.getSecondClause();
                }
                LOG.debug("surname detected as " + surname);
                
                // and the clause that comprises all givennames
                final Clause givennames = surnameParent.getFirstClause();
                LOG.debug("givennames detected as " + givennames);
                
                // create fullname phrase clause
                final PhraseClause fullnamePhrased = context
                        .createPhraseClause("\"" + givennames.getTerm() + ' ' + surname.getTerm() + "\"", null);
                LOG.debug("fullname phraseClause created " + fullnamePhrased);
                
                // create reversed fullname phrase clause
                final PhraseClause reversedPhrased = context
                        .createPhraseClause("\"" + surname.getTerm() + ' ' + givennames.getTerm() + "\"", null);
                LOG.debug("reversed phraseClause created " + reversedPhrased);
                
                // create the OR emcompassing both fullname versions
                final OrClause orClause = context.createOrClause(fullnamePhrased, reversedPhrased);
                LOG.debug("orClause created " + orClause);
                
                // create xorClause
                final LinkedList<Clause> list = new LinkedList<Clause>();
                list.add(doFullname);
                list.add(orClause);
                final XorClause xorClause = createXorClause(list);
                LOG.debug("XorClause created " + xorClause);
                
                // replace fullname with the new xorClause
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
    

    protected Hint getAlternationHint() {
        return Hint.FULLNAME_ON_LEFT;
    }
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}

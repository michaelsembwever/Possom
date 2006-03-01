// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;



import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class PrefixRemoverTransformer extends AbstractQueryTransformer {
    
   private static final Collection defaultPrefixes = Collections.unmodifiableCollection(
           Arrays.asList( 
               new TokenPredicate[] {
                TokenPredicate.SITEPREFIX,
                TokenPredicate.CATALOGUEPREFIX,
                TokenPredicate.PICTUREPREFIX,
                //TokenPredicate.NEWSPREFIX,
                TokenPredicate.WIKIPEDIAPREFIX,
                //TokenPredicate.TVPREFIX,
                TokenPredicate.WEATHERPREFIX
        }));

    private Collection/*<String>*/ prefixes = new ArrayList/*<String>*/();
    private Collection/*<TokenPredicate>*/ customPrefixes ;

    private static final String BLANK = "";

    public void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    
    public void visitImpl(final PhraseClause clause) {
        // don't remove prefix if it is infact a phrase.
    }
    
    public void visitImpl(final LeafClause clause) {
        if (clause == getContext().getQuery().getFirstLeafClause()) {
            for (final Iterator iterator = getPrefixesIterator(); iterator.hasNext();) {
                final TokenPredicate predicate = (TokenPredicate) iterator.next();
                if (clause.getPossiblePredicates().contains(predicate)
                        || clause.getKnownPredicates().contains(predicate)) {

                    getContext().getTransformedTerms().put(clause, BLANK);
                    return;
                }
            }

        }
    }
    
    private Iterator getPrefixesIterator(){
        synchronized( this ){
            if( customPrefixes == null && prefixes != null && prefixes.size() >0 ){
                final Collection/*<TokenPredicate>*/ cp = new ArrayList();
                for (final Iterator iterator = prefixes.iterator(); iterator.hasNext();) {
                    cp.add( TokenPredicate.valueOf( (String) iterator.next() ) );
                }
                customPrefixes = Collections.unmodifiableCollection(cp);
            }
        }
        return (prefixes != null && prefixes.size() >0
                ? customPrefixes
                : defaultPrefixes).iterator();
    }
}
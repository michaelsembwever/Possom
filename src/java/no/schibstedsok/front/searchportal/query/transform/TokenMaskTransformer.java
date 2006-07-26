// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.RegExpTokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;

/**
 *
 *
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@sesam.no">Mick Wever</a>
 * @version <tt>$Id$</tt>
 */
public final class TokenMaskTransformer extends AbstractQueryTransformer {

    /** TODO comment me. **/
    public enum Match {
        /** TODO comment me. **/
        PREFIX,
        /** TODO comment me. **/
        ANY
    };

    /** TODO comment me. **/
    public enum Mask {
        /** TODO comment me. **/
        INCLUDE,
        /** TODO comment me. **/
        EXCLUDE
    };

    private static final Logger LOG = Logger.getLogger(TokenMaskTransformer.class);

    private static final Collection<TokenPredicate> DEFAULT_PREFIXES = Collections.unmodifiableCollection(
            Arrays.asList(
            // Special case
            TokenPredicate.SITEPREFIX,
            // All magic words
            TokenPredicate.BOOK_MAGIC,
            TokenPredicate.CATALOGUE_MAGIC,
            TokenPredicate.CULTURE_MAGIC,
            TokenPredicate.MOVIE_MAGIC,
            TokenPredicate.NEWS_MAGIC,
            TokenPredicate.PICTURE_MAGIC,
            TokenPredicate.STOCK_MAGIC,
            TokenPredicate.WEBTV_MAGIC,
            TokenPredicate.WIKIPEDIA_MAGIC
            ));


    private static final String BLANK = "";

    private Collection<String> prefixes = new ArrayList<String>();
    private Collection<TokenPredicate> customPrefixes;
    private Match match = Match.ANY;
    private Mask mask = Mask.EXCLUDE;

    private Set<TokenPredicate> insidePrefixes = new HashSet<TokenPredicate>();
    private StringBuilder prefixBuilder = new StringBuilder();
    private Map<LeafClause,String> leaves = new HashMap<LeafClause,String>();
    private RegExpEvaluatorFactory regExpFactory = null;

    private static final String ERR_PREFIX_NOT_FOUND = "No such TokenPredicate ";

    /** TODO comment me. **/
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {

        // -->HACK while AlternationRotation is in implementation
        for (TokenPredicate predicate : getPrefixes()) {
            if (clause.getPossiblePredicates().contains(predicate)) {
                insidePrefixes.add(predicate);
            }
        }
        clause.getFirstClause().accept(this);

        if(insidePrefixes.size() > 0
        // <--HACK
                || Match.ANY == match || Mask.INCLUDE == mask){
            clause.getSecondClause().accept(this);
        }
    }

    /** TODO comment me. **/
    protected void visitImpl(final PhraseClause clause) {
        // don't remove prefix if it is infact a phrase.
    }

    /** TODO comment me. **/
    protected void visitImpl(final LeafClause clause) {

        // Mask.INCLUDE masks out everything by default
        final String transformedTerm = getContext().getTransformedTerms().get(clause);
        if(Mask.INCLUDE == mask){
            getContext().getTransformedTerms().put(clause, BLANK);
        }

        // Do not remove if the query is just the prefix.
        if (getContext().getQuery().getTermCount() > 1) {

            // -->HACK while AlternationRotation is in implementation
            if(insidePrefixes.size() > 0){

                if(clause.getField() != null){
                    clearInsidePrefixState();
                }else{
                    if(prefixBuilder.length()>0){
                        prefixBuilder.append(' ');
                    }
                    prefixBuilder.append(clause.getTerm());
                    leaves.put(clause, getContext().getTransformedTerms().get(clause));
                }
                for(TokenPredicate predicate : insidePrefixes){

                    final TokenEvaluator eval = regExpFactory.getEvaluator(predicate);
                    // HACK. if it isn't a RegExpTokenEvaluator it won't remove the prefix.
                    if(eval instanceof RegExpTokenEvaluator
                            && ((RegExpTokenEvaluator)eval).evaluateToken(null, prefixBuilder.toString(), null, true)){

                        for(LeafClause c : leaves.keySet()){
                            getContext().getTransformedTerms().put(c, Mask.INCLUDE == mask ? leaves.get(c) : BLANK);
                        }
                    }
                }
            }
            // <--HACK

            boolean check = Match.ANY == match;
            check |= Match.PREFIX == match && clause == getContext().getQuery().getFirstLeafClause();

            if (check) {
                for (TokenPredicate predicate : getPrefixes()) {

                    boolean transform = clause.getPossiblePredicates().contains(predicate);
                    transform &= predicate.evaluate(getContext().getTokenEvaluatorFactory()); // XXX maybe not needed
                    transform |= clause.getKnownPredicates().contains(predicate);

                    if (transform) {

                        getContext().getTransformedTerms().put(clause, Mask.INCLUDE == mask ? transformedTerm : BLANK);
                        return;
                    }
                }
            }
        }
    }

    private void clearInsidePrefixState(){
        // reset. not that it will be used again anyway ;-)
        insidePrefixes.clear();
        prefixBuilder.setLength(0);
        leaves.clear();
    }

    private Collection<TokenPredicate> getPrefixes() {
        synchronized (this) {
            if (customPrefixes == null && prefixes != null && prefixes.size() > 0) {
                final Collection<TokenPredicate> cp = new ArrayList(DEFAULT_PREFIXES);
                for (String tp : prefixes) {
                    try{
                        cp.add(TokenPredicate.valueOf(tp));
                    }catch(IllegalArgumentException iae){
                        LOG.error(ERR_PREFIX_NOT_FOUND + tp, iae);
                    }
                }
                customPrefixes = Collections.unmodifiableCollection(cp);
            }
        }
        return prefixes != null && prefixes.size() > 0
                ? customPrefixes
                : DEFAULT_PREFIXES;
    }

    /** TODO comment me. **/
    public Object clone() throws CloneNotSupportedException {

        final TokenMaskTransformer retValue = (TokenMaskTransformer)super.clone();
        retValue.customPrefixes = customPrefixes;

        retValue.prefixes = prefixes;
        retValue.insidePrefixes = new HashSet<TokenPredicate>();
        retValue.prefixBuilder = new StringBuilder();
        retValue.leaves = new HashMap<LeafClause,String>();
        retValue.match = match;
        retValue.mask = mask;

        return retValue;
    }

    /** TODO comment me. **/
    public void setContext(final QueryTransformer.Context cxt) {

        super.setContext(cxt);

        final RegExpEvaluatorFactory.Context regExpEvalFactory = ContextWrapper.wrap(
                RegExpEvaluatorFactory.Context.class, cxt);

        regExpFactory = RegExpEvaluatorFactory.valueOf(regExpEvalFactory);
    }

    /** TODO comment me. **/
    public void addPrefixes(final String[] pArr) {

        if(pArr.length > 0 && pArr[0].trim().length() >0){
            prefixes.addAll(Arrays.asList(pArr));
        }
    }



    /**
     * Getter for property match.
     * @return Value of property match.
     */
    public Match getMatch() {
        return match;
    }

    /**
     * Setter for property match.
     * @param match New value of property match.
     */
    public void setMatch(final Match match) {
        this.match = match;
    }



    /**
     * Getter for property mask.
     * @return Value of property mask.
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Setter for property mask.
     * @param mask New value of property mask.
     */
    public void setMask(final Mask mask) {
        this.mask = mask;
    }


}
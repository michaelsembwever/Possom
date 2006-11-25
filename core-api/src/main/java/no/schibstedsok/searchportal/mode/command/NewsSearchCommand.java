// Copyright (2006) Schibsted Søk AS
/*
 * NewsSearchCommand.java
 *
 * Created on March 7, 2006, 5:31 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.Map;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.token.TokenPredicate;

/**
 *
 * @author magnuse
 * @version $Id$
 */
public class NewsSearchCommand extends FastSearchCommand {

    // Filter used to get all articles.
    private static final String FAST_SIZE_HACK = " +size:>0";

    /** Creates a new instance of NewsSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public NewsSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    private StringBuilder filterBuilder = null;

    /**
     *
     * @param clause The clause to examine.
     */
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {
        switch(clause.getHint()){
            case PHRASE_ON_LEFT:
                // Web searches should use phrases over separate words.
                clause.getFirstClause().accept(visitor);
                break;
            default:
                // All other high level clauses are ignored.
                clause.getSecondClause().accept(visitor);
                break;
        }
    }

    /**
     * LeafClause
     *
     * A leaf clause with a site field does not add anything to the query. Also
     * if the query just contains the prefix do not output anything.
     *
     */
    protected void visitImpl(final LeafClause clause) {
        if (!  containsJustThePrefix() ) {
            super.visitImpl(clause);
        }
    }

    protected String getAdditionalFilter() {
        synchronized (this) {
            if (filterBuilder == null) {
                filterBuilder = new StringBuilder(super.getAdditionalFilter());

                // Add filter to retrieve all documents.
                if (containsJustThePrefix() || getTransformedQuery().equals("")) {
                    filterBuilder.append(FAST_SIZE_HACK);
                }

                if (!getSearchConfiguration().isIgnoreNavigation()) {

                    final String contentSource = getParameter("contentsource");
                    final String newsCountry = getParameter("newscountry");

                    // AAhhrghh. Need to provide backwards compatibility.
                    // People are linking us using contentsource="Norske nyheter"
                    if (contentSource != null && !contentSource.equals("")) {
                        if (contentSource.equals("Norske nyheter")) {
                            filterBuilder.append(" +newscountry:Norge");
                        } else {
                            filterBuilder.append(" +contentsource:"+ contentSource);
                        }
                    }
                    if (newsCountry != null && !newsCountry.equals("")) {
                        filterBuilder.append(" +newscountry:"+ newsCountry);
                    }
                }
            }
        }

        return filterBuilder.toString();
    }

    private boolean containsJustThePrefix() {

        final LeafClause firstLeaf = context.getQuery().getFirstLeafClause();

        return context.getQuery().getRootClause() == firstLeaf
          && (firstLeaf.getKnownPredicates().contains(TokenPredicate.NEWS_MAGIC)
              || firstLeaf.getPossiblePredicates().contains(TokenPredicate.NEWS_MAGIC));
    }

}

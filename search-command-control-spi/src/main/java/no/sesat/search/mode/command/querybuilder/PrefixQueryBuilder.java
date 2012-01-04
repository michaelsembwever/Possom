/*
 * Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.mode.command.querybuilder;

import java.util.Collection;
import java.util.HashSet;
import no.sesat.search.mode.config.querybuilder.PrefixQueryBuilderConfig;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.EmailClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.UrlClause;

/** QueryBuilder prefixing terms depending on their inclusion/exclusion.
 *
 * @todo grouping doesn't work due to a dependence on the lean of the binary tree.
 *
 * @version $Id$
 */
public class PrefixQueryBuilder extends AbstractQueryBuilder{

    // Constants -----------------------------------------------------

    protected enum PrefixState {
        AND, NOT, OR, DEFAULT
    };

    //private static final Logger LOG = Logger.getLogger(PrefixQueryBuilder.class);

    // Attributes ----------------------------------------------------

    private PrefixState state = PrefixState.DEFAULT;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public PrefixQueryBuilder(final Context cxt, final QueryBuilderConfig config) {
        super(cxt, config);
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------


    @Override
    protected PrefixQueryBuilderConfig getConfig() {
        return (PrefixQueryBuilderConfig) super.getConfig();
    }

    @Override
    protected Collection<String> getWordsToEscape() {

        final Collection<String> words = new HashSet<String>(super.getWordsToEscape());

        if(!getConfig().getAndPrefix().isEmpty()){ words.add(getConfig().getAndPrefix()); }
        if(!getConfig().getNotPrefix().isEmpty()){ words.add(getConfig().getNotPrefix()); }
        if(!getConfig().getOrPrefix().isEmpty()){  words.add(getConfig().getOrPrefix()); }

        return words;
    }

    protected void visitImpl(final LeafClause clause) {

        if(!isEmptyLeaf(clause)){
            insertClauseStatePrefix();
            appendToQueryRepresentation(getEscapedTransformedTerm(clause));
        }
    }

    /**
     * Adds quotes around the Email address and hard escape everything.
     * Since there is the @ symbols in email addresses.
     *
     * @param clause The email clause.
     */
    protected void visitImpl(final EmailClause clause) {

        if(!isEmptyLeaf(clause)){
            insertClauseStatePrefix();
            appendToQueryRepresentation(getContext().escape(getContext().getTransformedTerm(clause).toLowerCase()));
        }
    }

    /**
     * Adds quotes around the URL and hard escape everything.
     * Since there are so many symbols in URLs.
     *
     * @param clause The url clause.
     */
    protected void visitImpl(final UrlClause clause) {

        if(!isEmptyLeaf(clause)){
            insertClauseStatePrefix();
            appendToQueryRepresentation(getContext().escape(getContext().getTransformedTerm(clause).toLowerCase()));
        }
    }

    protected void visitImpl(final AndClause clause) {

        if (!isEmptyLeaf(clause)) {
            state = PrefixState.AND;
            if(getConfig().getAndGrouped()){ appendToQueryRepresentation(getConfig().getAndGroupOpen()); }
            clause.getFirstClause().accept(this);
            appendToQueryRepresentation(' ');
            state = PrefixState.AND;
            clause.getSecondClause().accept(this);
            if(getConfig().getAndGrouped()){ appendToQueryRepresentation(getConfig().getAndGroupClose()); }
        }
    }

    protected void visitImpl(final OrClause clause) {

        if (!isEmptyLeaf(clause)) {
            state = PrefixState.OR;
            if(getConfig().getOrGrouped()){ appendToQueryRepresentation(getConfig().getOrGroupOpen()); }
            clause.getFirstClause().accept(this);
            appendToQueryRepresentation(' ');
            state = PrefixState.OR;
            clause.getSecondClause().accept(this);
            if(getConfig().getOrGrouped()){ appendToQueryRepresentation(getConfig().getOrGroupClose()); }
        }
    }

    protected void visitImpl(final DefaultOperatorClause clause) {

        if (!isEmptyLeaf(clause)) {
            state = PrefixState.DEFAULT;
            if(getConfig().getDefaultGrouped()){ appendToQueryRepresentation(getConfig().getDefaultGroupOpen()); }
            clause.getFirstClause().accept(this);
            appendToQueryRepresentation(' ');
            state = PrefixState.DEFAULT;
            clause.getSecondClause().accept(this);
            if(getConfig().getDefaultGrouped()){ appendToQueryRepresentation(getConfig().getDefaultGroupClose()); }
        }
    }

    protected void visitImpl(final NotClause clause) {

        if(getConfig().getSupportsNot() && !isEmptyLeaf(clause)) {
            state = PrefixState.NOT;
            if(getConfig().getNotGrouped()){ appendToQueryRepresentation(getConfig().getNotGroupOpen()); }
            clause.getFirstClause().accept(this);
            if(getConfig().getNotGrouped()){ appendToQueryRepresentation(getConfig().getNotGroupClose()); }
        }
    }

    protected PrefixState getPrefixState(){
        return state;
    }

    // Private -------------------------------------------------------

    private void insertClauseStatePrefix(){


        switch(state){
            case AND:
                appendToQueryRepresentation(getConfig().getAndPrefix());
                break;
            case NOT:
                appendToQueryRepresentation(getConfig().getNotPrefix());
                break;
            case OR:
                appendToQueryRepresentation(getConfig().getOrPrefix());
                break;
            case DEFAULT:
            default:
                appendToQueryRepresentation(getConfig().getDefaultPrefix());
                break;
        }
    }

    // Inner classes -------------------------------------------------
}

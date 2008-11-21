/*
 * Copyright (2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.mode.command.querybuilder;

import java.util.StringTokenizer;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.BinaryOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.PhraseClause;
import org.apache.log4j.Logger;

/**
 * Visitor to create the filter string.
 *
 * Extracts filters from the query that have been defined,
 *  in modes.xml//modes/mode/search-command@field-filters,
 *  to be passed through to the backend.
 *
 * The filter are removed from the QueryBuilder.getQueryString due to the InitialisationQueryTransformer.
 *
 * For example handles the site: syntax.
 *
 * The default output uses a colon : to join field to the term, and prefixes the pair with the plus sign +
 * For example: +site:vg.no
 *
 * The " +" is defined as the deliminator, and is provided by getDelim() if a subclass wishes to alter just this.
 *
 * @todo add correct handling of NotClause and AndNotClause.
 * This also needs to be added to the query builder visitor above.
 *
 * @todo design for polymorphism and push out fast specifics to appropriate subclass.
 *
 * @version $Id$
 */
public class BaseFilterBuilder extends AbstractQueryBuilder implements FilterBuilder{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(BaseFilterBuilder.class);

    private static final String DELIM = " +";

    // Attributes ----------------------------------------------------

    private final StringBuilder additionalFilters = new StringBuilder();

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public BaseFilterBuilder(final Context cxt, final QueryBuilderConfig config) {
        super(cxt, config);
    }

    // Public --------------------------------------------------------

    /**
     * @deprecated use getFilterString() instead
     * @return
     */
    final String getFilter() {
        return getFilterString();
    }

    /** Default format is to apend filters like "field:value".
     *
     * {@inheritDoc}
     */
    public void addFilter(final String field, final String value) {

        additionalFilters.append(field + ':'+ value);
    }

    public String getFilterString() {

        return getQueryString()
                + (additionalFilters.length() > 0 ? ' ' : "")
                + additionalFilters.toString();
    }

    /** {@inheritDoc}
     *
     * When looking for a matching filter any leading QueryParser.OPERATORS
     * to field names in the filter string are ignored.
     *
     * @param string {@inheritDoc}
     * @return {@inheritDoc}
     */
    public String getFilter(final String string) {

        final StringBuilder sb = new StringBuilder();

        final StringTokenizer tokeniser = new StringTokenizer(getFilterString(), getDelim());

        if(tokeniser.hasMoreTokens()){

            while(tokeniser.hasMoreTokens()){

                final String[] pair = tokeniser.nextToken().split(":");

                if(pair[0].equals(string)){

                    if(sb.length() > 0){ sb.append(' '); }
                    sb.append(pair[1]);

                }else if(null == string && 1 == pair.length){

                    if(sb.length() > 0){ sb.append(' '); }
                    sb.append(pair[0]);

                }
            }
        }

        return sb.toString();
    }


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected void visitImpl(final LeafClause clause) {

        if (!isEmptyLeaf(clause)) {
            appendFilter(clause);
        }
    }

    protected void visitImpl(final PhraseClause clause) {
        final String field = getContext().getFieldFilter(clause);
        if (null != field) {
            appendFilter(clause);
        }
    }

    protected void visitImpl(final BinaryOperatorClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    protected void visitImpl(final NotClause clause) {
    }

    protected void visitImpl(final AndNotClause clause) {
    }

    protected void appendFilter(final LeafClause clause) {

        final String fieldAs = getContext().getFieldFilter(clause);
        final String term = clause.getTerm();

        appendToQueryRepresentation(getDelim() + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));
    }

    /** Override logic. Almost an inversion since the QueryBuilder hides fielded filters
     * and the FilterBuilder includes them.
     *
     * {@inheritDoc}
     * @param clause {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected boolean isEmptyLeaf(final Clause clause) {

        return clause instanceof LeafClause
                ? null == ((LeafClause)clause).getTerm() || null == getContext().getFieldFilter((LeafClause)clause)
                : super.isEmptyLeaf(clause);

    }

    protected String getDelim(){
        return DELIM;
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------


}

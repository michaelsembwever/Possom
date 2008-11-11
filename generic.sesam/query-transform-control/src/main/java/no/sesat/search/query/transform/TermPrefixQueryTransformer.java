/*
 * Copyright (2005-2008) Schibsted Søk AS
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
package no.sesat.search.query.transform;

import java.util.Map;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.EmailClause;
import no.sesat.search.query.IntegerClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.PhoneNumberClause;
import no.sesat.search.query.UrlClause;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * @see TermPrefixQueryTransformerConfig
 * @version $Id$
 */
public final class TermPrefixQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(TermPrefixQueryTransformer.class);

    private final TermPrefixQueryTransformerConfig config;

    /** Default constructor for QueryTransformers.
     *
     * @param config matching configuration class
     */
    public TermPrefixQueryTransformer(final QueryTransformerConfig config){
        this.config = (TermPrefixQueryTransformerConfig)config;
    }

    /**
     * This is th default fallback. Adds the prefix in the <code>prefix</code>
     * property
     *
     * @param clause The clause to prefix.
     */
     public void visitImpl(final LeafClause clause) {

        if (clause.getField() == null || getContext().getFieldFilter(clause) == null) {
            addPrefix(clause, config.getPrefix());
        }
    }

    /**
     * Add prefix to an integer clause.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final IntegerClause clause) {
        addPrefix(clause, config.getNumberPrefix());
    }

    /**
     * Add prefixes to an or clause. The two operand clauses are prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final OrClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * Add prefixes to an default operator clause. The two operand clauses are prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * Add prefixes to an and operator clause. The two operand clauses are prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * Add prefixes to an generic operator clause. The child operand clauses is prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     * Prefix a phone number clause with the number prefix.
     *
     * @param clause  The clause to prefix.
     */
    public void visitImpl(final PhoneNumberClause clause) {
        addPrefix(clause, config.getPhoneNumberPrefix());
    }

    /**
     * Prefix a url clause with the url prefix.
     *
     * @param clause  The clause to prefix.
     */
    public void visitImpl(final UrlClause clause) {
        addPrefix(clause, config.getUrlPrefix());
    }

    /**
     * Prefix a email clause with the email prefix.
     *
     * @param clause  The clause to prefix.
     */
    public void visitImpl(final EmailClause clause) {
        addPrefix(clause, config.getEmailPrefix());
    }

    private void addPrefix(final Clause clause, final String prefix) {

        final String term = getTransformedTerms().get(clause);

        if (!(term.equals("") || isAlreadyPrefixed(term, prefix))) {
            getTransformedTerms().put(clause, prefix + ':' + term);
        }
    }

    private static boolean isAlreadyPrefixed(final String term, final String prefix) {
        return term.indexOf(prefix + ':') > -1;
    }

    private Map<Clause,String> getTransformedTerms() {
        return getContext().getTransformedTerms();
    }

}

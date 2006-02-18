/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQueryParserContext.java
 *
 * Created on 12 January 2006, 12:06
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.IntegerClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.OrganisationNumberClause;
import no.schibstedsok.front.searchportal.query.PhoneNumberClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.WordClause;
import org.apache.log4j.Logger;

/** Default implementation of QueryParser.Context's createXxxClause methods.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQueryParserContext implements AbstractQueryParser.Context {
    
    private static final Logger LOG = Logger.getLogger(AbstractQueryParserContext.class);

    /** Creates a new instance of AbstractQueryParserContext.
     */
    public AbstractQueryParserContext() {
    }

    /** {@inheritDoc}
     */
    public String getQueryString() {
        return getTokenEvaluatorFactory().getQueryString();
    }


    //// Operator creators

    /** {@inheritDoc}
     */
    public AndClause createAndClause(
        final Clause first,
        final Clause second) {

        LOG.trace("createAndClause("+first+","+second+")");
        return AndClauseImpl.createAndClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public OrClause createOrClause(
        final Clause first,
        final Clause second) {

        LOG.trace("createOrClause("+first+","+second+")");
        return OrClauseImpl.createOrClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public AndNotClause createAndNotClause(
        final Clause first,
        final Clause second) {

        LOG.trace("createOrClause("+first+","+second+")");
        return AndNotClauseImpl.createAndNotClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public NotClause createNotClause(
        final Clause first) {

        LOG.trace("createNotClause("+first+")");
        return NotClauseImpl.createNotClause(first, getTokenEvaluatorFactory());
    }


    //// Leaf creators

    /** {@inheritDoc}
     */
    public WordClause createWordClause(
        final String term,
        final String field) {

        LOG.trace("createWordClause("+term+","+field+")");
        return WordClauseImpl.createWordClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public PhraseClause createPhraseClause(
        final String term,
        final String field) {

        LOG.trace("createPhraseClause("+term+","+field+")");
        return PhraseClauseImpl.createPhraseClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public IntegerClause createIntegerClause(
        final String term,
        final String field) {

        LOG.trace("createIntegerClause("+term+","+field+")");
        return IntegerClauseImpl.createIntegerClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public PhoneNumberClause createPhoneNumberClause(
        final String term,
        final String field) {

        LOG.trace("createPhoneNumberClause("+term+","+field+")");
        return PhoneNumberClauseImpl.createPhoneNumberClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public OrganisationNumberClause createOrganisationNumberClause(
        final String term,
        final String field) {

        LOG.trace("createOrganisationNumberClause("+term+","+field+")");
        return OrganisationNumberClauseImpl.createOrganisationNumberClause(term, field, getTokenEvaluatorFactory());
    }

}

/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQueryParserContext.java
 *
 * Created on 12 January 2006, 12:06
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

/** Default implementation of QueryParser.Context's createXxxClause methods.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQueryParserContext implements AbstractQueryParser.Context {

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
    public AndClauseImpl createAndClause(
        final LeafClause first,
        final Clause second) {

        return AndClauseImpl.createAndClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public OrClauseImpl createOrClause(
        final LeafClause first,
        final Clause second) {

        return OrClauseImpl.createOrClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public AndNotClauseImpl createAndNotClause(
        final LeafClause first,
        final Clause second) {

        return AndNotClauseImpl.createAndNotClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public NotClauseImpl createNotClause(
        final LeafClause first) {

        return NotClauseImpl.createNotClause(first, getTokenEvaluatorFactory());
    }


    //// Leaf creators

    /** {@inheritDoc}
     */
    public WordClauseImpl createWordClause(
        final String term,
        final String field) {

        return WordClauseImpl.createWordClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public PhraseClauseImpl createPhraseClause(
        final String term,
        final String field) {

        return PhraseClauseImpl.createPhraseClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public IntegerClauseImpl createIntegerClause(
        final String term,
        final String field) {

        return IntegerClauseImpl.createIntegerClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public PhoneNumberClauseImpl createPhoneNumberClause(
        final String term,
        final String field) {

        return PhoneNumberClauseImpl.createPhoneNumberClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public OrganisationNumberClauseImpl createOrganisationNumberClause(
        final String term,
        final String field) {

        return OrganisationNumberClauseImpl.createOrganisationNumberClause(term, field, getTokenEvaluatorFactory());
    }

}

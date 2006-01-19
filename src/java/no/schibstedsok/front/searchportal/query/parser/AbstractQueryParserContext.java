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
    public AndClause createAndClause(
        final LeafClause first,
        final Clause second) {

        return AndClause.createAndClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public OrClause createOrClause(
        final LeafClause first,
        final Clause second) {

        return OrClause.createOrClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public AndNotClause createAndNotClause(
        final LeafClause first,
        final Clause second) {

        return AndNotClause.createAndNotClause(first, second, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public NotClause createNotClause(
        final LeafClause first) {

        return NotClause.createNotClause(first, getTokenEvaluatorFactory());
    }


    //// Leaf creators

    /** {@inheritDoc}
     */
    public WordClause createWordClause(
        final String term,
        final String field) {

        return WordClause.createWordClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public PhraseClause createPhraseClause(
        final String term,
        final String field) {

        return PhraseClause.createPhraseClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public IntegerClause createIntegerClause(
        final String term,
        final String field) {

        return IntegerClause.createIntegerClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public PhoneNumberClause createPhoneNumberClause(
        final String term,
        final String field) {

        return PhoneNumberClause.createPhoneNumberClause(term, field, getTokenEvaluatorFactory());
    }

    /** {@inheritDoc}
     */
    public OrganisationNumberClause createOrganisationNumberClause(
        final String term,
        final String field) {

        return OrganisationNumberClause.createOrganisationNumberClause(term, field, getTokenEvaluatorFactory());
    }

}

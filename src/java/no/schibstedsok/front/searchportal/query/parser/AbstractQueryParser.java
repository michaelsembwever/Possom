/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQueryParser.java
 *
 * Created on 12 January 2006, 12:33
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Abstract helper for implementing a QueryParser
 * Provides default implementation to get the query object.
 * <b>This implementation is not synchronised / thread-safe.</b>
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQueryParser implements QueryParser {

    /** The Context an AbstractQueryParser implementation needs to work off.
     * The QueryParser is not responsible for
     *  - holding the user's orginal inputted query string,
     *  - holding the tokenEvalautorFactory responsible for tokenPredicate to evaluator mappings,
     *  - creation of Clause subtypes.
     **/
    public interface Context extends QueryStringContext {

        /** Get the tokenEvalautorFactory.
         * Responsible for  handing out evaluators against TokenPredicates.
         * Also holds state information about the current term/clause we are finding predicates against.
         *
         * @return the tokenEvaluatorFactory this Parser will use.
         */
        TokenEvaluatorFactory getTokenEvaluatorFactory();

        //// Operation creators

        /**
         * Creator wrapper method for AndClauseImpl objects.
         * The methods also allow a chunk of creation logic for the AndClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a AnAndClauseImplnstance matching the term, left and right child clauses.
         */
        AndClauseImpl createAndClause(final LeafClause first, final Clause second);
        /**
         * Creator wrapper method for OrClauseImpl objects.
         * The methods also allow a chunk of creation logic for the OrClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a OrOrClauseImplnstance matching the term, left and right child clauses.
         */
        OrClauseImpl createOrClause(final LeafClause first, final Clause second);
        /**
         * Creator wrapper method for AndNotClauseImpl objects.
         * The methods also allow a chunk of creation logic for the AndNotClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a AnAndNotClauseImplnstance matching the term, left and right child clauses.
         */
        AndNotClauseImpl createAndNotClause(final LeafClause first, final Clause second);
        /**
         * Creator wrapper method for NotClauseImpl objects.
         * The methods also allow a chunk of creation logic for the NotClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @return returns a NNotClauseImplinstance matching the term, left and right child clauses.
         */
        NotClauseImpl createNotClause(final LeafClause first);

        //// Leaf creators

        /**
         * Creator wrapper method for WordClauseImpl objects.
         * The methods also allow a chunk of creation logic for the WordClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a WoWordClauseImplnstance matching the term, left and right child clauses.
         */
        WordClauseImpl createWordClause(final String term, final String field);
        /**
         * Creator wrapper method for PhraseClauseImpl objects.
         * The methods also allow a chunk of creation logic for the PhraseClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a PhPhraseClauseImplnstance matching the term, left and right child clauses.
         */
        PhraseClauseImpl createPhraseClause(final String term, final String field);
        /**
         * Creator wrapper method for IntegerClauseImpl objects.
         * The methods also allow a chunk of creation logic for the IntegerClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a InIntegerClauseImplnstance matching the term, left and right child clauses.
         */
        IntegerClauseImpl createIntegerClause(final String term, final String field);
        /**
         * Creator wrapper method for PhoneNumberClauseImpl objects.
         * The methods also allow a chunk of creation logic for the PhoneNumberClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a PhPhoneNumberClauseImplnstance matching the term, left and right child clauses.
         */
        PhoneNumberClauseImpl createPhoneNumberClause(final String term, final String field);
        /**
         * Creator wrapper method for OrganisationNumberClauseImpl objects.
         * The methods also allow a chunk of creation logic for the OrganisationNumberClauseImpl to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a OrOrganisationNumberClauseImplnstance matching the term, left and right child clauses.
         */
        OrganisationNumberClauseImpl createOrganisationNumberClause(final String term, final String field);

    }

    /** Protected so an .jj file implementing this class can reuse.
     **/
    protected static final Log LOG = LogFactory.getLog(AbstractQueryParser.class);
    /** Error message when the parser tries to parse an empty query string.
     ***/
    protected static final String ERR_CANNOT_PARSE_EMPTY_QUERY
        = "QueryParser can not parse an empty query. (The \"QueryParser(QueryParser.Context)\" constructor must be used!)";

    /** the context this query parser implementation must work against.
     ***/
    protected Context context;
    /** the resulting query object.
     ***/
    private Query query;

    /**
     * do the actual parsing.
     * This method shouldn't be public but that's the way javacc creates it unfortunately.
     * @throws ParseException   when parsing the inputted query string.
     * @return the clause heirachy ready to wrap a Query around.
     */
    public abstract Clause parse() throws ParseException;

    /**
     * Get the query object.
     * A call to this method initates the parse() method if the query hasn't already been built.
     * A call to this when the queryStr (passed in the constructor) is null or zero length results in a
     * IllegalStateException being thrown.
     * @return the Query object, ready to use.
     * @throws ParseException  when parsing the inputted query string.
     */
    public Query getQuery() throws ParseException{
        if( query == null ){
            final String queryStr = context.getQueryString();
            if( context == null || queryStr == null || queryStr.trim().length()==0 ){
                throw new IllegalStateException(ERR_CANNOT_PARSE_EMPTY_QUERY);
            }
            final Clause clause = parse();
            query = new AbstractQuery(context.getQueryString()){
                public Clause getRootClause(){
                    return clause;
                }
            };
        }
        return query;
    }

}

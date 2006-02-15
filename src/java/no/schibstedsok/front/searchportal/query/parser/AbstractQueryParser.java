/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQueryParser.java
 *
 * Created on 12 January 2006, 12:33
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
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.query.WordClause;
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
         * Creator wrapper method for AndClause objects.
         * The methods also allow a chunk of creation logic for the AndClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a AndClause instance matching the term, left and right child clauses.
         */
        AndClause createAndClause(final LeafClause first, final Clause second);
        /**
         * Creator wrapper method for OrClause objects.
         * The methods also allow a chunk of creation logic for the OrClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a OrOrClauseImplnstance matching the term, left and right child clauses.
         */
        OrClause createOrClause(final LeafClause first, final Clause second);
        /**
         * Creator wrapper method for AndNotClause objects.
         * The methods also allow a chunk of creation logic for the AndNotClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a AnAndNotClauseImplnstance matching the term, left and right child clauses.
         */
        AndNotClause createAndNotClause(final LeafClause first, final Clause second);
        /**
         * Creator wrapper method for NotClause objects.
         * The methods also allow a chunk of creation logic for the NotClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param first the left child clause of the operation clause we are about to create (or find).
         * The current implementation always creates a right-leaning query heirarchy.
         * Therefore the left child clause to any operation clause must be a LeafClause.
         * @return returns a NNotClauseImplinstance matching the term, left and right child clauses.
         */
        NotClause createNotClause(final LeafClause first);

        //// Leaf creators

        /**
         * Creator wrapper method for WordClause objects.
         * The methods also allow a chunk of creation logic for the WordClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a WoWordClauseImplnstance matching the term, left and right child clauses.
         */
        WordClause createWordClause(final String term, final String field);
        /**
         * Creator wrapper method for PhraseClause objects.
         * The methods also allow a chunk of creation logic for the PhraseClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a PhPhraseClauseImplnstance matching the term, left and right child clauses.
         */
        PhraseClause createPhraseClause(final String term, final String field);
        /**
         * Creator wrapper method for IntegerClause objects.
         * The methods also allow a chunk of creation logic for the IntegerClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a InIntegerClauseImplnstance matching the term, left and right child clauses.
         */
        IntegerClause createIntegerClause(final String term, final String field);
        /**
         * Creator wrapper method for PhoneNumberClause objects.
         * The methods also allow a chunk of creation logic for the PhoneNumberClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a PhPhoneNumberClauseImplnstance matching the term, left and right child clauses.
         */
        PhoneNumberClause createPhoneNumberClause(final String term, final String field);
        /**
         * Creator wrapper method for OrganisationNumberClause objects.
         * The methods also allow a chunk of creation logic for the OrganisationNumberClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a OrOrganisationNumberClauseImplnstance matching the term, left and right child clauses.
         */
        OrganisationNumberClause createOrganisationNumberClause(final String term, final String field);

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

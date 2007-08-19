/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * QueryParser.java
 *
 * Created on 12 January 2006, 12:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.sesat.searchportal.query.parser;

import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.searchportal.query.AndClause;
import no.sesat.searchportal.query.AndNotClause;
import no.sesat.searchportal.query.Clause;
import no.sesat.searchportal.query.DefaultOperatorClause;
import no.sesat.searchportal.query.EmailClause;
import no.sesat.searchportal.query.IntegerClause;
import no.sesat.searchportal.query.NotClause;
import no.sesat.searchportal.query.OrClause;
import no.sesat.searchportal.query.NumberGroupClause;
import no.sesat.searchportal.query.PhoneNumberClause;
import no.sesat.searchportal.query.PhraseClause;
import no.sesat.searchportal.query.Query;
import no.sesat.searchportal.query.QueryStringContext;
import no.sesat.searchportal.query.UrlClause;
import no.sesat.searchportal.query.WordClause;
import no.sesat.searchportal.query.XorClause;
import no.sesat.searchportal.query.token.TokenEvaluationEngineContext;


/** Parser responsible for building the Query and its clause heirarchy.
 * This interface does not define the behaviour as to how the QueryParser will get the query string.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryParser {

    /** The Context an QueryParser implementation needs to work off.
     * The QueryParser is not responsible for
     *  - holding the user's orginal inputted query string,
     *  - holding the tokenEvalautorFactory responsible for tokenPredicate to evaluator mappings,
     *  - creation of Clause subtypes.
     **/
    public interface Context extends BaseContext, QueryStringContext, TokenEvaluationEngineContext {

        //// Operation creators

        /**
         * Creator wrapper method for DefaultOperatorClause objects.
         * The methods also allow a chunk of creation logic for the DefaultOperatorClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param first the left child clause of the operation clause we are about to create (or find).
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a DefaultOperatorClause matching the term, left and right child clauses.
         */
        DefaultOperatorClause createDefaultOperatorClause(final Clause first, final Clause second);
        /**
         * Creator wrapper method for AndClause objects.
         * The methods also allow a chunk of creation logic for the AndClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param first the left child clause of the operation clause we are about to create (or find).
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a AndClause instance matching the term, left and right child clauses.
         */
        AndClause createAndClause(final Clause first, final Clause second);
        /**
         * Creator wrapper method for OrClause objects.
         * The methods also allow a chunk of creation logic for the OrClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param first the left child clause of the operation clause we are about to create (or find).
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a OrOrClauseImplnstance matching the term, left and right child clauses.
         */
        OrClause createOrClause(final Clause first, final Clause second);
        /**
         * Creator wrapper method for XorClause objects.
         * The methods also allow a chunk of creation logic for the XorClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param first the left child clause of the operation clause we are about to create (or find).
         * @param second the right child clause of the operation clause we are about to create (or find).
         * @return returns a OrOrClauseImplnstance matching the term, left and right child clauses.
         */
        XorClause createXorClause(final Clause first, final Clause second, final XorClause.Hint hint);
        /**
         * Creator wrapper method for AndNotClause objects.
         * The methods also allow a chunk of creation logic for the AndNotClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param first the left child clause of the operation clause we are about to create (or find).
         * @return returns a AnAndNotClauseImplnstance matching the term, left and right child clauses.
         */
        AndNotClause createAndNotClause(final Clause first);
        /**
         * Creator wrapper method for NotClause objects.
         * The methods also allow a chunk of creation logic for the NotClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param first the left child clause of the operation clause we are about to create (or find).
         * @return returns a NNotClauseImplinstance matching the term, left and right child clauses.
         */
        NotClause createNotClause(final Clause first);

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
         * Creator wrapper method for NumberGroupClause objects.
         * The methods also allow a chunk of creation logic for the NumberGroupClause to be moved
         * out of the QueryParserImpl.jj file to here.
         * 
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a OrOrganisationNumberClauseImplnstance matching the term, left and right child clauses.
         */
        NumberGroupClause createNumberGroupClause(final String term, final String field);
        /**
         * Creator wrapper method for UrlClause objects.
         * The methods also allow a chunk of creation logic for the UrlClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a UrlClause matching the term, left and right child clauses.
         */
        UrlClause createUrlClause(final String term, final String field);
        /**
         * Creator wrapper method for EmailClause objects.
         * The methods also allow a chunk of creation logic for the EmailClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a EmailClause matching the term, left and right child clauses.
         */
        EmailClause createEmailClause(final String term, final String field);
    }


    /** Get the Query.
     *
     *@return the Query object.
     **/
    Query getQuery();

}

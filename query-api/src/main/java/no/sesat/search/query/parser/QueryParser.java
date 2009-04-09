/* Copyright (2005-2008) Schibsted ASA
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
 * QueryParser.java
 *
 * Created on 12 January 2006, 12:32
 *
 */

package no.sesat.search.query.parser;

import no.sesat.commons.ioc.BaseContext;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.EmailClause;
import no.sesat.search.query.IntegerClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.NumberGroupClause;
import no.sesat.search.query.PhoneNumberClause;
import no.sesat.search.query.PhraseClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.QueryStringContext;
import no.sesat.search.query.UrlClause;
import no.sesat.search.query.WordClause;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.token.TokenEvaluationEngineContext;


/** Parser responsible for building the Query and its clause heirarchy.
 * This interface does not define the behaviour as to how the QueryParser will get the query string.
 *
 * @version $Id$
 *
 */
public interface QueryParser {

    /**
     * Duplication of the parser's definition of SKIP. Must be kept uptodate!
     * It's actually a duplication of SKIP.
     */
    char[][] SKIP_CHARACTER_RANGES = {
        {' ', ' '},
        {'-', '-'},
        {'!', '!'},
        {'\u0023', '\u0029'},
        {'\u003b', '\u0040'},
        {'\u005b', '\u0060'},
        {'\u007b', '\u00bf'},
        {'\u00d7', '\u00d7'},
        {'\u00f7', '\u00f7'},
        {'\u2010', '\u2015'}
    };

    /**
     * Duplication of the parser's operators. Must be kept uptodate!
     */
    String[] OPERATORS = {"*", " -", " +", "(", ")"};

    /** The Context an QueryParser implementation needs to work off.
     * The QueryParser's context is responsible for:
     *  - holding the user's orginal inputted query string,
     *  - holding the tokenEvalautorFactory responsible for tokenPredicate to evaluator mappings,
     *  - creation of Clause subtypes (using the flyweight pattern).
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

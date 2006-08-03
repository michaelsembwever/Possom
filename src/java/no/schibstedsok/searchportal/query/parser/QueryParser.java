/* Copyright (2005-2006) Schibsted Søk AS
 * QueryParser.java
 *
 * Created on 12 January 2006, 12:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.query.parser;

import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.EmailClause;
import no.schibstedsok.searchportal.query.IntegerClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.OrganisationNumberClause;
import no.schibstedsok.searchportal.query.PhoneNumberClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.QueryStringContext;
import no.schibstedsok.searchportal.query.UrlClause;
import no.schibstedsok.searchportal.query.WordClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;


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
    public interface Context extends BaseContext, QueryStringContext {

        /** Get the tokenEvalautorFactory.
         * Responsible for  handing out evaluators against TokenPredicates.
         * Also holds state information about the current term/clause we are finding predicates against.
         *
         * @return the TokenEvaluationEngine this Parser will use.
         */
        TokenEvaluationEngine getTokenEvaluationEngine();

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
         * Creator wrapper method for OrganisationNumberClause objects.
         * The methods also allow a chunk of creation logic for the OrganisationNumberClause to be moved
         * out of the QueryParserImpl.jj file to here.
         *
         * @param term the term this clause represents.
         * @param field any field this clause was specified against.
         * @return returns a OrOrganisationNumberClauseImplnstance matching the term, left and right child clauses.
         */
        OrganisationNumberClause createOrganisationNumberClause(final String term, final String field);
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

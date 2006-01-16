/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQueryParser.java
 *
 * Created on 12 January 2006, 12:33
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import java.io.StringReader;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQueryParser implements QueryParser {

    public interface Context {
        String getQueryString();
        TokenEvaluatorFactory getTokenEvaluatorFactory();

        //// Operation creators

        AndClause createAndClause( final LeafClause first, final Clause second);
        OrClause createOrClause( final LeafClause first, final Clause second);
        AndNotClause createAndNotClause( final LeafClause first, final Clause second);
        NotClause createNotClause( final LeafClause first);

        //// Leaf creators

        WordClause createWordClause(final String term, final String field);
        PhraseClause createPhraseClause(final String term, final String field);
        IntegerClause createIntegerClause(final String term, final String field);
        PhoneNumberClause createPhoneNumberClause(final String term, final String field);
        OrganisationNumberClause createOrganisationNumberClause(final String term, final String field);

    }

    protected static final Log LOG = LogFactory.getLog(AbstractQueryParser.class);
    protected static final String ERR_CANNOT_PARSE_EMPTY_QUERY
        = "QueryParser can not parse an empty query. (The \"QueryParser(QueryParser.Context)\" constructor must be used!)";

    protected Context context;
    protected Query query;

    public abstract Clause parse() throws ParseException;

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

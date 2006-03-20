/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQueryParser.java
 *
 * Created on 12 January 2006, 12:33
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import java.util.Iterator;
import java.util.Stack;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
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
import org.apache.log4j.Logger;

/** Abstract helper for implementing a QueryParser
 * Provides default implementation to get the query object.
 * <b>This implementation is not synchronised / thread-safe.</b>
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQueryParser implements QueryParser {

    /** Protected so an .jj file implementing this class can reuse.
     **/
    protected static final Logger LOG = Logger.getLogger(AbstractQueryParser.class);
    private static final Stack<String> METHOD_STACK = new Stack<String>();

    /** Error message when the parser tries to parse an empty query string.
     ***/
    protected static final String ERR_EMPTY_CONTEXT
        = "The \"QueryParser(QueryParser.Context)\" constructor must be used!";
    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";

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
     * @return the Query object, ready to use.
     * @throws ParseException  when parsing the inputted query string.
     */
    public Query getQuery(){
        if( query == null ){
            final String queryStr = context.getQueryString();
            if( context == null ){
                throw new IllegalStateException(ERR_EMPTY_CONTEXT);
            }
            try{
                final Clause root = ( queryStr == null || queryStr.trim().length()==0 )
                    ? context.createWordClause("",null)
                    : parse();

                query = new AbstractQuery(context.getQueryString()){
                    public Clause getRootClause(){
                        return root;
                    }
                };
                
            }catch(ParseException pe){
                LOG.warn(ERR_PARSING, pe);
            } catch (TokenMgrError tme)  {
                LOG.error(ERR_PARSING, tme);
            }
            
            if( query == null ){
                // common post-exception handling
                query = new AbstractQuery(context.getQueryString()){
                    public Clause getRootClause(){
                        return context.createWordClause("",null);
                    }
                };
            }
        }
        return query;
    }

    protected final Context createContext(final String input){
        return ContextWrapper.wrap(
            QueryParser.Context.class,
            new BaseContext[]{
                new QueryStringContext(){
                    public String getQueryString(){
                        return input;
                    }
                },
                context
            }
        );
        
    }

    protected final void enterMethod(final String method){
        if( LOG.isTraceEnabled() ){
            METHOD_STACK.push(method);
            final StringBuffer sb = new StringBuffer();
            for( Iterator it = METHOD_STACK.iterator(); it.hasNext(); ){
                final String m = (String)it.next();
                sb.append("." + m );
            }
            LOG.trace(sb.toString());
        }
    }

    protected final void exitMethod(){
        if( LOG.isTraceEnabled() ){
            METHOD_STACK.pop();
        }
    }

}

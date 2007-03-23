/* Copyright (2005-2007) Schibsted Søk AS
 *
 * AbstractQueryParser.java
 *
 * Created on 12 January 2006, 12:33
 *
 */

package no.schibstedsok.searchportal.query.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Stack;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.QueryStringContext;
import no.schibstedsok.searchportal.query.finder.ParentFinder;
import no.schibstedsok.searchportal.query.parser.alt.RotationAlternation;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/** Abstract helper for implementing a QueryParser
 * Provides default implementation to get the query object.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQueryParser implements QueryParser {

    // Constants -----------------------------------------------------
    
    /** Protected so an .jj file implementing this class can reuse.
     **/
    protected static final Logger LOG = Logger.getLogger(AbstractQueryParser.class);
    private static final Logger PRODUCT_LOG = Logger.getLogger("no.schibstedsok.Product");

    /** Error message when the parser tries to parse an empty query string.
     ***/
    protected static final String ERR_EMPTY_CONTEXT
        = "The \"QueryParser(QueryParser.Context)\" constructor must be used!";
    
    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException of ";

    // Attributes ----------------------------------------------------
    
    private final Stack<String> methodStack = new Stack<String>();
    
    /** the context this query parser implementation must work against.
     ***/
    protected Context context;
    
    /** the resulting query object.
     ***/
    private Query query;

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * do the actual parsing.
     * This method shouldn't be public but that's the way javacc creates it unfortunately.
     * @throws ParseException   when parsing the inputted query string.
     * @return the clause hierarchy ready to wrap a Query around.
     */
    public abstract Clause parse() throws ParseException;

    /**
     * Get the query object.
     * A call to this method initiates the parse() method if the query hasn't already been built.
     * @return the Query object, ready to use.
     */
    public Query getQuery(){
        if( query == null ){
            final String queryStr = context.getQueryString();
            if( context == null ){
                throw new IllegalStateException(ERR_EMPTY_CONTEXT);
            }
            final ParentFinder parentFinder = new ParentFinder();
            try{
                if( queryStr != null && queryStr.trim().length()>0 ){

                    // Uncomment the following line, and comment the line after than, to disable RotationAlternation.
                    //final Clause root = parse();
                    final Clause root = alterations( parse(), parentFinder );

                    query = new AbstractQuery(context.getQueryString()){
                        public Clause getRootClause(){
                            return root;
                        }
                        public ParentFinder getParentFinder(){
                            return parentFinder;
                        }
                    };
                }

            }catch(ParseException pe){
                LOG.warn(ERR_PARSING + queryStr, pe);
                // also let product department know these queries are not working
                PRODUCT_LOG.info("<invalid-query type=\"ParseException\">" 
                        + StringEscapeUtils.escapeXml(queryStr) + "</invalid-query>");
                
            } catch (TokenMgrError tme)  {
                LOG.error(ERR_PARSING + queryStr, tme);
                // also let product department know these queries are not working
                PRODUCT_LOG.info("<invalid-query type=\"TokenMgrError\">" 
                        + StringEscapeUtils.escapeXml(queryStr) + "</invalid-query>");
            }

            if( query == null ){
                final Clause empty = context.createWordClause("",null);
                // common post-exception handling
                query = new AbstractQuery(context.getQueryString()){
                    public Clause getRootClause(){
                        return empty;
                    }
                    public boolean isBlank(){
                        return true;
                    }
                    public ParentFinder getParentFinder(){
                        return parentFinder;
                    }
                };
            }

        }
        return query;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** Create a new context the return the argument on any call to its getQueryString() method.
     * 
     * @param input the query string returned from the created context's getQueryString() method.
     * @return new content supplying access to query string "input"
     */
    protected final Context createContext(final String input){

        return ContextWrapper.wrap(
            QueryParser.Context.class,
            new QueryStringContext(){
                public String getQueryString(){
                    return input;
                }
            },
            context
        );

    }

    /** Debugging method for tracing a method entry.
     * 
     * @param method the name of the method
     */
    protected final void enterMethod(final String method){
        if( LOG.isTraceEnabled() ){
            methodStack.push(method);
            final StringBuilder sb = new StringBuilder();
            for( Iterator it = methodStack.iterator(); it.hasNext(); ){
                final String m = (String)it.next();
                sb.append("." + m );
            }
            LOG.trace(sb.toString());
        }
    }

    /**
     * Debugging method for tracing a method exit.
     */
    protected final void exitMethod(){
        if( LOG.isTraceEnabled() ){
            methodStack.pop();
        }
    }


    /**
     * 
     * @param query 
     * @param leftChar 
     * @param rightChar 
     * @return 
     */
    protected final String balance(String query, final char leftChar, final char rightChar){
        int left = 0, right = 0;
        final char[] chars = query.toCharArray();
        for( int i = 0; i < chars.length; ++i ){ 
            if( chars[i] == leftChar ){ ++left; }
            if( chars[i] == rightChar ){ ++right; }
        }
        if( left != right ){
            // uneven amount of (). Ignore all of them then.
            query = query.replaceAll("\\" + leftChar, "").replaceAll("\\" + rightChar, "");
            ReInit(new StringReader(query));
        }
        return query;
    }

    /**
     * 
     * @param query 
     * @param c 
     * @return 
     */
    protected final String even(String query, final char c){
        int count = 0;
        final char[] chars = query.toCharArray();
        for( int i = 0; i < chars.length; ++i ){ 
            if( chars[i] == c ){ ++count; }
        }
        if( count % 2 >0 ){
            // uneven amount of (). Ignore all of them then.    
            query = query.replaceAll("\\" + c, "");
            ReInit(new StringReader(query));
        }
        return query;
    }

    /**
     * 
     * @param query 
     * @return 
     */
    protected final String numberNeedsTrailingSpace(String query){
        
        // HACK because phone numbers and organisation numbers need to finish
        // with a space. SEARCH-672
        if( query.length() > 0 && Character.isDigit( query.charAt(query.length()-1) ) ){
            query = query + ' ';
            ReInit(new StringReader(query));
        }
        return query;
    }
    
    protected abstract void ReInit(Reader reader);
    
    // Private -------------------------------------------------------
    
    private Clause alterations(final Clause original, final ParentFinder parentFinder){

        // rotation alterations
        final RotationAlternation rotator = new RotationAlternation(
                ContextWrapper.wrap(RotationAlternation.Context.class,
                new BaseContext(){
                        public ParentFinder getParentFinder(){
                            return parentFinder;
                        }
                },
                context));

        return rotator.createRotations(original);
    }

}


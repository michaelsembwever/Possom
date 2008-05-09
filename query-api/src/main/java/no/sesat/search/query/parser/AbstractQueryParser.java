/* Copyright (2005-2007) Schibsted Søk AS
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
 *
 * AbstractQueryParser.java
 *
 * Created on 12 January 2006, 12:33
 *
 */

package no.sesat.search.query.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.query.Clause;
import no.sesat.search.query.Query;
import no.sesat.search.query.QueryStringContext;
import no.sesat.search.query.finder.ParentFinder;
import no.sesat.search.query.parser.alt.Alternation;
import no.sesat.search.query.parser.alt.FullnameAlternation;
import no.sesat.search.query.parser.alt.RotationAlternation;
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
    
    public static final String SKIP_REGEX;
    
    /** Protected so an .jj file implementing this class can reuse.
     **/
    protected static final Logger LOG = Logger.getLogger(AbstractQueryParser.class);
    private static final Logger PRODUCT_LOG = Logger.getLogger("no.sesat.Product");

    /** Error message when the parser tries to parse an empty query string.
     ***/
    protected static final String ERR_EMPTY_CONTEXT
        = "The \"QueryParser(QueryParser.Context)\" constructor must be used!";
    
    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException of ";

    // Attributes ----------------------------------------------------
    
    private final Stack<String> methodStack = new Stack<String>();
    
    /** the context this query parser implementation must work against.
     ***/
    protected transient Context context;
    
    /** the resulting query object.
     ***/
    private Query query;

    // Static --------------------------------------------------------

    static{
        
        // build our skip regular expression
        final StringBuilder builder = new StringBuilder();
        for(char[] range : QueryParser.SKIP_CHARACTER_RANGES){
            builder.append("[\\" + range[0] + "-\\" + range[1] + "]|");
        }
        // remove trailing '|'
        builder.setLength(builder.length() - 1);
        // our skip regular expression
        SKIP_REGEX = '(' + builder.toString() + ')';
    }

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
            final String q = context.getQueryString();
            
            if( context == null ){
                throw new IllegalStateException(ERR_EMPTY_CONTEXT);
            }
            
            final ParentFinder parentFinder = new ParentFinder();
            try{
                if( null != q && 0 < q.replaceAll(SKIP_REGEX, "").trim().length() && !"*".equals(q) ){

                    // Uncomment the following line, and comment the line after than, to disable RotationAlternation.
                    //final Clause root = parse();
                    final Clause root = alternate( parse(), parentFinder );

                    query = createQuery(context.getQueryString(), false, root, parentFinder);
                }

            }catch(ParseException pe){
                LOG.warn(ERR_PARSING + q, pe);
                // also let product department know these queries are not working
                PRODUCT_LOG.info("<invalid-query type=\"ParseException\">" 
                        + StringEscapeUtils.escapeXml(q) + "</invalid-query>");
                
            } catch (TokenMgrError tme)  {
                LOG.error(ERR_PARSING + q, tme);
                // also let product department know these queries are not working
                PRODUCT_LOG.info("<invalid-query type=\"TokenMgrError\">" 
                        + StringEscapeUtils.escapeXml(q) + "</invalid-query>");
            }

            if( query == null ){
                
                final Clause empty = context.createWordClause("",null);
                final String qStr = context.getQueryString();
                // common post-exception handling. 
                // * is a special query to search for everything 
                //  and should be treated as a non-blank query despite having crashed the parser.
                query = createQuery(qStr, !"*".equals(qStr), empty, parentFinder);
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


    /** Ensure that for every leftChar there is a matching rightChar.
     * Otherwise remove all occurences of both leftChar and rightChar.
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

    /** Ensure that there are a even number of c characters in the phrase, otherwise remove all occurences of c.
     * 
     * @param query 
     * @param c the character to ensure has an even occurence count.
     * @return unchanged or changes string.
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

    /** HACK because phone numbers and organisation numbers need to finish
     * with a space. SEARCH-672
     * 
     * @param query 
     * @return 
     */
    protected final String numberNeedsTrailingSpace(String query){
        
        if( query.length() > 0 && Character.isDigit( query.charAt(query.length()-1) ) ){
            query = query + ' ';
            ReInit(new StringReader(query));
        }
        return query;
    }
    
    /** HACK because a floating hyphon is interpretted as a NotClause.
     * Sesam syntax requires the hyphon to be adjacent, without whitespace, to the next term. SEARCH-3390
     * 
     * @param query 
     * @return 
     */
    protected final String fixFloatingHyphon(String query){
        
        if(0 <= query.indexOf(" - ")){
            query = query.replaceAll("( )+-( )+", "- ");
            ReInit(new StringReader(query));
        }
        return query;
    }
    
    protected abstract void ReInit(Reader reader);
    
    // Private -------------------------------------------------------
    
    private Clause alternate(final Clause original, final ParentFinder parentFinder){
        
        Clause result = original;
        for(Alternation alternation : getAlternations(parentFinder)){
            result = alternation.alternate(result);
        }
        return result;
    }
    
    private List<Alternation> getAlternations(final ParentFinder parentFinder){

        // the list we'll return
        final List<Alternation> alternations = new ArrayList<Alternation>();
        
        // the context each alternation will work with
        final Alternation.Context cxt = ContextWrapper.wrap(
                Alternation.Context.class,
                new BaseContext(){
                    public ParentFinder getParentFinder(){
                        return parentFinder;
                    }   
                },
                context);
                
        // create and add each alternation
        alternations.add(new RotationAlternation(cxt));
        //alternations.add(new FullnameAlternation(cxt)); // disable. see SEARCH-2269 

        return alternations;
    }


    private static Query createQuery(
            final String string, 
            final boolean blank,
            final Clause root, 
            final ParentFinder parentFinder){

        return AbstractQuery.createQuery(string, blank, root, parentFinder);
                /*new AbstractQuery(string){
            public Clause getRootClause(){

                return root;
            }
            public ParentFinder getParentFinder(){
                return parentFinder;
            }
            public boolean isBlank(){
                return blank;
            }
        };*/
    }
}


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
public abstract class AbstractQueryParserContext implements AbstractQueryParser.Context{
    
    /** Creates a new instance of AbstractQueryParserContext */
    public AbstractQueryParserContext() {
    }
    
    public String getQueryString(){
        return getTokenEvaluatorFactory().getQueryString();
    }
    
    public WordClause createWordClause(
        final String term, 
        final String field){

        return WordClause.createWordClause(term,field,getTokenEvaluatorFactory());
    }
    public PhraseClause createPhraseClause(
        final String term, 
        final String field){

        return PhraseClause.createPhraseClause(term,field,getTokenEvaluatorFactory());
    }

    public IntegerClause createIntegerClause(
        final String term, 
        final String field){

        return IntegerClause.createIntegerClause(term,field,getTokenEvaluatorFactory());
    }
    
}

/*
 * TestQueryTransformerContextProvider.java
 *
 * Created on 8. april 2006, 01:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.transform;


import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryTestContext;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.site.Site;

/**
 *
 * @author mick
 */
public final class QueryTransformerTestContext implements QueryTransformer.Context{

    private final Query query;
    private final Map<Clause,String> transformedTerms;
    private final TokenEvaluatorFactory tokenEvaluatorFactory;
    
    /**
     * Creates a new instance of TestQueryTransformerContextProvider
     */
    public QueryTransformerTestContext(
            final Query q,
            final Map<Clause,String> t){

        query = q;
        transformedTerms = t;
        tokenEvaluatorFactory  = new TokenEvaluatorFactoryImpl(
            new TokenEvaluatorFactoryTestContext(query.getQueryString()));
    }

    /**
     * Creates a new instance of TestQueryTransformerContextProvider
     */
    public QueryTransformerTestContext(
            final Query q,
            final Map<Clause,String> t,
            final TokenEvaluatorFactory tokenEvaluatorFactory){

        query = q;
        transformedTerms = t;
        this.tokenEvaluatorFactory = tokenEvaluatorFactory;
    }


    public Map<Clause,String> getTransformedTerms() {
        return transformedTerms;
    }
    public Site getSite() {
        return Site.DEFAULT;
    }
    public Query getQuery() {
        return query;
    }
    public String getTransformedQuery() {
        return query.getQueryString();
    }
    public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
        return FileResourceLoader.newPropertiesLoader(this, resource, properties);
    }

    public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
        return FileResourceLoader.newDocumentLoader(this, resource, builder);
    }
    public TokenEvaluatorFactory getTokenEvaluatorFactory(){
        return tokenEvaluatorFactory;
    }
}
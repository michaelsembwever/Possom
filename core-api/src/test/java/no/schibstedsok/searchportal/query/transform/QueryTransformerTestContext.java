// Copyright (2006) Schibsted Søk AS
/*
 * TestQueryTransformerContextProvider.java
 *
 * Created on 8. april 2006, 01:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.query.transform;


import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.TestCase;

import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineTestContext;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;

/**
 *
 * @author mick
 */
public final class QueryTransformerTestContext extends TestCase implements QueryTransformer.Context{

    private final Query query;
    private final Map<Clause,String> transformedTerms;
    private final TokenEvaluationEngine tokenEvaluationEngine;

    /**
     * Creates a new instance of TestQueryTransformerContextProvider
     */
    public QueryTransformerTestContext(
            final Query q,
            final Map<Clause,String> t){

        query = q;
        transformedTerms = t;
        tokenEvaluationEngine  = new TokenEvaluationEngineImpl(
            new TokenEvaluationEngineTestContext(query.getQueryString()));
    }

    /**
     * Creates a new instance of TestQueryTransformerContextProvider
     */
    public QueryTransformerTestContext(
            final Query q,
            final Map<Clause,String> t,
            final TokenEvaluationEngine tokenEvaluationEngine){

        query = q;
        transformedTerms = t;
        this.tokenEvaluationEngine = tokenEvaluationEngine;
    }


    /** TODO comment me. **/
    public Map<Clause,String> getTransformedTerms() {
        return transformedTerms;
    }
    /** TODO comment me. **/
    public Site getSite() {
        return getTestingSite();
    }

    /** TODO comment me. **/
    public Query getQuery() {
        return query;
    }
    /** TODO comment me. **/
    public String getTransformedQuery() {
        return query.getQueryString();
    }
    public PropertiesLoader newPropertiesLoader(
            final SiteContext siteCxt, 
            final String resource, 
            final Properties properties) {

        return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
    }
    public DocumentLoader newDocumentLoader(
            final SiteContext siteCxt, 
            final String resource, 
            final DocumentBuilder builder) {

        return FileResourceLoader.newDocumentLoader(siteCxt, resource, builder);
    }
    /** TODO comment me. **/
    public TokenEvaluationEngine getTokenEvaluationEngine(){
        return tokenEvaluationEngine;
    }
}
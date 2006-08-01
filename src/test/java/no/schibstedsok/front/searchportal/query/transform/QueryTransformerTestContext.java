// Copyright (2006) Schibsted Søk AS
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
import no.schibstedsok.front.searchportal.query.token.TokenEvaluationEngineTestContext;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.front.searchportal.site.Site;

/**
 *
 * @author mick
 */
public final class QueryTransformerTestContext implements QueryTransformer.Context{

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
        return Site.DEFAULT;
    }

    /** TODO comment me. **/
    public Query getQuery() {
        return query;
    }
    /** TODO comment me. **/
    public String getTransformedQuery() {
        return query.getQueryString();
    }
    /** TODO comment me. **/
    public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
        return FileResourceLoader.newPropertiesLoader(this, resource, properties);
    }

    /** TODO comment me. **/
    public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
        return FileResourceLoader.newDocumentLoader(this, resource, builder);
    }
    /** TODO comment me. **/
    public TokenEvaluationEngine getTokenEvaluationEngine(){
        return tokenEvaluationEngine;
    }
}
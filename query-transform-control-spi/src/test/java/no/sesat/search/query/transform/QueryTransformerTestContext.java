/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 * TestQueryTransformerContextProvider.java
 *
 * Created on 8. april 2006, 01:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.sesat.search.query.transform;


import no.sesat.search.datamodel.DataModel;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenEvaluationEngineImpl;
import no.sesat.search.query.token.TokenEvaluationEngineTestContext;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteTestCase;
import no.sesat.search.site.config.*;

import javax.xml.parsers.DocumentBuilder;
import java.util.Map;
import java.util.Properties;

/**
 * @author mick
 */
public final class QueryTransformerTestContext extends SiteTestCase implements QueryTransformer.Context {

    private final Query query;
    private final Map<Clause, String> transformedTerms;
    private final TokenEvaluationEngine tokenEvaluationEngine;
    private DataModel dataModel;

    /**
     * Creates a new instance of TestQueryTransformerContextProvider
     */
    public QueryTransformerTestContext(
            final Query q,
            final Map<Clause, String> t) {

        query = q;
        transformedTerms = t;
        tokenEvaluationEngine = new TokenEvaluationEngineImpl(
                new TokenEvaluationEngineTestContext(query.getQueryString()));
    }

    /**
     * Creates a new instance of TestQueryTransformerContextProvider
     */
    public QueryTransformerTestContext(
            final Query q,
            final Map<Clause, String> t,
            final TokenEvaluationEngine tokenEvaluationEngine) {

        query = q;
        transformedTerms = t;
        this.tokenEvaluationEngine = tokenEvaluationEngine;
    }


    /**
     * TODO comment me. *
     */
    public Map<Clause, String> getTransformedTerms() {
        return transformedTerms;
    }

    /**
     * TODO comment me. *
     */
    public Site getSite() {
        return getTestingSite();
    }

    /**
     * TODO comment me. *
     */
    public Query getQuery() {
        return query;
    }

    /**
     * TODO comment me. *
     */
    public String getTransformedQuery() {
        return query.getQueryString();
    }

    public DataModel getDataModel() {
        return dataModel;
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

    public BytecodeLoader newBytecodeLoader(SiteContext context, String className, String jar) {
        return FileResourceLoader.newBytecodeLoader(context, className, jar);
    }
    /**
     * TODO comment me. *
     */
    public TokenEvaluationEngine getTokenEvaluationEngine() {
        return tokenEvaluationEngine;
    }

    public void visitXorClause(Visitor visitor, XorClause clause) {
        clause.getFirstClause().accept(visitor);
    }

    public String getFieldFilter(final LeafClause clause) {
        // TODO. Return some thing as AbstractSearchCommand.getFieldFilter
        return null;
    }

}
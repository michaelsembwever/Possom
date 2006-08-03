// Copyright (2006) Schibsted Søk AS
/*
 * TokenEvaluationEngineTestContext.java
 *
 * Created on 8. april 2006, 01:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.query.token;


import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import no.schibstedsok.searchportal.configuration.FileResourcesSiteConfigurationTest;
import no.schibstedsok.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.searchportal.site.Site;

/**
 *
 * @author mick
 */
public final class TokenEvaluationEngineTestContext implements TokenEvaluationEngineImpl.Context{

    private final String query;

    /**
     * Creates a new instance of TokenEvaluationEngineTestContext
     */
    public TokenEvaluationEngineTestContext(final String query) {
        this.query = query;
    }

    /** TODO comment me. **/
    public String getQueryString() {
        return query;
    }

    /** TODO comment me. **/
    public Properties getApplicationProperties() {
        return FileResourcesSiteConfigurationTest.valueOf(Site.DEFAULT).getProperties();
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
    public Site getSite()  {
        return Site.DEFAULT;
    }

}

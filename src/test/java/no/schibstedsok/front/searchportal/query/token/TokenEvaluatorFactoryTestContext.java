// Copyright (2006) Schibsted Søk AS
/*
 * TokenEvaluatorFactoryTestContext.java
 *
 * Created on 8. april 2006, 01:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.token;


import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import no.schibstedsok.front.searchportal.configuration.FileResourcesSiteConfigurationTest;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.site.Site;

/**
 *
 * @author mick
 */
public final class TokenEvaluatorFactoryTestContext implements TokenEvaluatorFactoryImpl.Context{

    private final String query;

    /**
     * Creates a new instance of TokenEvaluatorFactoryTestContext
     */
    public TokenEvaluatorFactoryTestContext(final String query) {
        this.query = query;
    }

    public String getQueryString() {
        return query;
    }

    public Properties getApplicationProperties() {
        return FileResourcesSiteConfigurationTest.valueOf(Site.DEFAULT).getProperties();
    }

    public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
        return FileResourceLoader.newPropertiesLoader(this, resource, properties);
    }

    public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
        return FileResourceLoader.newDocumentLoader(this, resource, builder);
    }

    public Site getSite()  {
        return Site.DEFAULT;
    }

}

/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 * TokenEvaluationEngineTestContext.java
 *
 * Created on 8. april 2006, 01:17
 *
 */

package no.sesat.search.query.token;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.site.SiteTestCase;
import no.sesat.search.site.config.*;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;

/**
 *
 * @author mick
 */
public final class TokenEvaluationEngineTestContext extends SiteTestCase implements TokenEvaluationEngineImpl.Context{

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
        return FileResourcesSiteConfigurationTest.valueOf(getTestingSite()).getProperties();
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


    /** TODO comment me. **/
    public Site getSite()  {
        return getTestingSite();
    }

}

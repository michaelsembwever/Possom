/* Copyright (2006-2007) Schibsted Søk AS
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
 *
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

/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Created on 23 January 2006, 13:54
 */

package no.schibstedsok.searchportal.site.config;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.searchportal.site.SiteContext;

/** Defines the context for consumers of DocumentLoaders.
 *
 * @version $Id: ResourceContext.java 2045 2006-01-25 12:10:01Z mickw $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface DocumentContext extends BaseContext {
    /** Create a new DocumentLoader for the given resource name/path and load it with the given DocumentBuilder.
     * @param resource the resource name/path.
     * @param builder the DocumentBuilder to build the DOM resource with.
     * @return the new DocumentLoader to use.
     **/
    DocumentLoader newDocumentLoader(SiteContext siteCxt, String resource, DocumentBuilder builder);
}

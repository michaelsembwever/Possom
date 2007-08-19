/*
 * UrlRewriteFilter.java
 *
 * Created on 19 March 2007, 21:27
 *
 */

package no.sesat.searchportal.http.filters;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.searchportal.http.urlrewrite.UrlRewriterContainerFactory;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.site.config.DocumentLoader;
import no.sesat.searchportal.site.config.UrlResourceLoader;
import org.tuckey.web.filters.urlrewrite.UrlRewriterContainer;

/** Override of tuckey's UrlRewriteFilter that supplies an inputstream to the skins's urlrewrite.xml
 *   instead of the default /WEB-INF/urlrewrite.xml
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class UrlRewriteFilter extends org.tuckey.web.filters.urlrewrite.UrlRewriteFilter{
    
    // Constants -----------------------------------------------------
    
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /** {@inherit} **/
    @Override
    public UrlRewriterContainer getUrlRewriterContainer(final ServletRequest request){
        
        UrlRewriterContainer result = null;
        if(request instanceof HttpServletRequest){
            final HttpServletRequest httpRequest = (HttpServletRequest)request;
            final Site site = (Site) httpRequest.getAttribute(Site.NAME_KEY);
            
            final UrlRewriterContainerFactory factory = UrlRewriterContainerFactory.valueOf(
                    new UrlRewriterContainerFactory.Context(){
                        public DocumentLoader newDocumentLoader(SiteContext siteCxt,
                                                                String resource,
                                                                DocumentBuilder builder) {

                            return UrlResourceLoader.newDocumentLoader(siteCxt, resource, builder);
                        }
                        public Site getSite() {
                            return site;
                        }
            });
        
            result = factory.getUrlRewriterContainer();
            
            if(!result.isLoaded()){
                result.init(getFilterConfig());
                result.loadConf();
            }
        }
        return result;
    }

    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}

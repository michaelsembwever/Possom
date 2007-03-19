/*
 * UrlRewriteFilter.java
 *
 * Created on 19 March 2007, 21:27
 *
 */

package no.schibstedsok.searchportal.http.filters;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.http.urlrewrite.UrlRewriterContainerFactory;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;
import org.tuckey.web.filters.urlrewrite.UrlRewriterContainer;

/**
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
        return null != result ? result : super.getUrlRewriterContainer(request);
    }

    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}

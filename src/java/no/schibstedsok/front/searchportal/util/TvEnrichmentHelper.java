/*
 * TvEnrichmentHelper.java
 *
 * Created on 5. mai 2006, 10:18
 *
 * Helper class to merge TvEnrichment results.
 */

package no.schibstedsok.front.searchportal.util;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import no.schibstedsok.front.searchportal.output.VelocityResultHandler;
import no.schibstedsok.front.searchportal.result.Enrichment;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.view.i18n.TextMessages;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.Page;


/**
 * Helper class to merge TvEnrichment results.
 *
 * @author <a href="mailto:anders.johan.jamtli@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Revision$</tt>
 */
public class TvEnrichmentHelper {
    
    /** Creates a new instance of TvEnrichmentHelper */
    public TvEnrichmentHelper() {
        
    }
    
    /** 
     * Loop through the enrichments picking out webtv and tv results, merging the results using velocity template.
     * 
     * @param request   The request we are handling
     * @param engine    Instance of the Velocity engine
     * @param template  Velocity template to use when merging results
     */
    public static void mergeEnrichments(
            final HttpServletRequest request, final VelocityEngine engine, final Page siteMeshPage, final String template) {
        Enrichment webtvEnrich = null;
        Enrichment tvEnrich = null;
        
        final List <Enrichment> enrichments = (List <Enrichment>) request.getAttribute("enrichments");
        final Site site = (Site)request.getAttribute(Site.NAME_KEY);
        final TextMessages text = (TextMessages) request.getAttribute("text");
        
        for (Enrichment ee : enrichments) {
           if ("webtvEnrich".equals(ee.getName())) {
                webtvEnrich = ee;
           } else if ("tvEnrich".equals(ee.getName())) {
               tvEnrich = ee;
           }
        }
        
        request.setAttribute("webtvEnrich", webtvEnrich);
        request.setAttribute("tvEnrich", tvEnrich);

        if (webtvEnrich != null || tvEnrich != null) {
            final Template tvTemplate = VelocityResultHandler.getTemplate(engine, site, "tvEnrichMerge");
            final VelocityContext context = VelocityResultHandler.newContextInstance(engine);
            context.put("page", siteMeshPage);
            context.put("text", text);
            context.put("webtvenrich", webtvEnrich);
            context.put("tvenrich", tvEnrich);

            StringWriter sw = new StringWriter();
            try {
                tvTemplate.merge(context, sw);
            } catch (Exception e) {
                e.getStackTrace();
            }
            
            String tvEnrichStr = sw.toString();
            request.setAttribute("tvEnrichStr", tvEnrichStr);
        }
    }
}

// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result;

import no.geodata.maputil.CoordHelper;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.i18n.TextMessages;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.util.PagingDisplayHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Category;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.net.URLEncoder;

/** Handles the populating the velocity contexts.
 * Strictly view domain.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class VelocityResultHandler implements ResultHandler {

    private static Log log = LogFactory.getLog(VelocityResultHandler.class);
    private static final String VELOCITY_LOG_CATEGORY = "org.apache.velocity";

    static {
        initVelocity();
    }

    public void handleResult(final SearchResult result, final Map parameters) {

        if (log.isDebugEnabled()) {
            log.debug("ENTR: handleResult()");
        }
        // This requirement of the users of this class to send the web stuff
        // as parameters is a bit too implicit...

        HttpServletRequest request = (HttpServletRequest) parameters.get("request");
        HttpServletResponse response = (HttpServletResponse) parameters.get("response");

        if (request == null || response == null) {
            throw new IllegalStateException("Both request and response must be set in the parameters");
        }
        Writer w = new StringWriter();
        SearchConfiguration searchConfiguration = result.getSearchCommand().getSearchConfiguration();
        try {
            if (log.isDebugEnabled()) {
                log.debug("handleResult: Looking for template: " + searchConfiguration + searchConfiguration.getName() + ".vm");
            }
            Template template = Velocity.getTemplate(searchConfiguration.getName() + ".vm");
            if (log.isDebugEnabled()) {
                log.debug("handleResult: Created Template=" + template.getName());
            }
            VelocityContext context = new VelocityContext();
            populateVelocityContext(context, result, request, response);
            template.merge(context, w);
            response.getWriter().write(w.toString());
        } catch (Exception e) {
            throw new InfrastructureException(e);
        }
    }

    protected void populateVelocityContext(final VelocityContext context,
                                           final SearchResult result,
                                           final HttpServletRequest request,
                                           final HttpServletResponse response) {

        if (log.isDebugEnabled()) {
            log.debug("ENTR: populateVelocityContext()");
        }
        String queryString = result.getSearchCommand().getQuery().getQueryString();

        String queryStringURLEncoded = null;

        try {
            queryStringURLEncoded = URLEncoder.encode(queryString, "UTF-8");
            queryString = StringEscapeUtils.escapeHtml(queryString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        context.put("result", result);
        context.put("request", request);
        context.put("response", response);
        context.put("query", queryStringURLEncoded);
        context.put("globalSearchTips", ((RunningQuery) request.getAttribute("query")).getGlobalSearchTips());
        context.put("command", result.getSearchCommand());
        context.put("queryHTMLEscaped", queryString);
        context.put("locale", result.getSearchCommand().getQuery().getLocale());
        context.put("text", TextMessages.getMessages());
        context.put("currentTab", result.getSearchCommand().getQuery().getSearchMode());
        context.put("coordHelper", new CoordHelper());
        context.put("contextPath", request.getContextPath());
        context.put("hashGenerator", request.getAttribute("hashGenerator"));

        SearchConfiguration config = result.getSearchCommand().getSearchConfiguration();

        if (config.isPagingEnabled()) {
            PagingDisplayHelper pager = new PagingDisplayHelper(result.getHitCount(), config.getResultsToReturn(), 10);
            pager.setCurrentOffset(result.getSearchCommand().getQuery().getOffset());
            context.put("pager", pager);
        }

        Linkpulse linkpulse = new Linkpulse(XMLSearchTabsCreator.valueOf(Site.DEFAULT).getProperties());
        context.put("linkpulse", linkpulse);
    }

    private static void initVelocity() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: initVelocity()");
        }
        try {

            Category category = Category.getInstance(VELOCITY_LOG_CATEGORY);
            Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            Velocity.setProperty("runtime.log.logsystem.log4j.category", category.getName());
            Velocity.setProperty(Velocity.RESOURCE_LOADER, "class");
            Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.setProperty("class.resource.loader.cache", "true");
            Velocity.setProperty("class.resource.loader.modificationCheckInterval", "-1");
            Velocity.init();
        } catch (Exception e) {
            throw new InfrastructureException(e);
        }
    }
}

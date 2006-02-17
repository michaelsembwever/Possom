// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.output;

import java.util.Properties;
import no.geodata.maputil.CoordHelper;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.i18n.TextMessages;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.result.Decoder;
import no.schibstedsok.front.searchportal.result.Linkpulse;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import no.schibstedsok.front.searchportal.velocity.VelocityEngineFactory;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.util.PagingDisplayHelper;
import no.schibstedsok.front.searchportal.util.TradeDoubler;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.MathTool;

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
public final class VelocityResultHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(VelocityResultHandler.class);

    public void handleResult(final Context cxt, final Map parameters) {

        // Skip this result handler if xml is wanted.
        String[] xmlParam = (String[]) parameters.get("xml");
        if (xmlParam != null && xmlParam[0].equals("yes")) {
            return;
        }

        LOG.trace("handleResult()");

        // This requirement of the users of this class to send the web stuff
        // as parameters is a bit too implicit...

        final HttpServletRequest request = (HttpServletRequest) parameters.get("request");
        final HttpServletResponse response = (HttpServletResponse) parameters.get("response");

        if (request == null || response == null) {
            throw new IllegalStateException("Both request and response must be set in the parameters");
        }

        final Writer w = new StringWriter();
        final SearchConfiguration searchConfiguration = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("handleResult: Looking for template: " + searchConfiguration + searchConfiguration.getName() + ".vm");
            }

            final Site site = cxt.getSite();
            final String templateUrl = site.getTemplateDir() + "/" + searchConfiguration.getName() + ".vm";

            final VelocityEngine engine = VelocityEngineFactory.valueOf(new VelocityEngineFactory.Context() {
                public Site getSite() {
                    return site;
                }
            });

            final Template template = engine.getTemplate(templateUrl);

            if (LOG.isDebugEnabled()) {
                LOG.debug("handleResult: Created Template=" + template.getName());
            }

            final VelocityContext context = new VelocityContext();
            populateVelocityContext(context, cxt, request, response);
            template.merge(context, w);
            response.getWriter().write(w.toString());

        } catch (Exception e) {
            throw new InfrastructureException(e);
        }
    }

    protected void populateVelocityContext(final VelocityContext context,
                                           final Context cxt,
                                           final HttpServletRequest request,
                                           final HttpServletResponse response) {

        LOG.trace("populateVelocityContext()");

        String queryString = cxt.getQueryString();

        String queryStringURLEncoded = null;

        try {
            queryStringURLEncoded = URLEncoder.encode(queryString, "UTF-8");
            queryString = StringEscapeUtils.escapeHtml(queryString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        context.put("result", cxt.getSearchResult());
        context.put("request", request);
        context.put("response", response);
        context.put("query", queryStringURLEncoded);
        context.put("globalSearchTips", ((RunningQuery) request.getAttribute("query")).getGlobalSearchTips());
        context.put("command", cxt.getSearchResult().getSearchCommand());
        context.put("queryHTMLEscaped", queryString);
        context.put("locale", cxt.getSite().getLocale());
        context.put("text", TextMessages.valueOf(new TextMessages.Context() {
            public Site getSite() {
                return cxt.getSite();
            }
            public PropertiesLoader newPropertiesLoader(final String rsc, final Properties props) {
                return cxt.newPropertiesLoader(rsc, props);
            }
        }));
        context.put("currentTab", cxt.getSearchResult().getSearchCommand().getRunningQuery().getSearchMode());
        context.put("coordHelper", new CoordHelper());
        context.put("contextPath", request.getContextPath());
        context.put("hashGenerator", request.getAttribute("hashGenerator"));
        context.put("runningQuery", cxt.getSearchResult().getSearchCommand().getRunningQuery());
        context.put("math", new MathTool());
        context.put("site", cxt.getSite());
        context.put("tradedoubler", new TradeDoubler(request));

        final SearchConfiguration config = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();

        if (config.isPagingEnabled()) {
            PagingDisplayHelper pager = new PagingDisplayHelper(cxt.getSearchResult().getHitCount(), config.getResultsToReturn(), 10);
            pager.setCurrentOffset(cxt.getSearchResult().getSearchCommand().getRunningQuery().getOffset());
            context.put("pager", pager);
        }

        final Linkpulse linkpulse = new Linkpulse(XMLSearchTabsCreator.valueOf(cxt.getSite()).getProperties());
        context.put("linkpulse", linkpulse);

        final Decoder decoder = new Decoder();
        context.put("decoder", decoder);
    }
}

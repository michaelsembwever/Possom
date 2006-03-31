// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.servlet;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.SearchTabs;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.util.QueryStringHelper;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.UrlResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.query.run.QueryFactory;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.i18n.TextMessages;
import org.apache.commons.lang.time.StopWatch;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.log4j.Logger;

/** The Central Controller to incoming queries.
 * Controls the SearchMode -> RunningQuery creation and handling.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class SearchServlet extends HttpServlet {

    /** The serialVersionUID. */
    private static final long serialVersionUID = 3068140845772756438L;

    private static final Logger LOG = Logger.getLogger(SearchServlet.class);


    private SearchTabs tabs;

    /** {@inheritDoc}
     */
    public void destroy() {
        tabs.stopAll();
    }

    /** {@inheritDoc}
     */
    protected void doGet(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse)
                throws ServletException, IOException {


        if (httpServletRequest.getParameter("q") == null) {
            String redir = httpServletRequest.getContextPath();
            if (redir == null) { redir = "/"; }
            if (!redir.endsWith("/")) {
                LOG.debug("doGet: Adding / to " + redir);

                redir += "/";
            }

            LOG.info("doGet(): Empty Query String redirect=" + redir);

            httpServletResponse.sendRedirect(redir);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("ENTR: doGet()");
            LOG.debug("Character encoding ="  + httpServletRequest.getCharacterEncoding());
        }

        StopWatch stopWatch = null;
        if (LOG.isInfoEnabled()) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }

        final Site site = (Site) httpServletRequest.getAttribute(Site.NAME_KEY);

        if (tabs == null
                || (httpServletRequest.getParameter("reload") != null
                        && httpServletRequest.getParameter("reload").equals("tabs"))) {

            LOG.info("doGet(): ReLoading tabs");

            tabs = loadSearchTabs(site);
            LOG.warn("Tabs reloaded");
        }

        final String xmlParam = httpServletRequest.getParameter("xml");

        if (xmlParam != null && xmlParam.equals("yes")) {
            httpServletResponse.setContentType("text/xml; charset=utf-8");
        } else {
            httpServletResponse.setContentType("text/html; charset=utf-8");
        }
        httpServletResponse.setCharacterEncoding("UTF-8"); // correct encoding

        String searchModeKey = httpServletRequest.getParameter("c");

        if (searchModeKey == null) {
            searchModeKey = "d";
        }

        final SearchMode mode = tabs.getSearchMode(searchModeKey);

        final RunningQuery.Context rqCxt = new RunningQuery.Context() {
            // <editor-fold defaultstate="collapsed" desc=" RunningQuery.Context ">
            public SearchMode getSearchMode() {
                return mode;
            }

            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return UrlResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return UrlResourceLoader.newXStreamLoader(this, resource, xstream);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return UrlResourceLoader.newDocumentLoader(this, resource, builder);
            }

            public Site getSite() {
                return site;
            }
            //</editor-fold>
        };

        final RunningQuery query = QueryFactory.getInstance()
            .createQuery(rqCxt, httpServletRequest, httpServletResponse);

        httpServletRequest.setAttribute("locale", query.getLocale());
        httpServletRequest.setAttribute("query", query);
        httpServletRequest.setAttribute("site", site);
        httpServletRequest.setAttribute("text", TextMessages.valueOf(new TextMessages.Context() {
                // <editor-fold defaultstate="collapsed" desc=" TextMessages.Context ">
                public Site getSite() {
                    return site;
                }
                public PropertiesLoader newPropertiesLoader(final String rsc, final Properties props) {
                    return UrlResourceLoader.newPropertiesLoader(this, rsc, props);
                }
                // </editor-fold>
            }));

        if (httpServletRequest.getParameter("offset") != null
                && !"".equals(httpServletRequest.getParameter("offset"))) {

            query.setOffset(Integer.parseInt(httpServletRequest.getParameter("offset")));
        }

        if (httpServletRequest.getParameter("q") != null) {
            httpServletRequest.setAttribute("q", httpServletRequest.getParameter("q"));
        }

        httpServletRequest.setAttribute("c", searchModeKey);

        try {
                query.run();
        } catch (InterruptedException e) {
            LOG.error("Task timed out");
        }

        if (LOG.isInfoEnabled()) {
            stopWatch.stop();
            LOG.info("doGet(): Search took " + stopWatch + " " + query.getQueryString());
        }
    }
    
    private SearchTabs loadSearchTabs(final Site site) {
        return XMLSearchTabsCreator.valueOf(site).getSearchTabs();
    }


}

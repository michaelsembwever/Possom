package no.schibstedsok.front.searchportal.servlet;

import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.SearchTabs;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.query.QueryFactory;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.i18n.TextMessages;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchServlet extends HttpServlet {

    /** The serialVersionUID */
    private static final long serialVersionUID = 3068140845772756438L;

    private static Log log = LogFactory.getLog(SearchServlet.class);

    SearchTabs tabs;

    public void destroy() {
        tabs.stopAll();
    }

    public void init() throws ServletException {
        if(log.isInfoEnabled()){
            log.info("init():  Loading tabsfile");
        }
        try{
            tabs = XMLSearchTabsCreator.getInstance().createSearchTabs();
        }catch(Exception e){
            log.error("init", e);
            throw new ServletException("Failed to load tabsfile");
        }
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

        if(httpServletRequest.getParameter("q") == null){
            String redir = httpServletRequest.getContextPath();
            if(redir == null) { redir = "/"; }
            if(!redir.endsWith("/")){
                if(log.isDebugEnabled()){
                    log.debug("doGet: Adding / to " + redir);
                }
                redir += "/";
            }

            if(log.isInfoEnabled()){
                log.info("doGet(): Empty Query String redirect=" + redir);
            }
            httpServletResponse.sendRedirect(redir);
            return ;
        }

        if (log.isDebugEnabled()) {
            log.debug("ENTR: doGet()");
            log.debug("Character encoding ="  + httpServletRequest.getCharacterEncoding());
        }

        StopWatch stopWatch = null;
        if (log.isInfoEnabled()) {
            stopWatch = new StopWatch();
            stopWatch.start();
        }
        if (httpServletRequest.getParameter("reload") != null) {
            if (httpServletRequest.getParameter("reload").equals("tabs")) {
                if(log.isInfoEnabled()){
                    log.info("doGet(): (Re)Loading tabs");
                }
                tabs = XMLSearchTabsCreator.getInstance().createSearchTabs();
                log.warn("Tabs reloaded");
            }
        }

        httpServletResponse.setContentType("text/html; charset=utf-8");
        String searchModeKey = httpServletRequest.getParameter("c");

        if (searchModeKey == null) {
            searchModeKey = "d";
        }

        SearchMode mode = tabs.getSearchMode(searchModeKey);
        RunningQuery query = QueryFactory.getInstance().
                createQuery(mode, httpServletRequest, httpServletResponse);

        httpServletRequest.setAttribute("locale", query.getLocale());
        httpServletRequest.setAttribute("query", query);
        httpServletRequest.setAttribute("text", TextMessages.getMessages());

        if (httpServletRequest.getParameter("offset") != null &&
                !"".equals(httpServletRequest.getParameter("offset")))
        {
            query.setOffset(Integer.parseInt(httpServletRequest.getParameter("offset")));
        }

        if (httpServletRequest.getParameter("q") != null) {
            httpServletRequest.setAttribute("q", httpServletRequest.getParameter("q"));
        }

        httpServletRequest.setAttribute("c", searchModeKey);

        try {
                query.run();
        } catch (InterruptedException e) {
            log.error("Task timed out");
        }

        if (log.isInfoEnabled()) {
            stopWatch.stop();
            log.info("doGet(): Search took " + stopWatch + " " + query.getQueryString());
        }
    }
}

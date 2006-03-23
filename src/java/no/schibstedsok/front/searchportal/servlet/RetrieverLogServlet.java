// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Logs the viewings for 'papiraviser' and the news source
 *
 * @author <a href="mailto:thomas.kjerstad@schibsted.no">Thomas Kjaerstad</a>.
 * @version <tt>$Revision$</tt>
 *
 */
public final class RetrieverLogServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger("no.schibstedsok.Statistics");

    public void destroy() {  }
    public void init() {  }

    protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

        final String type = req.getParameter("type");

        //logs click on homepage link from yellow resultpage and from infopage
        if ("homepage".equals(type)) {

            LOG.info(
                    "<company-info type=\"" + req.getParameter("c") + "_hp\">"
                        + "<query>" + req.getParameter("q") + "</query>"
                        + ("y".equals(req.getParameter("c"))
                            ? "<y-position>" + req.getParameter("ypos") + "</y-position>"
                            : "")
                        + "<name>" + req.getParameter("name") + "</name>"
                    + "</company-info>");

        } else {

            LOG.info("<retriever-info name=\"papiraviser - " + req.getParameter("paper") + "\"\\>");
        }
        // clients must not cache these requests
        res.setHeader("Cache-Control", "no-cache, must-revalidate, post-check=0, pre-check=0");
        res.setHeader("Pragma", "no-cache"); // for old browsers
        res.setDateHeader("Expires", 0); // to be double-safe

    }
}

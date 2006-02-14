package no.schibstedsok.front.searchportal.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

import no.schibstedsok.front.searchportal.command.AbstractSearchCommand;

/**
 * Logs the viewings for 'papiraviser' and the news source
 *
 * @author <a href="mailto:thomas.kjerstad@schibsted.no">Thomas Kjaerstad</a>.
 * @version <tt>$Revision$</tt>
 * 
 */
public class RetrieverLogServlet extends HttpServlet {

    public void destroy() {}
    public void init() {}

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String type = req.getParameter("type");

        //logs click on homepage link from yellow resultpage and from infopage
        if ("homepage".equals(type)) {
            String ypos = "";
            if ("y".equals(req.getParameter("c")))
                ypos = " ypos: " + req.getParameter("ypos");
            Log log = LogFactory.getLog(AbstractSearchCommand.class);
            log.info("STATISTICS: " + req.getParameter("c") + "_hp company: " + req.getParameter("name") + " q: " + req.getParameter("q") + ypos);
            res.setHeader("Cache-Control", "no-cache");
        } else {
            String paper = req.getParameter("paper");
            Log log = LogFactory.getLog(AbstractSearchCommand.class);
            log.info("STATISTICS: papiraviser - " + paper);
            res.setHeader("Cache-Control", "no-cache");
        }

    }
}

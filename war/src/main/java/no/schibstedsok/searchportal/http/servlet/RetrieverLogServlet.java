// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.http.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Logs different statistics with ajax
 * XXX Rename to BoomerangServlet
 *
 * @author <a href="mailto:thomas.kjerstad@schibsted.no">Thomas Kjaerstad</a>.
 * @version <tt>$Id: 3361 $</tt>
 *
 */
public final class RetrieverLogServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger("no.schibstedsok.Statistics");
    private static final Logger ACCESS = Logger.getLogger("no.schibstedsok.Access");


    @Override
    public void destroy() {  }
    
    @Override
    public void init() {  }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res) 
            throws ServletException, IOException {
        
        final DataModel datamodel = (DataModel) req.getSession().getAttribute(DataModel.KEY);
        final ParametersDataObject parametersDO = datamodel.getParameters();

        final StringDataObject paper = parametersDO.getValue("paper");

        // news statistics is treated as before until Bernt is ready to adapt to new format
        if (paper != null) {
            LOG.info("<retriever-info name=\"papiraviser - " 
                    + paper.getXmlEscaped() 
                    + "\"/><!-- use boomerang instead -->");
        } else {
            LOG.info(
                    "<view-info>"
                    + "<collection>" + req.getParameter("c") + "</collection>"
                    + "<type>" + StringEscapeUtils.escapeXml(req.getParameter("type")) + "</type>"
                    + "<query>" + StringEscapeUtils.escapeXml(req.getParameter("q")) + "</query>"
                    + "<name>" + URLEncoder.encode(req.getParameter("name"), "utf-8") + "</name>"
                    + (null != req.getParameter("pos") ? "<position>" + req.getParameter("pos") + "</position>" : "")
                    + "</view-info><!-- use boomerang instead -->");
        }
        
        // the new format
        final List<String> paramKeys = new ArrayList<String>(parametersDO.getValues().keySet());
        Collections.sort(paramKeys);
        
        final StringBuilder bob = new StringBuilder("<boomerang>");
        for(String key : paramKeys){
            bob.append('<' + key + '>' + parametersDO.getValue(key).getXmlEscaped() + "</" + key + '>');
        }
        bob.append("</boomerang>");
        ACCESS.info(bob.toString());
        
        // clients must not cache these requests
        res.setHeader("Cache-Control", "no-cache, must-revalidate, post-check=0, pre-check=0");
        res.setHeader("Pragma", "no-cache"); // for old browsers
        res.setDateHeader("Expires", 0); // to be double-safe

    }
}

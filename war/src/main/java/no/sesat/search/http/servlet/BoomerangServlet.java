/*
 * Copyright (2006-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.http.servlet;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Provides the user-statistics logging in Sesat.
 * Links are logged with <b>ceremonial</b> boomerangs that come back (ie with a redirect response).
 * Javascript functionality (or user behavour) is logged with <b>hunting</b> boomerangs that do not come back.
 *
 * A cermonial example is:
 * http://sesam.no/boomerang/category=results;subcategory=main/http://wever.org
 *
 *
 * A hunting example is:
 * http://sesam.no/hunting/?parameter-list
 *
 *
 * @author <a href="mailto:thomas.kjerstad@sesam.no">Thomas Kjaerstad</a>
 * @author <a href="mailto:mick@wever.org">Mck</a>
 * @version <tt>$Id: 3361 $</tt>
 *
 */
public final class BoomerangServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(BoomerangServlet.class);
    private static final Logger ACCESS = Logger.getLogger("no.sesat.Access");

    private static final String CEREMONIAL = "/boomerang/";

    private static final Pattern ROBOTS = Pattern.compile("(Googlebot|Slurp|Crawler|Bot)", Pattern.CASE_INSENSITIVE);

    @Override
    public void destroy() {  }

    @Override
    public void init() {  }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {

        // clients must not cache these requests
        res.setHeader("Cache-Control", "no-cache, must-revalidate, post-check=0, pre-check=0");
        res.setHeader("Pragma", "no-cache"); // for old browsers
        res.setDateHeader("Expires", 0); // to be double-safe

        if(req.getRequestURI().startsWith(CEREMONIAL)){

            // ceremonial boomerang
            final StringBuffer url = req.getRequestURL();
            if(null != req.getQueryString()){
                url.append('?' + req.getQueryString());
            }

            // pick out the entrails
            final int boomerangStart = url.indexOf(CEREMONIAL) + CEREMONIAL.length();

            try{
                final String grub = url.substring(boomerangStart, url.indexOf("/", boomerangStart));
                LOG.debug(grub);

                // the url to return to
                final String destination = url.substring(
                        url.indexOf("/", url.indexOf(CEREMONIAL) + CEREMONIAL.length() + 1) + 1);

                final Map<String,String> entrails = new HashMap<String,String>();

                // request attribute to keep
                entrails.put("referer", req.getHeader("Referer"));
                entrails.put("method", req.getMethod());
                entrails.put("ipaddress", req.getRemoteAddr());
                entrails.put("user-agent", req.getHeader("User-Agent"));
                entrails.put("user-id", SearchServlet.getCookieValue(req, "SesamID"));
                entrails.put("user", SearchServlet.getCookieValue(req, "SesamUser"));

                // the grub details to add
                if(0 < grub.length()){
                    final StringTokenizer tokeniser = new StringTokenizer(grub, ";");
                    while(tokeniser.hasMoreTokens()){
                        final String[] entry = tokeniser.nextToken().split("=");
                        entrails.put(entry[0], 1 < entry.length ? entry[1] : entry[0]);
                    }
                }
                entrails.put("boomerang", destination);
                kangerooGrub(entrails);

                LOG.debug("Ceremonial boomerang to " + destination.toString());

                if(ROBOTS.matcher(req.getHeader("User-agent")).find()){
                    // robots like permanent redirects. and we're not interested in their clicks so ok to cache.
                    res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    res.setHeader("Location", destination.toString());
                    res.setHeader("Connection", "close");

                }else{
                    // default behaviour for users.
                    res.sendRedirect(destination.toString());
                }

            }catch(StringIndexOutOfBoundsException sioobe){
                // SEARCH-4668
                LOG.error("Boomerang url not to standard --> " + url);
                LOG.debug(sioobe.getMessage(), sioobe);
            }

        }else{

            // hunting boomerang, just grub, and the grub comes as clean parameters.
            final DataModel datamodel = (DataModel) req.getSession().getAttribute(DataModel.KEY);
            kangerooGrub(datamodel.getParameters().getValues());

        }

    }

    private void kangerooGrub(final Map<String,?> params){

        final List<String> paramKeys = new ArrayList<String>(params.keySet());

        Collections.sort(paramKeys);

        final StringBuilder bob = new StringBuilder("<boomerang>");

        for(String key : paramKeys){
            try {

                final String value = params.get(key) instanceof StringDataObject
                        ? ((StringDataObject) params.get(key)).getXmlEscaped()
                        : StringEscapeUtils.escapeXml((String) params.get(key));

                final String keyEscaped = StringEscapeUtils.escapeXml(URLDecoder.decode(key, "UTF-8"));

                bob.append("<parameter key=\"" + keyEscaped + "\" value=\"" + value + "\"/>");

            }catch (UnsupportedEncodingException ex) {
                LOG.error("Failed to kangerooGrub " + key, ex);
            }
        }
        bob.append("</boomerang>");
        ACCESS.info(bob.toString());
    }
}

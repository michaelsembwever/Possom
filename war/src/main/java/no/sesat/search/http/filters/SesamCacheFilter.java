/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.http.filters;

import com.opensymphony.oscache.web.filter.CacheFilter;

import javax.servlet.ServletRequest;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SesamCacheFilter extends CacheFilter {


    protected boolean isCacheable(ServletRequest servletRequest) {
        return servletRequest.getParameter("c") != null && servletRequest.getParameter("c").equals("d");
    }
}

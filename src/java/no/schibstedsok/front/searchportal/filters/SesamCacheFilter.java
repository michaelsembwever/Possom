package no.schibstedsok.front.searchportal.filters;

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

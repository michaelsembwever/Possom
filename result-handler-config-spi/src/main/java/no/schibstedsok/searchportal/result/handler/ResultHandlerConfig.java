/*
 * ResultHandlerConfig.java
 *
 * Created on 26 March 2007, 17:10
 *
 */

package no.schibstedsok.searchportal.result.handler;

import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public interface ResultHandlerConfig {
    /**
     * 
     * @param element 
     * @return 
     */
    ResultHandlerConfig readResultHandler(Element element);
}

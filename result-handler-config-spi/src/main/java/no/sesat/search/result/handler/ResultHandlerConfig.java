/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * ResultHandlerConfig.java
 *
 * Created on 26 March 2007, 17:10
 *
 */

package no.sesat.search.result.handler;

import java.io.Serializable;
import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public interface ResultHandlerConfig extends Serializable {
    /**
     * 
     * @param element 
     * @return 
     */
    ResultHandlerConfig readResultHandler(Element element);
}

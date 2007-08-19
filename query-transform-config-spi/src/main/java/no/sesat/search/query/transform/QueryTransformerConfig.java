/*
 * QueryTransformerConfig.java
 *
 * Created on 26 March 2007, 17:10
 *
 */

package no.sesat.search.query.transform;

import java.io.Serializable;
import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public interface QueryTransformerConfig extends Serializable {
    /**
     * 
     * @param element 
     * @return 
     */
    QueryTransformerConfig readQueryTransformer(Element element);
}

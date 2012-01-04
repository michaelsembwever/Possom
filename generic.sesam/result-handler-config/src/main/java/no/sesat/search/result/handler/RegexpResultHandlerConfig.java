/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 * You can use, redistribute, and/or modify it, under the terms of the Possom License.
 * You should have received a copy of the Possom License along with this program.
 * If not, see https://dev.sesat.no/confluence/display/Possom/Possom+License

 */
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;


/**
 * Applies a regular expression to a specified field in every result item
 *  adding a target field matching the first capturing group in the regular expression.
 *
 *
 * @version $Id$
 */
@Controller("RegexpResultHandler")
public final class RegexpResultHandlerConfig extends AbstractResultHandlerConfig {

    private String field;
    private String target;
    private String regexp;

    /**
     *
     * @param field
     */
    public void setField(final String field){
        this.field = field;
    }

    /**
     *
     * @return
     */
    public String getField(){
        return field;
    }

    /**
     *
     * @param target
     */
    public void setTarget(final String target){
        this.target = target;
    }

    /**
     *
     * @return
     */
    public String getTarget(){
        return target;
    }

    /**
     *
     * @param regexp
     */
    public void setRegexp(final String regexp){
        this.regexp = regexp;
    }

    /**
     *
     * @return
     */
    public String getRegexp(){
        return regexp;
    }

    @Override
    public RegexpResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);
        AbstractDocumentFactory.fillBeanProperty(this, null, "field", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, null, "target", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, null, "regexp", ParseType.String, element, null);
        return this;
    }

}

/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.searchportal.query.transform;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * AbstractQueryTransformerConfig
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>

 * @vesrion $Id$
 */
public abstract class AbstractQueryTransformerConfig implements QueryTransformerConfig{

    private static final Logger LOG = Logger.getLogger(AbstractQueryTransformerConfig.class);


    /** Only to be used by XStream and tests **/
    protected AbstractQueryTransformerConfig(){
    }

    /**
     *
     * @param element
     * @return
     */
    //@Override // TODO uncomment for java 6
    public AbstractQueryTransformerConfig readQueryTransformer(final Element element){

        // Override me to add custom deserialisation
        return this;
    }

    /**
     *
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    public @interface Controller {
        /**
         *
         * @return
         */
        public String value();
    }
}

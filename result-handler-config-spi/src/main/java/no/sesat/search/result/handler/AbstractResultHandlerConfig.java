/* Copyright (2006-2008) Schibsted Søk AS
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
package no.sesat.search.result.handler;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * AbstractResultHandlerConfig
 *
 *

 * @vesrion $Id$
 */
public abstract class AbstractResultHandlerConfig implements ResultHandlerConfig{

    private static final Logger LOG = Logger.getLogger(AbstractResultHandlerConfig.class);


    /** Only to be used by XStream and tests **/
    protected AbstractResultHandlerConfig(){
    }

    /**
     *
     * @param element
     * @return
     */
    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element){

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

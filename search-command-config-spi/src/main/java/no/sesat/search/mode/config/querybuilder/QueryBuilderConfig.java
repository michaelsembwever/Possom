/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
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
package no.sesat.search.mode.config.querybuilder;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.w3c.dom.Element;

/** Configuration pojo behind any QueryBuilder.
 *
 * @version $Id$
 */
public interface QueryBuilderConfig extends Serializable{

    public interface ModesW3cDomDeserialiser{
        /** Deserialisation from an w3c element.
         *
         * @param element
         * @return
         */
        QueryBuilderConfig readQueryBuilder(Element element);
    }

    /**
     * Used to indicate Controlling class to use.
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

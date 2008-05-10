/* Copyright (2007) Schibsted Søk AS
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
/*
 * DataObject.java
 *
 * Created on 22 January 2007, 21:26
 *
 */

package no.sesat.search.datamodel.generic;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Data information holding attributes.
 * Essentially a JavaBean, with properties implemented with getters and setters.
 *
 * This is an annotation rather than a "marker interface".
 *
 *
 * @version <tt>$Id$</tt>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface DataObject {

    public final class Property implements Serializable {
        private final String name;
        private final Object value;

        private Property() {
            name = null;
            value = null;
        }

        public Property(final String name, final Object value){
            this.name = name;
            this.value = value;
        }
        public String getName(){
            return name;
        }
        public Object getValue(){
            return value;
        }
    }
}

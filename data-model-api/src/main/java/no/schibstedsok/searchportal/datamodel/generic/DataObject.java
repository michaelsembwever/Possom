/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
/*
 * DataObject.java
 *
 * Created on 22 January 2007, 21:26
 *
 */

package no.schibstedsok.searchportal.datamodel.generic;

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
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
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

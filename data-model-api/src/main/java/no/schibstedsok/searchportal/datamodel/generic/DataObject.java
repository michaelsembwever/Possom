/*
 * DataObject.java
 *
 * Created on 22 January 2007, 21:26
 *
 */

package no.schibstedsok.searchportal.datamodel.generic;

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

    public final class Property{
        private final String name;
        private final Object value;
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

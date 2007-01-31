/*
 * AccessAllow.java
 *
 * Created on 23 January 2007, 15:05
 *
 */

package no.schibstedsok.searchportal.datamodel.access;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@Documented
@AccessConstraint
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AccessAllow {
    ControlLevel[] value();
}

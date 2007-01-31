/*
 * DataNode.java
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

/** Node information holding heirarchy-related attributes.
 * Provides instantiate...() methods to instantiate and set children dataNodes.
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
public @interface DataNode{}

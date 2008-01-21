/*
 * Copyright (2007) Schibsted Søk AS
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
 *
 * QueryTransformerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.sesat.search.query.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import no.sesat.search.site.config.*;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.Site;

/** Obtain a working QueryTransformer from a given QueryTransformerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class QueryTransformerFactory {

    public interface Context extends SiteContext, BytecodeContext {
    }

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    private Context context;

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    public QueryTransformerFactory(Context context) {
        this.context = context;
    }

    // Public --------------------------------------------------------

    /**
     *
     * @param config
     * @return
     */
    public QueryTransformer getController(final QueryTransformerConfig config){

        final String controllerName = "no.sesat.search.query.transform."
                + config.getClass().getAnnotation(Controller.class).value();

        try{
            final SiteClassLoaderFactory.Context ctrlCxt = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
                public Site getSite() {
                    return context.getSite();
                }
                public Spi getSpi() {
                    return Spi.QUERY_TRANSFORM_CONTROL;
                }
            };

            final ClassLoader ctrlClassLoader = SiteClassLoaderFactory.instanceOf(ctrlCxt).getClassLoader();

            @SuppressWarnings("unchecked")
            final Class<? extends QueryTransformer> cls
                    = (Class<? extends QueryTransformer>)ctrlClassLoader.loadClass(controllerName);

            final Constructor<? extends QueryTransformer> constructor = cls.getConstructor(QueryTransformerConfig.class);

            return constructor.newInstance(config);

        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

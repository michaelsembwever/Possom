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
 *
 * SearchCommandFactory.java
 *
 * Created on January 5, 2006, 10:17 AM
 *
 */

package no.sesat.search.mode;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;

import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeContext;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.Spi;


/** This factory creates the appropriate command for a given SearchConfiguration.
 *
 *
 * @version $Id: SearchCommandFactory.java 3359 2006-08-03 08:13:22Z mickw $
 */
public final class SearchCommandFactory {

    private static final Logger LOG = Logger.getLogger(SearchCommandFactory.class);
    public interface Context extends SiteContext, BytecodeContext {}

    private final Context context;

    /** Create a factory to work within the given context.
     *
     * @param context to work within
     */
    public SearchCommandFactory(final Context context) {
        this.context = context;
    }


    /** Create the appropriate command that's to work within the given SearchCommand.Context.
     *
     * @param cxt to work within
     * @return SearchCommand appropriate for the given context
     */
    public SearchCommand getController(final SearchCommand.Context cxt){

        final SearchConfiguration config = cxt.getSearchConfiguration();

        final String controllerName = "no.sesat.search.mode.command."
                + config.getClass().getAnnotation(Controller.class).value();

        try{

            final SiteClassLoaderFactory.Context classContext = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return Spi.SEARCH_COMMAND_CONTROL;
                        }
                    },
                    context
                );

            final SiteClassLoaderFactory loaderFactory = SiteClassLoaderFactory.instanceOf(classContext);

            @SuppressWarnings("unchecked")
            final Class<? extends SearchCommand> cls
                    = (Class<? extends SearchCommand>) loaderFactory.getClassLoader().loadClass(controllerName);

            final Constructor<? extends SearchCommand> constructor = cls.getConstructor(SearchCommand.Context.class);

            return constructor.newInstance(cxt);

        }
        catch (Exception e) {
            LOG.fatal("Failed to instansiating controller: " + controllerName + " with search configuration: " + config, e);
            throw new IllegalArgumentException(e);
        }
    }
}

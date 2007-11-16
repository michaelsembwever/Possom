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
package no.sesat.search.site.config;


import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.site.SiteContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/** Factory class for deserialising configurations from an xml document.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractConfigFactory<C> {
    
    public interface Context extends BaseContext, SiteContext, BytecodeContext{};

    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(AbstractConfigFactory.class);
    private static final String INFO_CONSTRUCT = "  Construct ";
    
    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    protected AbstractConfigFactory() {
        super();
    }

    // Public --------------------------------------------------------

    public boolean supported(final String xmlName, final Context context) {
        
        try {
            return null != findClass(xmlName, context);
        }catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------

    protected C construct(final Element element, final Context context) {
        
        final String xmlName = element.getTagName();
        LOG.debug(INFO_CONSTRUCT + xmlName);
        try {
            return findClass(xmlName, context).newInstance();
        }catch (InstantiationException ex) {
            throw new RuntimeException(ex); // TODO find better exception to throw
        }catch (IllegalAccessException ex) {
            throw new RuntimeException(ex); // TODO find better exception to throw
        }catch (ClassNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    protected abstract Class<C> findClass(final String xmlName, final Context context) throws ClassNotFoundException;

    @SuppressWarnings(value = "unchecked")
    protected Class<C> loadClass(
            final Context context, 
            final String classNameFQ, 
            final Spi spi) throws ClassNotFoundException {
        
            final SiteClassLoaderFactory.Context c = ContextWrapper.wrap(
                    SiteClassLoaderFactory.Context.class,
                    new BaseContext() {
                        public Spi getSpi() {
                            return spi;
                        }
                    },
                    context
                );

        final ClassLoader classLoader = SiteClassLoaderFactory.valueOf(c).getClassLoader();
        return (Class<C>) classLoader.loadClass(classNameFQ);
    }
    
    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

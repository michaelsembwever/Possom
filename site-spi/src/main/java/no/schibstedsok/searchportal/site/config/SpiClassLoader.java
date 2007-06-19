package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.Site;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @author magnuse
 */
public final class SpiClassLoader extends ResourceClassLoader {

    /** The context this class needs. */
    public interface Context extends ResourceClassLoader.Context, SpiContext {}

    private static final String CLASS_LOADER_FOR = "Class loader for ({0}, {1} => {2})";
    private static final Logger LOG = Logger.getLogger(SpiClassLoader.class);
            
    private final String jarName;

    private final ClassLoader parentSite;
    private final ClassLoader parentSpi;

    private final Context context;
    private final Site site;
    private final Spi spi;

    /**
     * Creates a new spi class loader.
     * {@todo describle hierarchy.}
     *
     * @param context the context.
     */
    public SpiClassLoader(final Context context) {
        super(context);

        this.context = context;
        this.site = context.getSite();
        this.spi = context.getSpi();

        jarName = spi + ".jar";

        // The sidekick classloader is used to load auxiliary (called by jvm when linking) classes from the skin. Tried last.
        parentSpi = spi.getParent() != null ? parentSpiClassLoader() : new SidekickClassLoader();
        parentSite = site.getParent() != null ? parentSiteClassLoader() : this.getClass().getClassLoader();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return MessageFormat.format(CLASS_LOADER_FOR, site, spi, spi.getParent());
    }

    /**
     * Tries to load class, delegating to parent class loaders in the following order.
     *
     * @param name the name of the class to find.
     * @param resolve if to resolve it.
     * @return the class
     * @throws ClassNotFoundException if the class could not be found in any class loader.
     */
    public synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException
    {
        // First, check if the class has already been loaded
        Class c = findLoadedClass(name);

        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                try {
                    c = parentSite.loadClass(name);
                } catch (ClassNotFoundException e1) {
                    c = parentSpi.loadClass(name);
                }
            }
        }

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }

    /** {@inheritDoc} */
    protected String getJarName() {
        return jarName;
    }

    /** {@inheritDoc} */
    protected Class<?> findClass(final String className) throws ClassNotFoundException {
        final Class clazz = super.findClass(className);
        LOG.info(MessageFormat.format("Class {0} loaded by {1}", className, this));
        return clazz;
    }

    private ClassLoader parentSpiClassLoader() {
        final SiteClassLoaderFactory.Context factoryContext = new SiteClassLoaderFactory.Context() {
            public BytecodeLoader newBytecodeLoader(final SiteContext siteCxt, final String name, final String jar) {
                return context.newBytecodeLoader(siteCxt, name, jar);
            }

            public Site getSite() {
                return site;
            }

            public Spi getSpi() {
                return spi.getParent();
            }
        };


        return SiteClassLoaderFactory.valueOf(factoryContext).getClassLoader();
    }

    private ClassLoader parentSiteClassLoader() {
        final SiteClassLoaderFactory.Context parentContext = new SiteClassLoaderFactory.Context() {

            public BytecodeLoader newBytecodeLoader(final SiteContext siteCxt, final String name, final String jar) {
                return context.newBytecodeLoader(siteCxt, name, jar);
            }

            public Site getSite() {
               return site.getParent();
            }

            public Spi getSpi() {
                return spi;
            }
        };

        return SiteClassLoaderFactory.valueOf(parentContext).getClassLoader();
    }

    /**
     * This class loader will do "resource" class loading from the skin. Any class available to the class loader of
     * the resource servlet (commons-resourcefeed) will be found.
     */
    private class SidekickClassLoader extends ResourceClassLoader {
        public SidekickClassLoader() {
            super(context, SpiClassLoader.this.getClass().getClassLoader());
        }

        protected String getJarName() {
            return null;
        }
    }
}

package no.schibstedsok.searchportal.site.config;


import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;

/**
 * @author magnuse
 */
public abstract class ResourceClassLoader extends ClassLoader {

    private static final Logger LOG = Logger.getLogger(ResourceClassLoader.class);

    /**  Context needed by this class. */
    public interface Context extends BytecodeContext, SiteContext {}

    private final Context context;

    /**
     * Creates a new resource class loader for a site.
     *
     * @param context the context.
     */
    public ResourceClassLoader(final Context context) {
        this.context = context;
    }

    public ResourceClassLoader(final Context context, final ClassLoader parent) {
        super(parent);
        this.context = context;
    }

    /**
     * Returns the jar file the class must be contained in. If null, all jar-files and classes available to the class
     * loader of the resource servlet are searched.
     *
     * @return the name of the jar file.
     */
    protected abstract String getJarName();

    /**
     * Finds classes using a {@link BytecodeLoader}.
     *
     * @param className the clas to find
     * @return the class.
     * @throws ClassNotFoundException if the class cannot be found in this class loader.
     */
    protected Class<?> findClass(final String className) throws ClassNotFoundException {

        final BytecodeLoader loader = context.newBytecodeLoader(context, className, getJarName());
        loader.abut();

        final byte[] bytecode = loader.getBytecode();

        // Resource loader loaded empty result means class was not found.
        if (bytecode.length == 0) {
            throw new ClassNotFoundException(className + " not found");
        }

        return defineClass(className, bytecode, 0, bytecode.length);
    }
}

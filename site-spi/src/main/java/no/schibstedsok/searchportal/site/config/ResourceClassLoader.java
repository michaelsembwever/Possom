package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.searchportal.site.SiteContext;

/**
  * Class loader using the resource loading framework to find classes.
 *
 * @author magnuse
 */
public final class ResourceClassLoader extends ClassLoader {

    /**  Context needed by this class. */
    public interface Context extends BytecodeContext, SiteContext {}

    private final Context context;

    /**
     * Creates a new class loader.
     *
     * @param context the context.
     * @param parentClassLoader the parent class loader. This classloader is asked for classes before this classloader.
     */
    public ResourceClassLoader(final Context context, final ClassLoader parentClassLoader) {
        super(parentClassLoader);
        this.context = context;
    }

    /**
     * Finds classes using a {@link BytecodeLoader}.
     *
     * @param className the clas to find
     * @return the class.
     * @throws ClassNotFoundException if the class cannot be found in this class loader.
     */
    protected Class<?> findClass(final String className) throws ClassNotFoundException {
        final BytecodeLoader loader = context.newBytecodeLoader(context, className);
        loader.abut();

        final byte[] bytecode = loader.getBytecode();

        // Resource loader loaded empty result means class was not found.
        if (bytecode.length == 0) {
            throw new ClassNotFoundException(className + " not found");
        }

        return defineClass(className, bytecode, 0, bytecode.length);
    }
}

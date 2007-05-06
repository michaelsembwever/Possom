package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.searchportal.site.SiteContext;

/**
  * Class loader using the resource loading framework to find classes.
 *
 * @author Magnus Eklund
 */
public final class ResourceClassLoader extends ClassLoader {

    public interface Context extends BytecodeContext, SiteContext {}

    private final Context context;

    public ResourceClassLoader(final Context context, final ClassLoader parentClassLoader) {
        super(parentClassLoader);
        this.context = context;
    }
    
    protected final Class<?> findClass(final String className) throws ClassNotFoundException {
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

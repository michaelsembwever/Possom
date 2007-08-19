/*
 * SearchCommandExecutorFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.sesat.search.mode.executor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.mode.SearchMode.SearchCommandExecutorConfig.Controller;

/** Obtain a working ResultHandler from a given ResultHandlerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchCommandExecutorFactory {

    // Constants -----------------------------------------------------


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of QueryTransformerFactory */
    private SearchCommandExecutorFactory() {
    }

    // Public --------------------------------------------------------

    /**
     *
     * @param config
     * @return
     */
    public static SearchCommandExecutor getController(final SearchMode.SearchCommandExecutorConfig config){

        try{

            final String name = "no.sesat.search.mode.executor."
                    + SearchMode.SearchCommandExecutorConfig.class.getDeclaredField(config.name())
                    .getAnnotation(Controller.class).value();

            final Class<? extends SearchCommandExecutor> cls
                    = (Class<? extends SearchCommandExecutor>)config.getClass().getClassLoader().loadClass(name);

            final Constructor<? extends SearchCommandExecutor> constructor = cls.getConstructor();

            return constructor.newInstance();

        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        } catch (NoSuchFieldException ex) {
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

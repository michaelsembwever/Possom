/*
 * ResultHandlerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.schibstedsok.searchportal.result.handler;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;

/** Obtain a working ResultHandler from a given ResultHandlerConfig.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class ResultHandlerFactory {
    
    // Constants -----------------------------------------------------
    
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of QueryTransformerFactory */
    private ResultHandlerFactory() {
    }
    
    // Public --------------------------------------------------------
    
    // Public --------------------------------------------------------
    
    // Public --------------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * 
     * @param config 
     * @return 
     */
    public static ResultHandler getController(final ResultHandlerConfig config){
        
        final String name = "no.schibstedsok.searchportal.result.handler." 
                + config.getClass().getAnnotation(Controller.class).value();
        
        try{
            
            final Class<? extends ResultHandler> cls 
                    = (Class<? extends ResultHandler>)config.getClass().getClassLoader().loadClass(name);
            
            final Constructor<? extends ResultHandler> constructor 
                    = cls.getConstructor(ResultHandlerConfig.class);

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

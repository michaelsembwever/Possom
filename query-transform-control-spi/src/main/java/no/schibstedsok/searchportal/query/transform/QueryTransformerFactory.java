/*
 * QueryTransformerFactory.java
 *
 * Created on 26 March 2007, 17:29
 *
 */

package no.schibstedsok.searchportal.query.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class QueryTransformerFactory {
    
    // Constants -----------------------------------------------------
    
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of QueryTransformerFactory */
    private QueryTransformerFactory() {
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
    public static QueryTransformer getController(final QueryTransformerConfig config){
        
        final String name = "no.schibstedsok.searchportal.query.transform." 
                + config.getClass().getAnnotation(Controller.class).value();
        
        try{
            
            final Class<? extends QueryTransformer> cls 
                    = (Class<? extends QueryTransformer>)config.getClass().getClassLoader().loadClass(name);
            
            final Constructor<? extends QueryTransformer> constructor 
                    = cls.getConstructor(QueryTransformerConfig.class);

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

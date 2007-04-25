// Copyright (2006-2007) Schibsted Søk AS
/*
 * SearchCommandFactory.java
 *
 * Created on January 5, 2006, 10:17 AM
 *
 */

package no.schibstedsok.searchportal.mode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;


/** This factory creates the appropriate command for a given SearchConfiguration.
 *
 * @author mick
 * @version $Id: SearchCommandFactory.java 3359 2006-08-03 08:13:22Z mickw $
 */
public final class SearchCommandFactory {

    private SearchCommandFactory() {  }

    
    /** Create the appropriate command given the configuration inside the context.
     * 
     * @param cxt 
     * @return 
     */
    public static SearchCommand getController(final SearchCommand.Context cxt){
        
        final SearchConfiguration config = cxt.getSearchConfiguration();
        
        final String name = "no.schibstedsok.searchportal.mode.command." 
                + config.getClass().getAnnotation(Controller.class).value();
        
        try{
            
            final Class<? extends SearchCommand> cls 
                    = (Class<? extends SearchCommand>)config.getClass().getClassLoader().loadClass(name);
            
            final Constructor<? extends SearchCommand> constructor 
                    = cls.getConstructor(SearchCommand.Context.class);

            return constructor.newInstance(cxt);
            
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
}

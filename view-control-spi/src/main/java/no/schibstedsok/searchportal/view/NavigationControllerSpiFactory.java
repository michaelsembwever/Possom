/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
package no.schibstedsok.searchportal.view;

import no.schibstedsok.searchportal.view.navigation.NavigationConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.view.navigation.NavigationControllerFactory;
import no.schibstedsok.searchportal.site.config.*;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.Site;


/**
 * 
 * @author magnuse
 * @version $Id$
 */
public final class NavigationControllerSpiFactory {

    public interface Context extends SiteContext, BytecodeContext {}

    private final Context context;

    /**
     *
     * @param context
     */
    public NavigationControllerSpiFactory(final Context context) {
        this.context = context;
    }

    public <T extends NavigationConfig.Nav> NavigationControllerFactory<T> getController(final T navConf){

        final String controllerName = navConf.getClass().getAnnotation(NavigationConfig.Nav.ControllerFactory.class).value();

        try{

            final SiteClassLoaderFactory.Context classContext = new SiteClassLoaderFactory.Context() {
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
                public Site getSite() {
                    return context.getSite();
                }
                public Spi getSpi() {
                    return Spi.VIEW_CONTROL;
                }
            };

            final SiteClassLoaderFactory loaderFactory = SiteClassLoaderFactory.valueOf(classContext);

            @SuppressWarnings("unchecked")
            final Class<? extends NavigationControllerFactory<T>> factory 
                    = (Class<? extends NavigationControllerFactory<T>>) 
                    loaderFactory.getClassLoader().loadClass(controllerName);

            final Constructor<? extends NavigationControllerFactory<T>> constructor
                    = (Constructor<? extends NavigationControllerFactory<T>>) factory.getConstructor();

            return constructor.newInstance();

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
/*
 * NavigatorHelper.java
 * 
 * Created on 30-Apr-2007, 16:15:40
 */

package no.schibstedsok.searchportal.util;

import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObjectSupport;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand;
import no.schibstedsok.searchportal.mode.config.FastCommandConfig;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Navigator;
import org.apache.log4j.Logger;

/**
 * @author andersjj
 */
public class NavigatorHelper {

    public class NavigatorWrapper {
        private final Navigator navigator;
        private final String url;
        private final String backUrl;
        private final StringDataObject fieldValue;
        
        public NavigatorWrapper(final Navigator navigator, final String url, final String backUrl, final StringDataObject fieldValue) {
            this.navigator = navigator;
            this.url = url;
            this.backUrl = backUrl;
            this.fieldValue = fieldValue;
        }
        
        public final String getUrl() {
            return url;
        }
             
        public final String getBackUrl() {
            return backUrl;
        }
        
        public final String getId() {
            return navigator.getId();
        }
        
        public final String getName() {
            return navigator.getName();
        }
        
        public final String getField() {
            return navigator.getField();
        }
        
        public final String getDisplayName() {
            return navigator.getDisplayName();
        }
        
        public final StringDataObject getFieldValue() {
            return fieldValue;
        }
        
        public final Navigator getChildNavigator() {
            return navigator.getChildNavigator();
        }
       
        @Override
        public final String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("[").append(getId()).append(", ").append(getName()).append(", ").append(getFieldValue().getString()).append("]");
            return sb.toString();
        }
    }

    private static final Logger LOG = Logger.getLogger(NavigatorHelper.class);
    private static final NavigatorHelper INSTANCE = new NavigatorHelper();
    
    private NavigatorHelper() {
    }

    public static final NavigatorHelper getInstance() {
        return INSTANCE;
    }
    
    public final List<Navigator> getNavigatorList(final DataModel datamodel, final AbstractSimpleFastSearchCommand asfsc, final String navigatorKey) {
        if (datamodel == null) {
            throw new NullPointerException("datamodel");
        } 
        if (asfsc == null) {
            throw new NullPointerException("asfsc");
        }
        if (navigatorKey == null) {
            throw new NullPointerException("navigator");
        }
        
        final FastCommandConfig fcc = asfsc.getSearchConfiguration();

        final List<Navigator> navigatorList = new ArrayList<Navigator>();
        Navigator navigator = fcc.getNavigator(navigatorKey);
        navigatorList.add(navigator);
        
        final ParametersDataObject parameters = datamodel.getParameters();
        if (parameters.getValue("nav_" + navigatorKey) == null) {
            return navigatorList;
        }
        
        final String navigatorIndex = parameters.getValue("nav_" + navigatorKey).getString();
        Navigator prevNavigator = navigator;
        while ((navigator = navigator.getChildNavigator()) != null) {
            navigatorList.add(navigator);
            if (prevNavigator != null && prevNavigator.getName().equals(navigatorIndex)) {
                break;
            }
            prevNavigator = navigator;
        }
        return navigatorList;
    }
    
    public final List<NavigatorWrapper> getNavigators(final DataModel datamodel, final FastSearchResult fsr, final String navigatorKey) {
        final AbstractSimpleFastSearchCommand asfsc = (AbstractSimpleFastSearchCommand) fsr.getSearchCommand();
        final List<Navigator> navigatorList = getNavigatorList(datamodel, asfsc, navigatorKey);
        final ParametersDataObject parameters = datamodel.getParameters();
        final List<NavigatorWrapper> backLinks = new ArrayList<NavigatorWrapper>();
        
        StringBuilder sb = new StringBuilder();
                
        for (int i = 0; i < navigatorList.size(); i++) {
            final Navigator navigator = navigatorList.get(i);
            final StringBuilder nsb = new StringBuilder(); 
            final StringBuilder bsb = new StringBuilder();
            
            nsb.append("nav_").append(navigatorKey).append("=").append(navigator.getName());
            
            if (i > 0) {
                final Navigator prevNavigator = navigatorList.get(i - 1);
                bsb.append("nav_").append(navigatorKey).append("=").append(prevNavigator.getName());
            }
            
            final String field = navigator.getField();
            final StringDataObject fieldValue = parameters.getValue(field);
          
            bsb.append(sb);
            if (fieldValue != null) {
                final String value = fieldValue.getUtf8UrlEncoded();
                sb.append("&#38;").append(field).append("=").append(value);
                
            }
            nsb.append(sb);
                    
            final NavigatorWrapper nw = new NavigatorWrapper(navigator, nsb.toString(), bsb.toString(),
                    parameters.getValue(field) != null ? parameters.getValue(field) : new StringDataObjectSupport(navigator.getDisplayName()));
            
            LOG.debug(nw.toString());
            
            backLinks.add(nw);
        }
        
        LOG.debug("Navigator back links for '" + navigatorKey + "':\n" + backLinks);
        return backLinks;
    }
}

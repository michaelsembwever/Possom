/*
 * NavigationItem.java
 * 
 * Created on 11/06/2007, 10:42:11
 * 
 */

package no.schibstedsok.searchportal.result;

/**
 *
 * @author mick
 * @version $Id$
 */
public interface NavigationItem extends ResultList<NavigationItem>{
    
    boolean isSelected();
    void setSelected(boolean selected);
    
    boolean isChildSelected();
    NavigationItem getSelectedChild();
    NavigationItem getChildByTitle(String title);
}

/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * NavigationItem.java
 * 
 * Created on 11/06/2007, 10:42:11
 * 
 */

package no.sesat.search.result;

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

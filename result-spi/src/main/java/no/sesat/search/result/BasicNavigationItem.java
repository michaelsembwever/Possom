/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * BasicNavigationItem.java
 * 
 * Created on 11/06/2007, 11:25:25
 * 
 */

package no.sesat.search.result;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mick</a>
 * @version $Id$
 */
public class BasicNavigationItem extends BasicResultList<NavigationItem> implements NavigationItem{

    private boolean selected = false;
    private int depth = 0;
    
    public BasicNavigationItem(){}

    /**
     * @param title
     * @param url
     * @param hitCount
     */
    public BasicNavigationItem(final String title, final String url, final int hitCount) {
        super(title, url, hitCount);
    }

    /**
     * @param selected
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    /**
     * @return
     */
    public boolean isSelected() {
        return selected;
    }
    
    public NavigationItem getSelectedChild() {
        
        return getChildSelectedImpl();
    }

    public boolean isChildSelected() {
        
        return null != getChildSelectedImpl();
    }

    public NavigationItem getChildByTitle(String title) {
        for (NavigationItem navigationItem : getResults()) {
            if (navigationItem.getTitle().equals(title)) {
                return navigationItem;
            }
        }
        return null;
    }

    /**
     * Return a list of NavigationItem's that makes up the selected
     * path of this NavigationItem. This path will go from this element
     * all the way to the last selected child.
     *
     * @return List of selected elements.
     */
    public List<NavigationItem> getSelectedBranch() {
        Vector<NavigationItem> res = new Vector<NavigationItem>();
        NavigationItem current = this;
        while(current.isChildSelected()) {
            current=current.getSelectedChild();
            res.add(current);
        }
        return res;
    }

    /**
     * Create a list off all children collected recursivly (Depth first). 
     *
     * @return
     */
    public List<NavigationItem> getChildrenRecursive() {
        Vector<NavigationItem> res = new Vector<NavigationItem>();
        getChildrenRecursiveHelper(res, this);
        return res;
    }

    private void getChildrenRecursiveHelper(List<NavigationItem> res, NavigationItem nav) {
        for(NavigationItem child : nav.getResults()) {
            res.add(child);
            getChildrenRecursiveHelper(res, child);
        }
    }

    public int getDepth() {
        return depth;
    }

    /**
     *  The depth of this element. (Used when elements are organized in tree like struktures
     *
     * @param d Depth of element.
     *
     * @return this
     */
    public void setDepth(int d) {
        depth = d;
    }

    private NavigationItem getChildSelectedImpl() {
        
        // XXX Geir's original work had a dirty flag here to cache this result.
        //  I doubt that this is an application hotspot, 
        //  and the flag wasn't bomb-proof, 
        // so it's all been removed for now.
        
        NavigationItem childSelected = null;
        for (NavigationItem item : getResults()) {
            if (item.isSelected()) {
                childSelected = item;
                break;
            }
        }
        return childSelected;
    }
}

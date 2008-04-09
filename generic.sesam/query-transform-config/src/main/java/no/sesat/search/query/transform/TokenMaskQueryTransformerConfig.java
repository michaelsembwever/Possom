/* Copyright (2006-2008) Schibsted Søk AS
 * This file is part of SESAT.
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

 */
package no.sesat.search.query.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Mask (inclusively or exclusively) terms in the query that
 * positionally (prefix or anywhere) contains TokenPredicates.
 *
 * <b>Note</b> Using <code>position="prefix" predicates="*_MAGIC"</code> is kinda pointless but is often done anyway.
 * <b>Note</b> position="prefix" only currently works with single terms. XXX
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@sesam.no">Mick Wever</a>
 * @version <tt>$Id$</tt>
 */
@Controller("TokenMaskQueryTransformer")
public final class TokenMaskQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private static final Logger LOG = Logger.getLogger(TokenMaskQueryTransformerConfig.class);

    /** Position restrictions when searching for matching predicates. **/
    public enum Position {
        /** TODO comment me. **/
        PREFIX,
        /** TODO comment me. **/
        ANY
    };

    /** Types of masking to perform during transformation. **/
    public enum Mask {
        /** TODO comment me. **/
        INCLUDE,
        /** TODO comment me. **/
        EXCLUDE
    };

    // do not remove token predicates by default any more

    // do not remove token predicates by default any more
    private static final Collection<TokenPredicate> DEFAULT_PREDICATES = Collections.EMPTY_SET;

    private Collection<String> prefixes = new ArrayList<String>();
    private Collection<TokenPredicate> customPredicates;
    private Position position = Position.ANY;
    private Mask mask = Mask.EXCLUDE;

    private static final String ERR_PREFIX_NOT_FOUND = "No such TokenPredicate ";

    /**
     *
     * @return
     */
    public Collection<TokenPredicate> getPredicates() {

        synchronized (this) {
            if (customPredicates == null && prefixes != null && prefixes.size() > 0) {
                final Collection<TokenPredicate> cp = new ArrayList(DEFAULT_PREDICATES);
                for (String tp : prefixes) {
                    try{
                        cp.add(TokenPredicate.Static.getTokenPredicate(tp));
                    }catch(IllegalArgumentException iae){
                        LOG.error(ERR_PREFIX_NOT_FOUND + tp, iae);
                    }
                }
                customPredicates = Collections.unmodifiableCollection(cp);
            }
        }
        return prefixes != null && prefixes.size() > 0
                ? customPredicates
                : DEFAULT_PREDICATES;
    }

    @Override
    public TokenMaskQueryTransformerConfig readQueryTransformer(final Element qt){

        super.readQueryTransformer(qt);
        addPredicates(qt.getAttribute("predicates").split(","));
        if(qt.getAttribute("position").length() > 0){
            setPosition(Position.valueOf(qt.getAttribute("position").toUpperCase()));
        }
        if(qt.getAttribute("mask").length() >0){
            setMask(Mask.valueOf(qt.getAttribute("mask").toUpperCase()));
        }
        return this;
    }

    /** TODO comment me. **/
    public void addPredicates(final String[] pArr) {

        if(pArr.length > 0 && pArr[0].trim().length() >0){
            prefixes.addAll(Arrays.asList(pArr));
        }
    }

    /**
     * Getter for property position.
     *
     * @return Value of property position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Setter for property position.
     *
     * @param position New value of property position.
     */
    public void setPosition(final Position position) {
        this.position = position;
    }

    /**
     * Getter for property mask.
     * @return Value of property mask.
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Setter for property mask.
     * @param mask New value of property mask.
     */
    public void setMask(final Mask mask) {
        this.mask = mask;
    }


}

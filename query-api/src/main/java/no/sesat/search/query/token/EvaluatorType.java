/* Copyright (2008) Schibsted Søk AS
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
package no.sesat.search.query.token;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.commons.ioc.BaseContext;

/**
 * The types of TokenPredicates that exist.
 * @version $Id$
 */
public final class EvaluatorType implements BaseContext, Serializable {

    private static final Map<String,EvaluatorType> INSTANCES = new HashMap<String,EvaluatorType>();

    public static final EvaluatorType FAST
            = new EvaluatorType("FAST", "no.sesat.search.query.token.FastQueryMatchingEvaluatorFactory");

    public static final EvaluatorType REGEX
            = new EvaluatorType("REGEX", "no.sesat.search.query.token.RegExpEvaluatorFactory");

    public static final EvaluatorType JEP
            = new EvaluatorType("JEP", "no.sesat.search.query.token.JepEvaluatorFactory");

    private final String name;

    private final String clsName;

    public EvaluatorType(final String name, final String factoryClsName) {
        super();

        this.name = name;
        this.clsName = factoryClsName;
        synchronized (INSTANCES) {
            INSTANCES.put(name, this);
        }
    }

    public String getEvaluatorFactoryClassName() {
        return clsName;
    }

    public static Set<EvaluatorType> getInstances() {
        synchronized (INSTANCES) {
            return Collections.unmodifiableSet(new HashSet<EvaluatorType>(INSTANCES.values()));
        }
    }

    public static EvaluatorType instanceOf(final String name){
        synchronized (INSTANCES) {
            return INSTANCES.get(name);
        }
    }
}

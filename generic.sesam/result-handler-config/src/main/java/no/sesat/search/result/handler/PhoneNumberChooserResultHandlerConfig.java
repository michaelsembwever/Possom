/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.result.handler;

import no.sesat.search.result.handler.AbstractResultHandlerConfig.Controller;


/**
 * @deprecated Sesam.no katalog specific. Will be removed soon.
 * Use FieldChooser instead with from="yphovedtelefon,ypandretelefoner,ypmobiltelefon" target="ypanynumber".
 *
 * @version <tt>$Revision: 4510 $</tt>
 */
@Controller("PhoneNumberChooser")
public final class PhoneNumberChooserResultHandlerConfig extends AbstractResultHandlerConfig {}
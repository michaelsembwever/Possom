/* Copyright (2007) Schibsted Søk AS
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
package no.sesat.search.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;



/**
 * <b> Must be threadsafe </b>
 *
 * @version <tt>$Id$</tt>
 */
public final class TradeDoubler {

    private final static String secretCode = "997";
    private final static String organization = "1064392";
    private final static String event = "46757";
    private final HttpServletRequest request;

    public TradeDoubler(HttpServletRequest request) {
        this.request = request;
    }

    public String getChecksum(
            final String orderNumber,
            final String orderValue) throws RuntimeException {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
        final String s = new String(TradeDoubler.getSecretCode() + orderNumber + orderValue);
        digest.update(s.getBytes());
        return new String(Hex.encodeHex(digest.digest()));
    }

    public String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getEvent() {
        return event;
    }

    public static String getOrganization() {
        return organization;
    }

    public static String getSecretCode() {
        return secretCode;
    }

    public String getCookieTDUID() {
        return getCookie("TRADEDOUBLER");
    }

    public String getCookieOrderNumber(){
        return getCookie("TRADEDOUBLER-onr");
    }

    public String getCookieChecksum(){
        return getCookie("TRADEDOUBLER-cs");
    }

    public String getCookieReportInfo(){
        return getCookie("TRADEDOUBLER-ri");
    }

    private String getCookie(String name){
        if (this.request == null){
            return "";
        }
        String value = "";
        final Cookie[] cookies = this.request.getCookies();
        if (cookies != null){
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(name)) {
                    if (cookies[i].getValue() != null) {
                        value = cookies[i].getValue();
                    }
                }
            }
        }
        return value;
    }
}

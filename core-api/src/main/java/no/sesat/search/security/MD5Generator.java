/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.security;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class MD5Generator {
    
    private static final byte[] EMPTY_STRING = new byte[0];
    
    private final String secret;

    public MD5Generator(final String secret) {
        
        this.secret = secret;
    }

    public String generateMD5(final String s) {
        
        final MessageDigest digest  = getDigest("MD5");

        digest.update(null != s ? s.getBytes() : EMPTY_STRING);
        digest.update(secret.getBytes());

        return String.valueOf(Hex.encodeHex(digest.digest()));
    }

    public boolean validate(final String s, final String hash) {
        
        return generateMD5(s).equals(hash);

    }

    /**
     * Returns a MessageDigest for the given <code>algorithm</code>.
     *
     * @param algorithm The MessageDigest algorithm name.
     * @return An digest instance.
     * @throws RuntimeException when a {@link java.security.NoSuchAlgorithmException} is caught,
     */
    static MessageDigest getDigest(String algorithm) {
        
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

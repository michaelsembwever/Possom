package no.schibstedsok.front.searchportal.security;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MD5Generator {
    private final String secret;

    public MD5Generator(String secret) {
        this.secret = secret;
    }

    public String generateMD5(String s) {
        final MessageDigest digest  = getDigest("MD5");

        digest.update(s.getBytes());
        digest.update(secret.getBytes());

        return new String(Hex.encodeHex(digest.digest()));
    }

    public boolean validate(String s, String hash) {
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

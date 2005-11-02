package no.schibstedsok.front.searchportal.security;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MD5Generator {
    private String secret;

    public MD5Generator(String secret) {
        this.secret = secret;
    }

    public String generateMD5(String s) {
        MessageDigest digest  = getDigest("MD5");

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

    public static void main(String[] args) {
        MD5Generator g = new MD5Generator("secret");

        System.out.println(g.generateMD5("123"));
        System.out.println(g.generateMD5("1234"));
        System.out.println(g.validate("123", g.generateMD5("1233")));

    }

}

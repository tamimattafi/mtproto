package com.attafitamim.mtproto.client.sockets.secure;

import com.attafitamim.mtproto.security.digest.core.DigestMode;
import com.attafitamim.mtproto.security.digest.core.IDigest;
import com.attafitamim.mtproto.security.digest.jvm.Digest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CryptoUtils {

    public static byte[] SHA1(InputStream in) throws IOException {
        IDigest crypt = Digest.Companion.createDigest(DigestMode.SHA1);;
        crypt.reset();
        // Transfer bytes from in to out
        byte[] buf = new byte[4 * 1024];
        while (in.read(buf) > 0) {
            Thread.yield();
            crypt.update(buf);
        }
        in.close();
        return crypt.digest(null);
    }

    public static byte[] SHA1(String fileName) throws IOException {
        IDigest crypt = Digest.Companion.createDigest(DigestMode.SHA1);;
        crypt.reset();
        FileInputStream in = new FileInputStream(fileName);
        // Transfer bytes from in to out
        byte[] buf = new byte[4 * 1024];
        while (in.read(buf) > 0) {
            Thread.yield();
            // out.write(buf, 0, len);
            crypt.update(buf);
        }
        in.close();
        return crypt.digest(null);
    }

    public static byte[] SHA1(byte[] src) {
        IDigest crypt = Digest.Companion.createDigest(DigestMode.SHA1);;
        crypt.reset();
        return crypt.digest(src);
    }

    public static byte[] SHA1(byte[]... src1) {
        IDigest crypt = Digest.Companion.createDigest(DigestMode.SHA1);;
        crypt.reset();
        for (byte[] bytes : src1) {
            crypt.update(bytes);
        }
        return crypt.digest(null);
    }

    public static byte[] SHA256(byte[]... src1) {
        IDigest crypt = Digest.Companion.createDigest(DigestMode.SHA256);;

        for (byte[] bytes : src1) {
            crypt.update(bytes);
        }

        return crypt.digest(null);
    }

    public static byte[] concat(byte[]... v) {
        int len = 0;
        for (int i = 0; i < v.length; i++) {
            len += v[i].length;
        }
        byte[] res = new byte[len];
        int offset = 0;
        for (int i = 0; i < v.length; i++) {
            System.arraycopy(v[i], 0, res, offset, v[i].length);
            offset += v[i].length;
        }
        return res;
    }

    public static byte[] substring(byte[] src, int start, int len) {
        byte[] res = new byte[len];
        System.arraycopy(src, start, res, 0, len);
        return res;
    }

    /**
     * Adds padding
     */
    public static byte[] align(byte[] src, int factor) {
        if (src.length % factor == 0) {
            return src;
        }
        int padding = factor - src.length % factor;

        return concat(src, RandomUtils.randomByteArray(padding));
    }

    public static byte[] alignKeyZero(byte[] src, int size) {
        if (src.length == size) {
            return src;
        }

        if (src.length > size) {
            return substring(src, src.length - size, size);
        } else {
            return concat(new byte[size - src.length], src);
        }
    }

    public static byte[] xor(byte[] a, byte[] b) {
        byte[] res = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = (byte) (a[i] ^ b[i]);
        }
        return res;
    }

    public static BigInteger loadBigInt(byte[] data) {
        return new BigInteger(1, data);
    }
}
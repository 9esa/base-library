package org.zuzuk.utils;

import com.google.api.client.util.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Some common utils
 */
public class Utils {

    /* Returns url parameter from Value attribute of Enum. Used for requesting */
    public static String getUrlParameter(Enum obj) {
        try {
            Value parameterName = obj.getClass().getField(obj.name()).getAnnotation(Value.class);
            return parameterName.value();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    /* Returns MD5 hash of string */
    public static String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /* Null-safe equivalent of a.equals(b) */
    public static boolean objectsEquals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}

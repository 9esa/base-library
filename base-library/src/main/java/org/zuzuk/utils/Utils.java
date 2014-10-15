package org.zuzuk.utils;

import android.support.annotation.NonNull;

import com.google.api.client.util.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Some common utils
 */
@SuppressWarnings("unchecked")
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

    /**
     * Equals thisObject to thatObject by IEqualsTypedChecker if thisObject is not null.
     * Use it inside equals(Object obj) method
     */
    public static <T extends EqualsTypedChecker> boolean objectEqualTo(@NonNull T thisObject, Object thatObject) {
        return thatObject == thisObject
                ||
                thatObject != null
                        && thatObject.getClass().equals(thisObject.getClass())
                        && thisObject.areEqual(thisObject, thatObject);
    }

    /* Returns hash code from multiple objects */
    public static int hashCode(Object... values) {
        return Arrays.hashCode(values);
    }
}

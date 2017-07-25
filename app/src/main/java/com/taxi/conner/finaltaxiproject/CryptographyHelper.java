package com.taxi.conner.finaltaxiproject;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Conner on 21/04/2017.
 */

public class CryptographyHelper {



    /**
     * Creates a random salt string for new account
     * @return - String being random salt value
     */
    public String createSalt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        StringBuilder builder = new StringBuilder();
        for(byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        String result = builder.toString();
        return result;
    }

    /**
     *
     * @param password - String being desired password
     * @param salt - String being users generated salt value
     * @return - String being users hashed password
     */
    public String hashPasswordWithSalt(String password, String salt) {
        String hashedPassword = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(salt.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest(password.getBytes("UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i < bytes.length; i++) {
                stringBuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hashedPassword = stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }
}
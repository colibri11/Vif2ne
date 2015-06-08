/*
 * Copyright (C) 2015 by Sergey Omarov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by serg 21.05.15 20:19
 */

package ru.vif2ne.backend;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class LocalUtils {
    public static String formatDateTime(Context ctx, Date date) {
        if (date == null) return "***";
        return DateUtils.formatDateTime(ctx, date.getTime(),
                DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_TIME |
                        DateUtils.FORMAT_SHOW_YEAR);
    }

    public static Date stringToDate(String isoDateString, String format) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat(format);
        return f.parse(isoDateString);
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(clear);
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }

    private static byte[] getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "passsword";
        int iterationCount = 1000;
        int saltLength = 32; // bytes; should be the same size as the output (256 / 8 = 32)
        int keyLength = 256; // 256-bits for AES-256, 128-bits for AES-128, etc
        byte[] salt; // Should be of saltLength

    /* When first creating the key, obtain a salt with this: */
        SecureRandom random = new SecureRandom();
        salt = new byte[saltLength];
        random.nextBytes(salt);

        salt = "876yugytfvvb6ingi667rv7r76r5".getBytes();

    /* Use this to derive the key from the password: */
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                iterationCount, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        return keyFactory.generateSecret(keySpec).getEncoded();
    }

    public static String enc(String value) {
        try {
            byte[] b = value.getBytes("UTF-8");
            byte[] encryptedData = encrypt(getKey(), b);
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String dec(String value) {
        try {
            byte[] encryptedData = Base64.decode(value, Base64.DEFAULT);
            byte[] decryptedData = decrypt(getKey(), encryptedData);
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(
                    new String(value
                            .replace("\"", "&quot;")
                            .replace("\n", "\r\n")
                            .getBytes(), "UTF-8"), "windows-1251").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}



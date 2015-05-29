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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static byte[] getKey() {
        byte[] keyStart = "sdfvsbdfvW$%WVWER%CCW$#%GVWV$W#$%B%^B".getBytes();
        SecureRandom sr;
        KeyGenerator kgen = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
            sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(keyStart);
            kgen.init(128, sr); // 192 and 256 bits may not be available
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
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
            byte[] decryptedData = new byte[0];
            decryptedData = decrypt(getKey(), encryptedData);
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }


}



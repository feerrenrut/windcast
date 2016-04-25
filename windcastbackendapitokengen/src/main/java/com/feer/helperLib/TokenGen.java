package com.feer.helperLib;

import com.feer.helperLib.scrts.impl.scrtsImpl;
import com.feer.helperLib.scrts.scrts;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class TokenGen {
    public static final SimpleDateFormat DATE_FORMAT;
    private static scrts SCRT;
    private static MessageDigest MD;
    static {
        SCRT = new scrtsImpl();
        DATE_FORMAT  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.UK);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            MD = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create TokenGen", e);
        }
    }

    public static void initialise(scrts scrt){
        SCRT = scrt;
    }

    public static String GetToken(final String methodName, Date time, UUID user) {
        String msg = methodName + DATE_FORMAT.format(time) + user.toString();
        byte[] bytes;
        try {
            bytes = MD.digest((msg + SCRT.getScrt()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to encode digest string.", e);
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    static public boolean TestToken(String methodName, String userIdStr, long dateTime, String tokenToTestString) {
        Date creationTime = new Date(dateTime);

        userIdStr = new String(Base64.decode(userIdStr.getBytes(), Base64.NO_WRAP) );
        String regenTokenString = TokenGen.GetToken(
                methodName,
                creationTime,
                UUID.fromString(userIdStr));

        return regenTokenString.equals(tokenToTestString);
    }
}

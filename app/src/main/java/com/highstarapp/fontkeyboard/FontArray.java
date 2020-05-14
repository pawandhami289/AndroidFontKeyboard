package com.highstarapp.fontkeyboard;


import android.icu.lang.UCharacter;

import java.io.UnsupportedEncodingException;

public class FontArray {



    public static  String newString(final int codePoint) {
        if (UCharacter.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(UCharacter.toChars(codePoint));
        }
    }





    public static StringBuilder newStrings(final int[] codePoints) {
        String[] result = new String[codePoints.length];
        StringBuilder sb = new StringBuilder();
        char[] codeUnits = new char[3];
        for (int i = 0; i < codePoints.length; i++) {
            int count = UCharacter.toChars(codePoints[i], codeUnits, 0);
            result[i] = new String(codeUnits, 0, count);
            sb.append(result[i]);
        }
        return sb;
    }


}

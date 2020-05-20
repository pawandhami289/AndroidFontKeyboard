package com.highstarapp.fontkeyboard;

import android.content.res.Configuration;
import android.icu.lang.UCharacter;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import java.io.UnsupportedEncodingException;

import static android.content.Context.WINDOW_SERVICE;
import static com.highstarapp.fontkeyboard.AppKeyboard.TAG;

public class UnicodeUtils {

    /*
    called for if unicode character having 1 code point and having length >= 6 for ex: 128514
     */
    public static  String bindSingleCodePoint(final int codePoint) {
        if (UCharacter.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(UCharacter.toChars(codePoint));
        }
    }


    /*
    called for if unicode character having more than 1 code points for ex: 128115 8205 9792 65039
    */
    public static StringBuilder bindMultipleCodePoints(final int[] codePoints) {
        String[] result = new String[codePoints.length];
        StringBuilder sb = new StringBuilder();
        char[] codeUnits = new char[codePoints.length];
        for (int i = 0; i < codePoints.length; i++) {
            int count = UCharacter.toChars(codePoints[i], codeUnits, 0);
            result[i] = new String(codeUnits, 0, count);
            sb.append(result[i].trim());
        }
        return sb;
    }



}

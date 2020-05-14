@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.highstarapp.fontkeyboard

import android.content.Context
import android.util.Log

inline fun Context.saveKeyboard(xml: String, code:String,selectedFontPosition:String) {
    val xmlFile = getSharedPreferences(Constant.SELECTED_KEYBOARD_XML, Context.MODE_PRIVATE)
    val caps = getSharedPreferences(Constant.IS_CAPS, Context.MODE_PRIVATE)
    val selectedPosition = getSharedPreferences(Constant.SELECTED_FONT_POSITION, Context.MODE_PRIVATE)
    xmlFile.edit().putString(Constant.SELECTED_KEYBOARD_XML, xml).apply()
    caps.edit().putString(Constant.IS_CAPS, code).apply()
    selectedPosition.edit().putString(Constant.SELECTED_FONT_POSITION, selectedFontPosition).apply()
    Log.d("xmlFile: ", xmlFile.getString(Constant.SELECTED_KEYBOARD_XML, ""))
    Log.d("caps: ", caps.getString(Constant.IS_CAPS, ""))
    Log.d("selectedPosition: ", selectedPosition.getString(Constant.SELECTED_FONT_POSITION, ""))
}

inline fun Context.getKeyboard(): String? {
    val xmlFile = getSharedPreferences(Constant.SELECTED_KEYBOARD_XML, Context.MODE_PRIVATE)
    return xmlFile.getString(Constant.SELECTED_KEYBOARD_XML, "")
}
inline fun Context.getCode(): String? {
    val caps = getSharedPreferences(Constant.IS_CAPS, Context.MODE_PRIVATE)
    return caps.getString(Constant.IS_CAPS, "")
}
inline fun Context.getSelectedFontPosition(): String? {
    val selectedPosition = getSharedPreferences(Constant.SELECTED_FONT_POSITION, Context.MODE_PRIVATE)
    return selectedPosition.getString(Constant.SELECTED_FONT_POSITION, "")
}
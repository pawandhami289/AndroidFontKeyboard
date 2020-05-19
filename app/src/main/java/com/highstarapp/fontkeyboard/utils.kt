@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.highstarapp.fontkeyboard

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.lang.Exception


/*saved to local storage in SharedPreferences,
this file data will not be loosed until app uninstall and clear app cache
 */
fun Context.saveKeyboard(xml: String, code:String,selectedFontPosition:String) {
    val xmlFile = getSharedPreferences(Constant.SELECTED_KEYBOARD_XML, Context.MODE_PRIVATE)
    val caps = getSharedPreferences(Constant.IS_CAPS, Context.MODE_PRIVATE)
    val selectedPosition = getSharedPreferences(Constant.SELECTED_FONT_POSITION, Context.MODE_PRIVATE)
    xmlFile.edit().putString(Constant.SELECTED_KEYBOARD_XML, xml).apply()
    caps.edit().putString(Constant.IS_CAPS, code).apply()
    selectedPosition.edit().putString(Constant.SELECTED_FONT_POSITION, selectedFontPosition).apply()
   /* Log.d("xmlFile: ", xmlFile.getString(Constant.SELECTED_KEYBOARD_XML, ""))
    Log.d("caps: ", caps.getString(Constant.IS_CAPS, ""))
    Log.d("selectedPosition: ", selectedPosition.getString(Constant.SELECTED_FONT_POSITION, ""))*/
}

fun Context.getKeyboard(): String? {
    val xmlFile = getSharedPreferences(Constant.SELECTED_KEYBOARD_XML, Context.MODE_PRIVATE)
    return xmlFile.getString(Constant.SELECTED_KEYBOARD_XML, "")
}
fun Context.getCode(): String? {
    val caps = getSharedPreferences(Constant.IS_CAPS, Context.MODE_PRIVATE)
    return caps.getString(Constant.IS_CAPS, "")
}
fun Context.getSelectedFontPosition(): String? {
    val selectedPosition = getSharedPreferences(Constant.SELECTED_FONT_POSITION, Context.MODE_PRIVATE)
    return selectedPosition.getString(Constant.SELECTED_FONT_POSITION, "")
}

fun Context.adjustFontScale(configuration: Configuration){
    try{
    if (configuration.fontScale>1.30) {
        configuration.fontScale = 1.30f
        val metrics: DisplayMetrics = resources.displayMetrics
        val wm: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        resources.updateConfiguration(configuration, metrics)
    }
    }catch (e:Exception){
        e.printStackTrace()
    }
}
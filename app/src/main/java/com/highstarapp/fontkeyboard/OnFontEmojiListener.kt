package com.highstarapp.fontkeyboard

import java.util.ArrayList

/*
when user click on font from scroll view list, this listener listen to that event
and pass the response to AppKeyboardView class to bind the selected font data
 */
interface OnFontEmojiListener {

    fun onFontChange(fonts : ArrayList<Models.AppFonts>,textSize:Int)

}
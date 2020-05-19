package com.highstarapp.fontkeyboard

/*
a model class to hold font code points, and sends to the @OnFontEmojiListener class method
 */
object Models {

    data class AppFonts(val codePoints: MutableList<Int>/*, val label:String*/)

}
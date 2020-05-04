package com.highstarapp.fontkeyboard


import android.content.Context
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.ArrayList
import javax.xml.parsers.DocumentBuilderFactory


class SoftKeyboard: InputMethodService(), AppKeyboardView.OnKeyboardActionListener {


    private lateinit var appKeyboardView: AppKeyboardView
    private lateinit var fontsContainer: LinearLayout
    private lateinit var appKeyboard : AppKeyboard
    private var caps : Boolean  = false
    private  lateinit var mInputMethodManager: InputMethodManager
    private lateinit var mWorkSeperator: String

    private lateinit var  qwertyKeyboard : AppKeyboard
    private lateinit var  symbolsKeyboardNumeric : AppKeyboard
    private lateinit var  symbolsKeyboardArithmetic : AppKeyboard
    private lateinit var parentLayout : LinearLayout
    private val fontsList:MutableList<String> = ArrayList()
    private val fontBubble:ArrayList<Models.AppFonts> = ArrayList()


    private fun parseXmlFont(){
        val inputStream = assets.open("fonts.xml")
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(inputStream)
        val element = doc.documentElement
        element.normalize()
        val nList = doc.getElementsByTagName("bubble_font")

        for (i in 0 until nList.length) {
            val node = nList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element2 = node as Element
                fontBubble.add(Models.AppFonts(getValue("code",element2).toInt(),getValue("label",element2)))
            }
        }
    }

    private fun getValue(tag: String, element: Element): String {
        val nodeList = element.getElementsByTagName(tag).item(0).childNodes
        val node = nodeList.item(0)
        return node.nodeValue
    }

    override fun onCreate() {
        super.onCreate()
        mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mWorkSeperator = resources.getString(R.string.word_separators)
        parseXmlFont()
    }

    override fun onCreateInputView(): View {
        parentLayout = layoutInflater.inflate(R.layout.keyboardview_layout,null) as LinearLayout
        appKeyboardView = parentLayout.findViewById(R.id.appKeyboardView)
        fontsContainer = parentLayout.findViewById(R.id.fontsContainer)
        appKeyboard = qwertyKeyboard
        appKeyboardView.keyboard = appKeyboard
        appKeyboardView.onKeyboardActionListener = this

        inflateFontsView()
        return parentLayout
    }

    private fun inflateFontsView() {
        fontsList.clear()
        fontsList.add("Share App")
        fontsList.add("Normal")
        fontsList.add("Serif2")
        fontsList.add("Serif3")
        fontsList.add("Serif4")
        fontsList.add("Serif5")
        fontsList.add("Serif6")
        fontsList.add("Serif7")
        fontsList.add("Serif8")
        fontsList.add("Serif9")
        fontsList.add("Serif10")

        val buttonViewList: MutableList<TextView> = ArrayList()

        for (i in 0 until fontsList.size){
            val cardView : CardView = CardView(this)
            val cardViewParams  = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            cardViewParams.setMargins(20,20,0,20)
            cardView.radius=5.0f
            cardView.cardElevation=10.0f
            cardView.layoutParams=cardViewParams
            val textView = TextView(this)
            val textViewParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
            textView.gravity=Gravity.CENTER
            textView.textSize = 16.0f
            textView.setPadding(30,15,30,15)
            textView.setTextColor(ContextCompat.getColor(this,R.color.colorBlack))
            textView.layoutParams = textViewParams
            textView.text = fontsList[i]
            textView.id = i+1

            cardView.addView(textView)
            fontsContainer.addView(cardView)
            buttonViewList.add(textView)
            textView.setTextColor(ContextCompat.getColor(this,R.color.colorBlack))
            textView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorWhite))
            textView.setOnClickListener {
                textView.setTextColor(ContextCompat.getColor(this,R.color.colorWhite))
                textView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorSelected))

                for (j in 0 until buttonViewList.size){
                    val textView1 = buttonViewList[j]
                    if (textView1.id!=textView.id){
                        textView1.setTextColor(ContextCompat.getColor(this,R.color.colorBlack))
                        textView1.setBackgroundColor(ContextCompat.getColor(this,R.color.colorWhite))
                    }
                }
                //  appKeyboardView.onFontChange(fontBubble)

            }
        }
    }


    override fun onInitializeInterface() {
        super.onInitializeInterface()
        qwertyKeyboard = AppKeyboard(this, R.xml.qwerty)
        symbolsKeyboardNumeric = AppKeyboard(this,R.xml.symbol_numeric)
        symbolsKeyboardArithmetic = AppKeyboard(this,R.xml.symbol_arithmetic)
    }

    override fun swipeRight() {

    }

    override fun onPress(primaryCode: Int) {

    }

    override fun onRelease(primaryCode: Int) {
    }

    override fun swipeLeft() {
    }

    override fun swipeUp() {
    }

    override fun swipeDown() {
    }



    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val inputConnection : InputConnection = currentInputConnection
        if (inputConnection!=null){
            when(primaryCode){
                AppKeyboard.KEYCODE_DELETE->{
                    inputConnection.deleteSurroundingText(1,0)
                }
                AppKeyboard.KEYCODE_SHIFT->{
                    caps = !caps
                    appKeyboard.isShifted = caps
                    appKeyboardView.invalidateAllKeys()
                }
                AppKeyboard.KEYCODE_DONE->{
                    inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER))
                }
                AppKeyboard.KEYCODE_MODE_CHANGE->{
                    val currentKeyboard : AppKeyboard = appKeyboardView.keyboard
                    appKeyboard = if (currentKeyboard == qwertyKeyboard) symbolsKeyboardNumeric else qwertyKeyboard
                    appKeyboardView.keyboard = appKeyboard
                }
                /*   AppKeyboard.KEYCODE_MODE_CHARACTERS_NUMERIC->{
                       val currentKeyboard: AppKeyboard = appKeyboardView.keyboard
                       appKeyboard = if (currentKeyboard == symbolsKeyboardNumeric) symbolsKeyboardArithmetic else symbolsKeyboardNumeric
                       appKeyboardView.keyboard = appKeyboard
                   }*/
                -101->{
                    val imeManager : InputMethodManager = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imeManager.showInputMethodPicker()
                }
                else->{
                    var code : Char = primaryCode.toChar()
                    if (Character.isLetter(code) && caps){
                        code = Character.toUpperCase(code)
                    }
                    inputConnection.commitText(code.toString(),1)
                }
            }
        }
    }


    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyLongPress(keyCode, event)

    }


    override fun onText(text: CharSequence?) {
    }




}
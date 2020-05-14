package com.highstarapp.fontkeyboard


import android.app.Activity
import android.content.Context
import android.icu.lang.UCharacter
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.*
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.StringWriter
import java.util.ArrayList
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


@Suppress("DEPRECATION")
class SoftKeyboard: InputMethodService(), AppKeyboardView.OnKeyboardActionListener {


    private lateinit var appKeyboardView: AppKeyboardView
    private lateinit var fontsContainer: LinearLayout
    private lateinit var appKeyboard : AppKeyboard
    private var caps : Boolean  = false
    private  lateinit var mInputMethodManager: InputMethodManager
    private lateinit var mWorkSeparator: String

    private lateinit var  qwertyKeyboard : AppKeyboard
    private lateinit var  symbolsKeyboardNumeric : AppKeyboard
    private lateinit var  symbolsKeyboardArithmetic : AppKeyboard
    private lateinit var parentLayout : ConstraintLayout
    private var fontsList = arrayOf<String>()
    private val fontCharacterArray:ArrayList<Models.AppFonts> = ArrayList()

    private var fontXmlArray = arrayOf<String>()
    private var fontPosition : Int = 0
    private lateinit var  document : Document

    private  var selectedFont: String? = null



    private fun parseXmlFont(fontXmlFile: String,fontLabel:String){
        if (fontPosition!=0)
        saveKeyboard(fontXmlFile,fontLabel,fontPosition.toString())

        val inputStream = assets.open(fontXmlFile)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        document = dBuilder.parse(inputStream)
        val element = document.documentElement
        element.normalize()
        val nList = document.getElementsByTagName(Constant.XML_NODE)
        fontCharacterArray.clear()
        var isCodeSame = false

        for (i in 0 until nList.length) {
            val node = nList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element2 = node as Element
                var splitCodePoint: List<String> = listOf()
                val codePoints: MutableList<Int> = mutableListOf()

                    if (getValue(fontLabel, element2).contains(" ")) {
                        splitCodePoint = getValue(fontLabel, element2).split(" ")
                        for (j in splitCodePoint.indices) {
                            codePoints.add(splitCodePoint[j].toInt())
                        }
                        fontCharacterArray.add(Models.AppFonts(codePoints/*,getValue("label",element2)*/))
                    } else {
                        codePoints.add(getValue(fontLabel, element2).toInt())
                        fontCharacterArray.add(Models.AppFonts(codePoints/*,getValue("label",element2)*/))
                    }
               // fontCharacterArray.add(Models.AppFonts(getValue("code",element2).toInt(),getValue("label",element2)))
            }
        }
        appKeyboardView.onFontChange(fontCharacterArray)
    }

    private fun checkIfNodeExist(document: Document, pathExpression: String):Boolean{
        var matches = false
        val xPathFactory = XPathFactory.newInstance()
        val xPath = xPathFactory.newXPath()
        try{
            val xPathExpression = xPath.compile(pathExpression)
            val nodes: NodeList = xPathExpression.evaluate(document,XPathConstants.NODESET) as NodeList
            if (nodes!=null && nodes.length>0){
                matches=true
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return matches
    }

    private fun getValue(tag: String, element: Element): String {
        val nodeList = element.getElementsByTagName(tag).item(0).childNodes
        val node = nodeList.item(0)
        return node.nodeValue
    }


    override fun onCreate() {
        super.onCreate()
        mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mWorkSeparator = resources.getString(R.string.word_separators)
        fontsList =  resources.getStringArray(R.array.fontName)
        fontXmlArray = resources.getStringArray(R.array.xmlFontNames)
    }


    private fun hideKeyboard(){
        parentLayout.visibility= View.GONE
        appKeyboardView.isEnabled=false
    }
    private fun showKeyboard(){
        parentLayout.visibility= View.VISIBLE
        appKeyboardView.isEnabled=true
        if (parentLayout!=null) {
            (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(parentLayout.windowToken, 0)
        }
    }

    private fun isKeyboardVisible():Boolean{
        return appKeyboardView.visibility == View.VISIBLE
    }

    override fun onCreateInputView(): View {
        parentLayout = layoutInflater.inflate(R.layout.keyboardview_layout,null) as ConstraintLayout
        appKeyboardView = parentLayout.findViewById(R.id.appKeyboardView)
        fontsContainer = parentLayout.findViewById(R.id.fontsContainer)
        appKeyboard = qwertyKeyboard
        appKeyboardView.keyboard = appKeyboard
        appKeyboardView.onKeyboardActionListener = this
        inflateFontsView()
        try{
            if (!getKeyboard().isNullOrEmpty() && !getCode().isNullOrEmpty())
                getCode()?.let { getKeyboard()?.let { it1 -> parseXmlFont(it1, it) } }
            Log.e("dfdsf-sfs%#%", getKeyboard())
            Log.e("dfdCODEs%#%", getCode())
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

        return parentLayout
    }

    private fun inflateFontsView() {

        val buttonViewList: MutableList<TextView> = ArrayList()
    try {
        for (i in fontsList.indices){
            val cardView : CardView = CardView(this)
            val cardViewParams  = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            cardViewParams.setMargins(20,25,0,20)
            cardView.radius=5.0f
            cardView.cardElevation=10.0f
            cardView.layoutParams=cardViewParams
            val textView = TextView(this)
            val textViewParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
            textView.gravity=Gravity.CENTER
            textView.textSize = 16.0f
            textView.setPadding(35,16,35,16)
            textView.setTextColor(ContextCompat.getColor(this,R.color.colorBlack))
            textView.layoutParams = textViewParams
            textView.text = fontsList[i]
            textView.id = i+1

            cardView.addView(textView)
            fontsContainer.addView(cardView)
            buttonViewList.add(textView)
            if (!getSelectedFontPosition().isNullOrEmpty()){
                if(i==getSelectedFontPosition()?.toInt()) {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                    textView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSelected))
                }
                else {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                    textView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
                }
            }else {
                if (i == 1) {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                    textView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSelected))
                } else {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                    textView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
                }
            }

            textView.setOnClickListener {
                fontPosition = i
                textView.setTextColor(ContextCompat.getColor(this,R.color.colorWhite))
                textView.setBackgroundColor(ContextCompat.getColor(this,R.color.colorSelected))

                for (j in 0 until buttonViewList.size){
                    val textView1 = buttonViewList[j]
                    if (textView1.id!=textView.id){
                        textView1.setTextColor(ContextCompat.getColor(this,R.color.colorBlack))
                        textView1.setBackgroundColor(ContextCompat.getColor(this,R.color.colorWhite))
                    }
                }
                if (i!=0){
                    if (appKeyboardView.keyboard==qwertyKeyboard) parseXmlFont(fontXmlArray[i],Constant.CODE)
                    else selectedFont = fontXmlArray[i]
                }else{

                }
            }
        }
    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }

    }


    override fun onInitializeInterface() {
        super.onInitializeInterface()
        try {
            qwertyKeyboard = AppKeyboard(this, R.xml.qwerty)
            symbolsKeyboardNumeric = AppKeyboard(this,R.xml.symbol_numeric)
            symbolsKeyboardArithmetic = AppKeyboard(this,R.xml.symbol_arithmetic)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }



    override fun swipeRight() {
    }

    override fun onPress(primaryCode: Int) {
    }

    override fun onRelease(primaryCode: IntArray?) {
    }

    override fun swipeLeft() {
    }

    override fun swipeUp() {
    }

    override fun swipeDown() {
    }


    override fun onKey(primaryCode: IntArray?, keyCodes: IntArray?) {
        val inputConnection : InputConnection = currentInputConnection
        try {
            if (inputConnection!=null){
                if (primaryCode!!.size>1){
                    val codePint: IntArray = primaryCode
                    inputConnection.commitText(FontArray.newStrings(codePint),1)
                }
                else{
                    val pCode: Int = primaryCode[0]
                    when(pCode){
                        AppKeyboard.KEYCODE_DELETE[0]-> {
                            //inputConnection.deleteSurroundingText(1, 0)
                            inputConnection.deleteSurroundingTextInCodePoints(1,0)
                        }
                        AppKeyboard.KEYCODE_SHIFT-> {
                            caps = !caps
                            appKeyboardView.invalidateAllKeys()
                            if (fontPosition != 0){
                                if (caps) {
                                    if (checkIfNodeExist(document,Constant.CHECK_CODE_CAP_EXIST))
                                        parseXmlFont(fontXmlArray[fontPosition], Constant.CODE_CAPS)
                                }
                                else{
                                    parseXmlFont(fontXmlArray[fontPosition], Constant.CODE)
                                }
                            }
                            else {
                                appKeyboardView.invalidateAllKeys()
                                appKeyboard.isShifted = caps
                            }
                        }
                        AppKeyboard.KEYCODE_DONE->{
                            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER))
                        }
                        AppKeyboard.KEYCODE_MODE_CHANGE->{
                            val currentKeyboard : AppKeyboard = appKeyboardView.keyboard
                            appKeyboard = if (currentKeyboard == qwertyKeyboard) symbolsKeyboardNumeric else qwertyKeyboard
                            appKeyboardView.keyboard = appKeyboard
                            if (selectedFont!=null) {
                                parseXmlFont(selectedFont!!, Constant.CODE)
                                selectedFont=null
                            }
                        }
                        AppKeyboard.KEYCODE_MODE_CHARACTERS_NUMERIC->{
                            val currentKeyboard: AppKeyboard = appKeyboardView.keyboard
                            appKeyboard = if (currentKeyboard == symbolsKeyboardNumeric) symbolsKeyboardArithmetic else symbolsKeyboardNumeric
                            appKeyboardView.keyboard = appKeyboard

                        }
                        AppKeyboard.KEYCODE_LANGUAGE_SWITCH->{
                            try {
                                val imeManager : InputMethodManager= this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imeManager.showInputMethodPicker()
                            }
                            catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                        else->{
                            var code : Char = pCode.toChar()
                            if (Character.isLetter(code) && caps){
                                if (fontPosition == 0)
                                    code = Character.toUpperCase(code)
                            }
                            if (pCode.toString().length>=6){
                                //for decimal code point of utf-16 unicode characters
                                inputConnection.commitText(FontArray.newString(pCode),1)
                                // inputConnection.setComposingText(FontArray.setChar(),1)
                            }else{
                                //for decimal code point of utf-8 unicode characters
                                inputConnection.commitText(code.toString(),1)
                            }
                        }
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_ENTER->{
                // Let the underlying text editor always handle these.
                return false
            }
            else->{

            }
        }
        return super.onKeyDown(keyCode, event)

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    override fun onText(text: CharSequence?) {
    }


}
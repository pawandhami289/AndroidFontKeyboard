package com.highstarapp.fontkeyboard


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.UnicodeSet
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.lang.StringBuilder
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/*
this is the main class of this app, which is used for:
-register app to android os
-listen key press events
-save fonts to local storage when device orientation changes
etc..
 */
class SoftKeyboard: InputMethodService(), AppKeyboardView.OnKeyboardActionListener {

    private lateinit var appKeyboardView: AppKeyboardView//app keyboard layout
    private lateinit var fontsContainer: LinearLayout//fonts root layout
    private lateinit var horizontalScrollView: HorizontalScrollView//horizontal scroll view to hold custom fonts
    private lateinit var appKeyboard : AppKeyboard//app keyboard, keys, rows, key size, no. of keys in a row etc
    private var caps : Boolean  = false//check for caps letters
    private  lateinit var mInputMethodManager: InputMethodManager//
    private lateinit var mWorkSeparator: String
    private lateinit var  qwertyKeyboard : AppKeyboard//normal keyboard
    private lateinit var  symbolsKeyboardNumeric : AppKeyboard//numeric keyboard
    private lateinit var  symbolsKeyboardArithmetic : AppKeyboard//arithmetic symbols keyboard
    private lateinit var parentLayout : ConstraintLayout//app keyboard root layout
    private var fontsNameList:ArrayList<Models.AppFonts> = ArrayList()//list of all fonts labels/names
    private val fontCharacterArray:ArrayList<Models.AppFonts> = ArrayList()//holds selected font code points, which is used to draw character and output character.
    private var fontXmlArray = arrayOf<String>()//holds font code points from assets folder(location: app/assets)
    private var fontPosition : Int = 0//current selected font position
    private var alreadySelectedPosition : Int = 0//current selected font position
    private lateinit var  document : Document//parse xml file
    private  var editorInfo: EditorInfo?=null// get the other apps editor info


    private fun parseXmlFontNames(){
        val inputStream = assets.open(Constant.FONT_NAME_XML)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(inputStream)
        val element = doc.documentElement
        element.normalize()
        val nList = doc.getElementsByTagName(Constant.XML_NODE)
        fontsNameList.clear()
        for (i in 0 until nList.length) {
            val node = nList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element2 = node as Element
                var splitCodePoint: List<String> = listOf()
                val codePoints: MutableList<Int> = mutableListOf()
                if (getValue(Constant.FONT_NAME, element2).contains(" ")) {
                    splitCodePoint = getValue(Constant.FONT_NAME, element2).split(" ")
                    for (j in splitCodePoint.indices) {
                        codePoints.add(splitCodePoint[j].toInt())
                    }
                    fontsNameList.add(Models.AppFonts(codePoints))
                } else {
                    codePoints.add(getValue(Constant.FONT_NAME, element2).toInt())
                    fontsNameList.add(Models.AppFonts(codePoints))
                }
            }
        }
    }

    //parse xml files from assets folder to get code points of unicode character.
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
            }
        }
            appKeyboardView.onFontChange(fontCharacterArray,resources.getDimensionPixelSize(R.dimen.key_text_size))
    }

    //to get code point values from xml present in assets folder
    private fun getValue(tag: String, element: Element): String {
        val nodeList = element.getElementsByTagName(tag).item(0).childNodes
        val node = nodeList.item(0)
        return node.nodeValue
    }

    //to check if xml file having codeCap or not, because all unicode character does not have uppercase character
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


    //callback function of InputMethodService
    override fun onCreate() {
        super.onCreate()
        adjustFontScale(resources.configuration)
        mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mWorkSeparator = resources.getString(R.string.word_separators)
        fontXmlArray = resources.getStringArray(R.array.xmlFontNames)
        parseXmlFontNames()
        try{
            val html= Html.fromHtml(129397.toString())
           // Log.e("fdsfdshtml=", ""+html)
        }catch (e:Exception){
            e.printStackTrace()
        }
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

    //inflate the ui or keyboard
    override fun onCreateInputView(): View {
        parentLayout = layoutInflater.inflate(R.layout.keyboardview_layout,null) as ConstraintLayout
        appKeyboardView = parentLayout.findViewById(R.id.appKeyboardView)
        fontsContainer = parentLayout.findViewById(R.id.fontsContainer)
        horizontalScrollView = parentLayout.findViewById(R.id.horizontalScrollView)
        appKeyboard = qwertyKeyboard
        appKeyboardView.keyboard = appKeyboard
        appKeyboardView.onKeyboardActionListener = this
        inflateFontsView()
        getSavedFont()
        return parentLayout
    }

    /*save selected font, because on orientation changes, all data will be loss and ui recreate, so we save selected font and display
    on orientation changes, it enhance user interaction to the app
     */
    private fun getSavedFont(){
        try{
            if (!getKeyboard().isNullOrEmpty() && !getCode().isNullOrEmpty())
                getCode()?.let { getKeyboard()?.let { it1 -> parseXmlFont(it1, it) } }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }

    // inflate fonts ui and  horizontal scroll view on top of the keyboard
    private fun inflateFontsView() {
        val buttonViewList: MutableList<TextView> = ArrayList()
    try {
        for (i in fontsNameList.indices){
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
            textView.setTextColor(ContextCompat.getColor(this,R.color.colorBlack))
            val size = fontsNameList[i].codePoints.size
            val intArray = IntArray(size)
            if (size>1){
                for (j in 0 until size){
                    intArray[j]=fontsNameList[i].codePoints[j]
                }
                textView.text = UnicodeUtils.bindMultipleCodePoints(intArray)
            }else{
               val len: Int = intArray[0]
            }
            textView.id = i+1
            textView.setPadding(35, 16, 35, 16)
            textView.layoutParams = textViewParams
            cardView.addView(textView)
            fontsContainer.addView(cardView)
            buttonViewList.add(textView)
            if (!getSelectedFontPosition().isNullOrEmpty()){
                fontPosition=getSelectedFontPosition()!!.toInt()
                if(i==fontPosition) {
                    val selectedView = textView.findViewById<TextView>(textView.id)//scroll to selected font position
                    selectedView.parent.requestChildFocus(selectedView,selectedView)
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
                if (i!=0){
                    fontPosition = i
                    if (fontPosition!=alreadySelectedPosition) {//if user tap the already selected position
                        caps=false
                        updateShiftKey()
                        alreadySelectedPosition=fontPosition
                        textView.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
                        textView.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.colorSelected
                            )
                        )
                        if (appKeyboardView.keyboard == qwertyKeyboard) parseXmlFont(
                            fontXmlArray[i],
                            Constant.CODE
                        )
                        else saveKeyboard(fontXmlArray[i], Constant.CODE, fontPosition.toString())
                        for (j in 0 until buttonViewList.size) {
                            val textView1 = buttonViewList[j]
                            if (textView1.id != textView.id) {
                                textView1.setTextColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.colorBlack
                                    )
                                )
                                textView1.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.colorWhite
                                    )
                                )
                            }
                        }
                    }
                }else{
                    try{
                        val webPage: Uri = Uri.parse(Constant.WEB_URL)
                        val intent = Intent(Intent.ACTION_VIEW, webPage)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }
    }

    //called after onCreate() method. initialize the basic setup
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

    //more  info on AppKeyboardView
    override fun swipeRight() {
    }
    //more  info on AppKeyboardView
    override fun onPress(primaryCode: Int) {
       // appKeyboardView.setKeyBackground(resources.getDrawable(R.drawable.pressed))
    }
    //more  info on AppKeyboardView
    override fun onRelease(primaryCode: IntArray?) {
       // appKeyboardView.setKeyBackground(resources.getDrawable(R.drawable.normal))
    }
    //more  info on AppKeyboardView
    override fun swipeLeft() {
    }
    //more  info on AppKeyboardView
    override fun swipeUp() {
    }
    //more  info on AppKeyboardView
    override fun swipeDown() {
    }

    private fun updateShiftKey(){
        val keys: MutableList<AppKeyboard.Key> = qwertyKeyboard.keys
        var currentKey: AppKeyboard.Key
        for (i in 0 until keys.size){
            currentKey = keys[i]
            appKeyboardView.invalidateAllKeys()
            if (currentKey.codes[0]==-1){
                currentKey.label = null
                if (appKeyboardView.isShifted || caps){
                    currentKey.icon = resources.getDrawable(R.drawable.ic_keyboard_capslock_blue_24dp)

                }else{
                    currentKey.icon = resources.getDrawable(R.drawable.ic_keyboard_capslock_black_24dp)
                }
            }
        }
    }

    //gives the current editor info of the using app like, watspp, intagram, facebook etc.
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
       // Log.e("onStartInput:", attribute!!.imeOptions.toString())
    }
    //called after onStartInput()
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfo = info!!
       // Log.e("onStartInputView:", info.imeOptions.toString())
    }

    private val outPutTextList: MutableList<Int> = mutableListOf()
    private var outputText: StringBuilder = StringBuilder()
    //when key is pressed, more  info on AppKeyboardView
    override fun onKey(primaryCode: IntArray?, keyCodes: IntArray?) {
        val inputConnection : InputConnection = currentInputConnection
        try {
            if (inputConnection!=null){
                if (primaryCode!!.size>1){
                    val codePint: IntArray = primaryCode
                    val stringBuilder = UnicodeUtils.bindMultipleCodePoints(codePint)
                    inputConnection.commitText(stringBuilder.trim(),1)

                }
                else{
                    val pCode: Int = primaryCode[0]
                    when(pCode){
                        AppKeyboard.KEYCODE_DELETE[0]-> {
                            //inputConnection.deleteSurroundingText(1, 0)
                         inputConnection.deleteSurroundingTextInCodePoints(1,0)
                        }
                        AppKeyboard.KEYCODE_SHIFT-> {
                            if (fontCharacterArray.isNotEmpty()){
                                if (checkIfNodeExist(document,Constant.CHECK_CODE_CAP_EXIST)){
                                    caps = !caps
                                    appKeyboardView.invalidateAllKeys()
                                    if (caps) {
                                        parseXmlFont(fontXmlArray[fontPosition], Constant.CODE_CAPS)
                                    }else{
                                        parseXmlFont(fontXmlArray[fontPosition], Constant.CODE)
                                    }
                                }
                            }
                            else {
                                caps = !caps
                                appKeyboardView.invalidateAllKeys()
                                appKeyboard.isShifted = caps
                            }
                            updateShiftKey()
                        }
                        AppKeyboard.KEYCODE_DONE->{
                            if (editorInfo!=null){
                                when(editorInfo!!.imeOptions){
                                    EditorInfo.IME_ACTION_GO-> inputConnection.performEditorAction(EditorInfo.IME_ACTION_GO)
                                    EditorInfo.IME_ACTION_NEXT-> inputConnection.performEditorAction(EditorInfo.IME_ACTION_NEXT)
                                    EditorInfo.IME_ACTION_SEARCH-> inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
                                    EditorInfo.IME_ACTION_SEND-> inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
                                    EditorInfo.IME_ACTION_DONE-> inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE)
                                    else->inputConnection.performEditorAction(EditorInfo.IME_ACTION_GO)
                                }
                            }
                        }
                        AppKeyboard.KEYCODE_MODE_CHANGE->{
                            val currentKeyboard : AppKeyboard = appKeyboardView.keyboard
                            appKeyboard = if (currentKeyboard == qwertyKeyboard) symbolsKeyboardNumeric else qwertyKeyboard
                            appKeyboardView.keyboard = appKeyboard
                            if (appKeyboardView.keyboard == qwertyKeyboard) {
                                getSavedFont()
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
                                val composingText: String = UnicodeUtils.bindSingleCodePoint(pCode)
                                inputConnection.commitText(composingText,1)
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

    //more  info on AppKeyboardView
    override fun onText(text: CharSequence?) {
    }


}
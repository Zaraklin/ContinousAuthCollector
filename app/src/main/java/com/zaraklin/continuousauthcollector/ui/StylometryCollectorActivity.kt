package com.zaraklin.continuousauthcollector.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.zaraklin.continuousauthcollector.R
import com.zaraklin.continuousauthcollector.data.RTDb.KeyPressedItem
import com.zaraklin.continuousauthcollector.data.RTDb.KeyPressedItemList
import com.zaraklin.continuousauthcollector.util.Singletons
import kotlinx.android.synthetic.main.activity_stylometry_collector.*
import kotlinx.android.synthetic.main.content_stylometry_collector.*
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

class StylometryCollectorActivity : AppCompatActivity() {

    var localDateTimeBefore : LocalDateTime? = null
    var keyPressedList : ArrayList<KeyPressedItem> = arrayListOf()
    var currentUid : String = ""
    var currentStylometryText : Int = 0
    val TAG = "StylometryCollector"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stylometry_collector)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_school_24dp)
        supportActionBar?.title = "    Cont. Auth Collector"

        atachStylomtryCollector()
        btSubmitTxtUser.setOnClickListener{ persistCollectedData() }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext) ?: return
        currentUid = sharedPref.getString(getString(R.string.uid_key), "")
        currentStylometryText = sharedPref.getInt(getString(R.string.stylometry_text_key), 1)
    }

    override fun onResume() {
        super.onResume()

        Log.i(TAG, "Collecting Stylometry")
        dayText()
    }

    private fun dayText(){
        txtRandom.text = resources.getStringArray(R.array.txt_random)[currentStylometryText]
    }

    private fun atachStylomtryCollector(){
        txtUserInput.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var longDiff : Long = 0L
                var key : String = ""

                longDiff = 0L
                if (localDateTimeBefore != null){
                    longDiff = (LocalDateTime.now().millisOfDay - localDateTimeBefore!!.millisOfDay).toLong()
                }

                if (count < before){
                    key = "[Backspace]"
                }
                else{
                    key = s.toString().get(start + count - 1).toString()

                    when(key.get(0).toInt()){
                        10 -> key = "[Enter]"
                        13 -> key = "[Space]"
                        32 -> key = "[Space]"
                    }
                }

                var keyPressedItem : KeyPressedItem =
                    KeyPressedItem(key, DateTime.now(), longDiff)
                keyPressedList.add(keyPressedItem)

                localDateTimeBefore = LocalDateTime.now()
            }
        })
    }

    private fun persistCollectedData(){
        Log.i(TAG, "Sending Stylometry to remote database")

        val kPList : List<KeyPressedItem> = keyPressedList.toList()
        Singletons.firebaseUtils.addListKeyPressed(KeyPressedItemList(currentUid, kPList))

        val newId = if (currentStylometryText == 0) 1 else 0
        val now : DateTime = DateTime.now()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext) ?: return
        with (sharedPref.edit()) {
            putInt(getString(R.string.stylometry_text_key), newId)
            putLong(getString(R.string.last_stylometry_collection), now.millis)
            commit()
        }

        Snackbar.make(currentFocus, "Sending data", Snackbar.LENGTH_SHORT).show()
        finish()
    }
}

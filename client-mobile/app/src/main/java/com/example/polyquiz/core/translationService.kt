package com.example.polyquiz.core


import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference

object TranslationService {

    fun saveLanguageToDB(language: String, configsRef: DatabaseReference) {
        configsRef.child("lang").setValue(language)
    }

    // Inspired : https://stackoverflow.com/questions/69235575/return-a-value-from-a-listeners-onsuccess-in-kotlin-android
    /*
     * How to use :
     * getLanguageFromDB(configsRef) { lang ->
     *     // Do stuff with lang
     * }
     */
    fun getLanguageFromDB(configsRef: DatabaseReference, callback: (String) -> Unit) {
        configsRef.child("lang").get().addOnSuccessListener { dataSnapshot : DataSnapshot ->
            if(dataSnapshot.exists()) {
                callback(dataSnapshot.value as String) // return lang in callback
            } else {
                // If we reach else block, it means its an account created before configs was a thing
                // TODO : Set account language to french?
                callback("fr") // default to french
            }
        }
    }

    fun setLanguage(language: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language)
        )
    }


}

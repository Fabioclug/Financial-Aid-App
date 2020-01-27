package br.com.fclug.financialaid.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Fabioclug on 2017-10-21.
 */
@Parcelize
data class OnlineUser(override var username: String, var name: String) : User(username),
        Parcelable {

    override fun getExhibitName(): String {
        return name
    }
}
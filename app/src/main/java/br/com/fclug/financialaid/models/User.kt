package br.com.fclug.financialaid.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Fabioclug on 2016-09-06.
 */
@Parcelize
open class User(open var username: String) : Parcelable {

    open fun getExhibitName(): String {
        return username
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is User) return false
        return username == other.username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }
}
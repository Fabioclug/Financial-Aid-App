package br.com.fclug.financialaid.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Fabioclug on 2016-10-17.
 */
@Parcelize
data class TransactionSplit(var debtor: User, var value: Long) : Parcelable {

    //TODO: remove additional constructors after Kotlin migration is complete
    constructor(other: TransactionSplit) : this(other.debtor, other.value)
}
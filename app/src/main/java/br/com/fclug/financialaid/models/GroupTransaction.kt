package br.com.fclug.financialaid.models

import android.os.Parcel
import android.os.Parcelable
import br.com.fclug.financialaid.models.TransactionSplit
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Fabioclug on 2016-10-17.
 */
@Parcelize
data class GroupTransaction(override var id: Long, var description: String, var payer: User,
                       var value: Long, var date: Date, var splits: MutableList<TransactionSplit>)
    : UniqueObject(), Parcelable {

    //TODO: remove additional constructors after Kotlin migration is complete
    constructor(id: Long, description: String, payer: User, value: Long, date: Date): this(id,
            description, payer, value, date, ArrayList())

    constructor(description: String, payer: User, value: Long, date: Date,
                splits: MutableList<TransactionSplit>):
            this(-1, description, payer, value, date, splits)

    fun addSplit(split: TransactionSplit) {
        splits.add(split)
    }
}
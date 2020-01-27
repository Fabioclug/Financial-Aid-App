package br.com.fclug.financialaid.models

import br.com.fclug.financialaid.utils.AppUtils
import java.util.*

/**
 * Created by Fabioclug on 2016-06-09.
 */
data class Transaction(override var id: Long, var description: String, var value: Long,
                       var category: Category, var date: Date,
                       var accountId: Long) : UniqueObject() {

    //TODO: remove additional constructor after Kotlin migration is complete
    constructor(description: String, value: Long, category: Category, date: Date, accountId: Long) :
            this(0, description, value, category, date, accountId)

    val formattedValue: String
        get() = AppUtils.formatCurrencyValue(value)

}
package br.com.fclug.financialaid.models

import br.com.fclug.financialaid.utils.AppUtils

data class Account(override var id: Long, var name: String, var balance: Long, var type: String) :
        UniqueObject() {

    //TODO: remove additional constructor after Kotlin migration is complete
    constructor(name: String, balance: Long, type: String): this(0, name, balance, type)

    val formattedBalance: String
        get() = AppUtils.formatCurrencyValue(balance)
}
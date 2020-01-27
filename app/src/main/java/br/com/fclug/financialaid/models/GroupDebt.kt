package br.com.fclug.financialaid.models

/**
 * Created by Fabioclug on 2016-10-17.
 */
data class GroupDebt(var creditor: User, var debtor: User, var value: Long) {

    fun swapMembers() {
        val aux = creditor
        creditor = debtor
        debtor = aux
        value *= -1
    }

}
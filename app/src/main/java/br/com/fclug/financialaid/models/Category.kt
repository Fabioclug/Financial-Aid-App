package br.com.fclug.financialaid.models

/**
 * Created by Fabioclug on 2017-01-08.
 */
data class Category(var name: String, var color: Int, var isIncoming: Boolean) {

    override fun toString(): String {
        return name
    }

}
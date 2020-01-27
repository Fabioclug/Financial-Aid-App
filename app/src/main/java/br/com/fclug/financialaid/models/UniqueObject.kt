package br.com.fclug.financialaid.models

//TODO: change to an interface once all classes that extend UniqueObject are migrated to Kotlin
abstract class UniqueObject {
    abstract var id: Long
}
package br.com.fclug.financialaid.server

data class ApiResponseError(val title: String, val description: String, val status: Int)

data class ApiResponse<T>(val message: String, val errors: List<ApiResponseError>, val result: T)

data class UserCreation(val username: String, val name: String, val password: String)

data class ExistingUser(val existing: Boolean)

package br.com.fclug.financialaid.server

data class ApiError(val title: String, val description: String, val status: Int)

data class ApiResponseError(val errors: List<ApiError>)

data class ApiResponse<T>(val message: String, val result: T)

data class UserCreation(val username: String, val name: String, val password: String)

data class UsernameCheck(val username: String)

data class ExistingUser(val existing: Int)

data class UserLogin(val username: String, val password: String)

data class LoginSession(val name: String, val token: String)

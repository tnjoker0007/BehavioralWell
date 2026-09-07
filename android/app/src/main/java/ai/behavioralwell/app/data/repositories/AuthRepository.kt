package ai.behavioralwell.app.data.repositories

import ai.behavioralwell.app.core.network.RetrofitClient
import ai.behavioralwell.app.core.security.TokenStorage
import ai.behavioralwell.app.data.models.TokenResponse
import ai.behavioralwell.app.data.models.UserCreateRequest
import ai.behavioralwell.app.data.models.UserLoginRequest

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

class AuthRepository(private val tokenStorage: TokenStorage) {

    private val api = RetrofitClient.apiService

    suspend fun login(email: String, password: String): NetworkResult<TokenResponse> {
        return try {
            val response = api.login(UserLoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val tokenResp = response.body()!!
                tokenStorage.saveTokens(
                    accessToken = tokenResp.accessToken,
                    refreshToken = tokenResp.refreshToken,
                    userId = tokenResp.user.id,
                    email = tokenResp.user.email,
                    name = tokenResp.user.name
                )
                NetworkResult.Success(tokenResp)
            } else {
                NetworkResult.Error("Login failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun register(name: String, email: String, password: String): NetworkResult<TokenResponse> {
        return try {
            val response = api.register(UserCreateRequest(name, email, password))
            if (response.isSuccessful && response.body() != null) {
                val tokenResp = response.body()!!
                tokenStorage.saveTokens(
                    accessToken = tokenResp.accessToken,
                    refreshToken = tokenResp.refreshToken,
                    userId = tokenResp.user.id,
                    email = tokenResp.user.email,
                    name = tokenResp.user.name
                )
                NetworkResult.Success(tokenResp)
            } else {
                NetworkResult.Error("Registration failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun logout() {
        tokenStorage.clearTokens()
    }
}

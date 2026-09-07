package ai.behavioralwell.app.data.api

import ai.behavioralwell.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface BehavioralWellApiService {

    // --- Authentication ---
    @POST("auth/register")
    suspend fun register(@Body request: UserCreateRequest): Response<TokenResponse>

    @POST("auth/login")
    suspend fun login(@Body request: UserLoginRequest): Response<TokenResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    // --- Consent Management ---
    @GET("consent")
    suspend fun getConsent(): Response<ConsentResponse>

    @PUT("consent")
    suspend fun updateConsent(@Body request: ConsentUpdateRequest): Response<ConsentResponse>

    // --- Telemetry Ingestion ---
    @POST("telemetry")
    suspend fun sendTelemetry(@Body input: TelemetryInput): Response<Map<String, Any>>

    @POST("telemetry/batch")
    suspend fun sendBatchTelemetry(@Body input: BatchTelemetryInput): Response<Map<String, Any>>

    @GET("telemetry/status")
    suspend fun getTelemetryStatus(): Response<TelemetryStatusResponse>

    // --- Baseline & Risk Staging ---
    @GET("baseline/progress")
    suspend fun getBaselineProgress(): Response<BaselineProgressResponse>

    @GET("risk/current")
    suspend fun getCurrentRisk(): Response<RiskAssessmentResponse>

    // --- Interventions & Self Check ---
    @POST("interventions/start")
    suspend fun startIntervention(@Body input: InterventionStartInput): Response<Map<String, Any>>

    @POST("interventions/complete")
    suspend fun completeIntervention(@Body input: InterventionCompleteInput): Response<Map<String, Any>>

    @POST("self-check")
    suspend fun submitSelfCheck(@Body input: SelfReportInput): Response<Map<String, Any>>

    // --- Unified Dashboard ---
    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>
}

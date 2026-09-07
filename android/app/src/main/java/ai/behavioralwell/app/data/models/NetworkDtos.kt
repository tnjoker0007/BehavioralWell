package ai.behavioralwell.app.data.models

import com.google.gson.annotations.SerializedName

// --- Auth DTOs ---
data class UserCreateRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("age_group") val ageGroup: String = "25-34",
    @SerializedName("occupation_category") val occupationCategory: String = "Technology"
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class TokenRefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("age_group") val ageGroup: String,
    val timezone: String,
    @SerializedName("occupation_category") val occupationCategory: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("baseline_completed") val baselineCompleted: Boolean
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer",
    val user: UserResponse
)

// --- Consent DTOs ---
data class ConsentUpdateRequest(
    @SerializedName("keyboard_enabled") val keyboardEnabled: Boolean,
    @SerializedName("usage_enabled") val usageEnabled: Boolean,
    @SerializedName("motion_enabled") val motionEnabled: Boolean,
    @SerializedName("work_enabled") val workEnabled: Boolean,
    @SerializedName("mobility_enabled") val mobilityEnabled: Boolean
)

data class ConsentResponse(
    @SerializedName("keyboard_enabled") val keyboardEnabled: Boolean,
    @SerializedName("usage_enabled") val usageEnabled: Boolean,
    @SerializedName("motion_enabled") val motionEnabled: Boolean,
    @SerializedName("work_enabled") val workEnabled: Boolean,
    @SerializedName("mobility_enabled") val mobilityEnabled: Boolean,
    @SerializedName("user_id") val userId: String,
    @SerializedName("updated_at") val updatedAt: String
)

// --- Telemetry & Batch Ingestion ---
data class TelemetryInput(
    @SerializedName("idempotency_key") val idempotencyKey: String? = null,
    val timestamp: String? = null,

    // Keyboard
    @SerializedName("typing_speed") val typingSpeed: Float? = null,
    @SerializedName("key_press_duration") val keyPressDuration: Float? = null,
    @SerializedName("pause_duration") val pauseDuration: Float? = null,
    @SerializedName("correction_rate") val correctionRate: Float? = null,

    // Screen & Usage
    @SerializedName("screen_time") val screenTime: Float? = null,
    @SerializedName("unlock_count") val unlockCount: Int? = null,
    @SerializedName("night_usage") val nightUsage: Float? = null,
    @SerializedName("app_switch_frequency") val appSwitchFrequency: Float? = null,

    // Motion
    @SerializedName("movement_intensity") val movementIntensity: Float? = null,
    @SerializedName("acceleration_variance") val accelerationVariance: Float? = null,
    @SerializedName("stationary_duration") val stationaryDuration: Float? = null,

    // Work / Tasks
    @SerializedName("task_accuracy") val taskAccuracy: Float? = null,
    @SerializedName("task_completion_time") val taskCompletionTime: Float? = null,
    @SerializedName("task_error_rate") val taskErrorRate: Float? = null,

    // Mobility
    @SerializedName("speed_variance") val speedVariance: Float? = null,
    @SerializedName("route_variability") val routeVariability: Float? = null
)

data class BatchTelemetryInput(
    val batch: List<TelemetryInput>
)

data class TelemetryStatusResponse(
    @SerializedName("total_records") val totalRecords: Int,
    @SerializedName("last_telemetry_at") val lastTelemetryAt: String?,
    @SerializedName("modalities_collected") val modalitiesCollected: List<String>
)

// --- Baseline & Staging ---
data class BaselineProgressResponse(
    @SerializedName("baseline_status") val baselineStatus: String,
    @SerializedName("completion_percent") val completionPercent: Float,
    @SerializedName("days_collected") val daysCollected: Int,
    @SerializedName("days_required") val daysRequired: Int,
    @SerializedName("modalities_status") val modalitiesStatus: Map<String, String>
)

data class ContributorFactor(
    val feature: String,
    val modality: String,
    val direction: String,
    @SerializedName("z_score") val zScore: Float,
    @SerializedName("human_explanation") val humanExplanation: String
)

data class ModalityBreakdown(
    val keyboard: Float,
    val usage: Float,
    val motion: Float,
    val work: Float,
    val mobility: Float
)

data class RiskAssessmentResponse(
    @SerializedName("risk_score") val riskScore: Float,
    val stage: Int,
    @SerializedName("stage_label") val stageLabel: String,
    val confidence: Float,
    val trend: String,
    @SerializedName("persistence_days") val persistenceDays: Int,
    @SerializedName("top_contributors") val topContributors: List<ContributorFactor>,
    @SerializedName("modality_scores") val modalityScores: ModalityBreakdown,
    val timestamp: String
)

// --- Interventions & Self Check ---
data class SelfReportInput(
    val mood: String,
    @SerializedName("stress_level") val stressLevel: Int,
    val note: String? = null
)

data class InterventionStartInput(
    @SerializedName("activity_type") val activityType: String
)

data class InterventionCompleteInput(
    @SerializedName("session_id") val sessionId: Int? = null,
    @SerializedName("feedback_score") val feedbackScore: Int? = null,
    @SerializedName("result_metrics") val resultMetrics: Map<String, Any>? = null
)

// --- Aggregated Dashboard Response ---
data class DashboardResponse(
    val user: UserResponse,
    val consent: ConsentResponse,
    @SerializedName("baseline_progress") val baselineProgress: BaselineProgressResponse,
    @SerializedName("current_risk") val currentRisk: RiskAssessmentResponse,
    @SerializedName("recommended_interventions") val recommendedInterventions: List<Map<String, Any>>
)

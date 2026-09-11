# BehavioralWell Real-Time App Telemetry Report

**Export Timestamp:** `2026-09-08T09:20:45.795288Z`
**Database:** `behavioral_well.db`

## Database Table Summary

| Table Name | Record Count |
|---|---|
| `users` | 1 |
| `consents` | 2 |
| `behavioral_telemetry` | 19 |
| `user_baselines` | 18 |
| `risk_assessments` | 19 |
| `intervention_sessions` | 7 |
| `self_reports` | 6 |

### Table: `users` (1 records)

| id | name | email | hashed_password | age_group | timezone | occupation_category | created_at | baseline_completed |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| usr_4e5675825c | Alex Morgan | demo@behavioralwell.ai | a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea | 25-34 | UTC | Software Engineer | 2026-09-07 09:15:28.880791 | 1 |

### Table: `consents` (2 records)

| id | user_id | keyboard_enabled | usage_enabled | motion_enabled | work_enabled | mobility_enabled | created_at | updated_at |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | usr_4e5675825c | 1 | 1 | 1 | 1 | 1 | 2026-09-07 09:15:28.888293 | 2026-09-07 09:15:28.888298 |
| 2 | usr_demo12345 | 1 | 1 | 0 | 1 | 0 | 2026-09-07 16:07:46.145174 | 2026-09-07 16:08:41.892991 |

### Table: `behavioral_telemetry` (19 records)

| id | user_id | timestamp | typing_speed | key_press_duration | pause_duration | correction_rate | screen_time | unlock_count | night_usage | app_switch_frequency | movement_intensity | acceleration_variance | stationary_duration | task_accuracy | task_completion_time | task_error_rate | speed_variance | route_variability | raw_features_json |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 10 | usr_4e5675825c | 2026-09-07 09:15:29.230827 | 62.0 | 115.0 | 0.35 | 4.2 | 4.1 | 42 | 0.2 | 12.0 | 1.02 | 0.08 | 4.5 | 94.5 | 18.2 | 5.5 | 0.12 | 0.05 | None |
| 11 | usr_4e5675825c | 2026-09-07 09:15:29.276128 | 62.0 | 115.0 | 0.35 | 4.2 | 4.1 | 42 | 0.2 | 12.0 | 1.02 | 0.08 | 4.5 | 94.5 | 18.2 | 5.5 | 0.12 | 0.05 | None |
| 12 | usr_4e5675825c | 2026-09-07 09:15:29.317976 | 62.0 | 115.0 | 0.35 | 4.2 | 4.1 | 42 | 0.2 | 12.0 | 1.02 | 0.08 | 4.5 | 94.5 | 18.2 | 5.5 | 0.12 | 0.05 | None |
| 13 | usr_4e5675825c | 2026-09-07 09:15:29.357334 | 62.0 | 115.0 | 0.35 | 4.2 | 4.1 | 42 | 0.2 | 12.0 | 1.02 | 0.08 | 4.5 | 94.5 | 18.2 | 5.5 | 0.12 | 0.05 | None |
| 14 | usr_4e5675825c | 2026-09-07 09:15:29.396809 | 62.0 | 115.0 | 0.35 | 4.2 | 4.1 | 42 | 0.2 | 12.0 | 1.02 | 0.08 | 4.5 | 94.5 | 18.2 | 5.5 | 0.12 | 0.05 | None |
| 15 | usr_demo12345 | 2026-09-07 16:07:46.159397 | 52.5 | 115.0 | 0.22 | 0.04 | 4.2 | 35 | 0.5 | 8.1 | 0.35 | 0.08 | 14.5 | None | None | None | None | None | {'idempotency_key': 'test_evt_1001'} |
| 16 | usr_demo12345 | 2026-09-07 16:07:46.190723 | None | None | None | None | 5.1 | 42 | None | None | None | None | None | None | None | None | None | None | {'idempotency_key': 'test_evt_1002'} |
| 17 | usr_demo12345 | 2026-09-07 16:08:11.863998 | 52.5 | 115.0 | 0.22 | 0.04 | 4.2 | 35 | 0.5 | 8.1 | 0.35 | 0.08 | 14.5 | None | None | None | None | None | {'idempotency_key': 'test_evt_1001'} |
| 18 | usr_demo12345 | 2026-09-07 16:08:41.815909 | 52.5 | 115.0 | 0.22 | 0.04 | 4.2 | 35 | 0.5 | 8.1 | 0.35 | 0.08 | 14.5 | None | None | None | None | None | {'idempotency_key': 'test_evt_1788797321_A'} |
| 19 | usr_demo12345 | 2026-09-07 16:08:41.848296 | None | None | None | None | 5.1 | 42 | None | None | None | None | None | None | None | None | None | None | {'idempotency_key': 'test_evt_1788797321_B'} |

### Table: `user_baselines` (18 records)

| id | user_id | feature_name | mean | std | min_val | max_val | samples_count | updated_at |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 9 | usr_4e5675825c | movement_intensity | 1.0199999999999998 | 1.0 | 1.02 | 1.02 | 14 | 2026-09-07 09:15:29.013151 |
| 10 | usr_4e5675825c | acceleration_variance | 0.07999999999999999 | 1.0 | 0.08 | 0.08 | 14 | 2026-09-07 09:15:29.013154 |
| 11 | usr_4e5675825c | stationary_duration | 4.5 | 1.0 | 4.5 | 4.5 | 14 | 2026-09-07 09:15:29.013158 |
| 12 | usr_4e5675825c | task_accuracy | 94.5 | 9.450000000000001 | 94.5 | 94.5 | 14 | 2026-09-07 09:15:29.013162 |
| 13 | usr_4e5675825c | task_completion_time | 18.199999999999996 | 1.8199999999999996 | 18.2 | 18.2 | 14 | 2026-09-07 09:15:29.013166 |
| 14 | usr_4e5675825c | task_error_rate | 5.5 | 1.0 | 5.5 | 5.5 | 14 | 2026-09-07 09:15:29.013169 |
| 15 | usr_4e5675825c | speed_variance | 0.12000000000000004 | 1.0 | 0.12 | 0.12 | 14 | 2026-09-07 09:15:29.013173 |
| 16 | usr_4e5675825c | route_variability | 0.05000000000000001 | 1.0 | 0.05 | 0.05 | 14 | 2026-09-07 09:15:29.013176 |
| 17 | usr_demo12345 | screen_time | 4.5600000000000005 | 0.4409081537009718 | 4.2 | 5.1 | 5 | 2026-09-07 16:08:41.860385 |
| 18 | usr_demo12345 | unlock_count | 37.8 | 3.4292856398964497 | 35.0 | 42.0 | 5 | 2026-09-07 16:08:41.860391 |

### Table: `risk_assessments` (19 records)

| id | user_id | timestamp | risk_score | stage | stage_label | confidence | trend | persistence_days | top_contributors | modality_scores |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 10 | usr_4e5675825c | 2026-09-07 09:15:29.266728 | 0.0 | 0 | Stage 0 — Stable | 0.9 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 11 | usr_4e5675825c | 2026-09-07 09:15:29.307362 | 0.0 | 0 | Stage 0 — Stable | 0.9 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 12 | usr_4e5675825c | 2026-09-07 09:15:29.348266 | 0.0 | 0 | Stage 0 — Stable | 0.9 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 13 | usr_4e5675825c | 2026-09-07 09:15:29.388710 | 0.0 | 0 | Stage 0 — Stable | 0.9 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 14 | usr_4e5675825c | 2026-09-07 09:15:29.428589 | 0.0 | 0 | Stage 0 — Stable | 0.9 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 15 | usr_demo12345 | 2026-09-07 16:07:46.174042 | 0.0 | 0 | Stage 0 — Stable | 0.95 | stable | 1 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 16 | usr_demo12345 | 2026-09-07 16:07:46.198435 | 0.0 | 0 | Stage 0 — Stable | 0.95 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 17 | usr_demo12345 | 2026-09-07 16:08:11.884125 | 0.0 | 0 | Stage 0 — Stable | 0.95 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 18 | usr_demo12345 | 2026-09-07 16:08:41.831848 | 0.0 | 0 | Stage 0 — Stable | 0.95 | stable | 0 | [] | {'keyboard': 0.0, 'usage': 0.0, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |
| 19 | usr_demo12345 | 2026-09-07 16:08:41.868669 | 36.7 | 1 | Stage 1 — Early Deviation | 0.9 | stable | 1 | [{'feature': 'unlock_count', 'modality': 'usage', 'direction': 'elevated', 'z_score': 1.22, 'human_explanation': 'Phone unlock frequency is elevated by 1.2 std-dev compared to your personal baseline.'}, {'feature': 'screen_time', 'modality': 'usage', 'direction': 'elevated', 'z_score': 1.22, 'human_explanation': 'Daily active screen time is elevated by 1.2 std-dev compared to your personal baseline.'}] | {'keyboard': 0.0, 'usage': 36.7, 'motion': 0.0, 'work': 0.0, 'mobility': 0.0} |

### Table: `intervention_sessions` (7 records)

| id | user_id | activity_type | started_at | completed_at | completed | feedback_score | result_metrics |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | usr_4e5675825c | Breathing Reset | 2026-09-07 15:32:14.691634 | None | 0 | None | None |
| 2 | usr_4e5675825c | Breathing Reset | 2026-09-07 15:32:37.738744 | 2026-09-07 15:32:37.757034 | 1 | 5 | {'heart_rate_bpm': 68} |
| 3 | usr_4e5675825c | Breathing Reset | 2026-09-07 15:33:09.507205 | 2026-09-07 15:33:09.525679 | 1 | 5 | {'heart_rate_bpm': 68} |
| 4 | usr_4e5675825c | Breathing Reset | 2026-09-07 15:35:53.433642 | None | 0 | None | None |
| 5 | usr_4e5675825c | Reaction Challenge | 2026-09-07 15:36:23.187510 | None | 0 | None | None |
| 6 | usr_4e5675825c | Reaction Challenge | 2026-09-07 15:45:04.322399 | None | 0 | None | None |
| 7 | usr_4e5675825c | Breathing Reset | 2026-09-07 15:45:12.909886 | None | 0 | None | None |

### Table: `self_reports` (6 records)

| id | user_id | timestamp | mood | stress_level | note |
| --- | --- | --- | --- | --- | --- |
| 1 | usr_4e5675825c | 2026-09-07 15:32:37.772158 | Calm | 2 | Feeling rested after breathing reset |
| 2 | usr_4e5675825c | 2026-09-07 15:33:09.540371 | Calm | 2 | Feeling rested after breathing reset |
| 3 | usr_4e5675825c | 2026-09-07 15:35:25.673579 | Anxious | 4 | NNNNNN |
| 4 | usr_4e5675825c | 2026-09-07 15:45:37.647795 | Anxious | 5 | None |
| 5 | usr_4e5675825c | 2026-09-08 09:15:06.680058 | Anxious | 5 | knvvojenvl d |
| 6 | usr_4e5675825c | 2026-09-08 09:20:33.003441 | Great | 1 | kjbuendv |


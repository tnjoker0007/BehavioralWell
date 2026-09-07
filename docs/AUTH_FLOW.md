# BehavioralWell — Authentication & Token Security Architecture

This document describes the shared authentication mechanism used by both **Web (React)** and **Android Native (Kotlin + Compose)** clients.

---

## 1. Authentication Architecture

```text
               ANDROID APP                              WEB DASHBOARD
              (Kotlin/Compose)                          (React/Vite)
                     │                                       │
                     └───────────────────┬───────────────────┘
                                         │
                                   HTTP POST /api/auth/login
                                         │
                                         ▼
                               FASTAPI AUTH SERVICE
                                         │
                                 Verify Password Hash
                                 (SHA-256 / bcrypt)
                                         │
                                         ▼
                               Issuance of JWT Pair:
                        - access_token (Expires: 7 Days)
                        - refresh_token (Expires: 30 Days)
```

---

## 2. Token Specification

- **Algorithm:** HS256
- **Header:** `Authorization: Bearer <access_token>`
- **Access Token Payload:**
  ```json
  {
    "sub": "usr_demo12345",
    "email": "demo@behavioralwell.ai",
    "exp": 1789230910,
    "type": "access"
  }
  ```
- **Refresh Token Payload:**
  ```json
  {
    "sub": "usr_demo12345",
    "exp": 1791230910,
    "type": "refresh"
  }
  ```

---

## 3. Secure Token Storage Guidelines

- **Android Client:** Store `access_token` and `refresh_token` in `EncryptedSharedPreferences` backed by **Android Keystore**. Never store tokens in plain `SharedPreferences`.
- **Web Client:** Store tokens in `localStorage` or `httpOnly` secure cookies.
- **Password Security:** Passwords are NEVER stored in plaintext. Passwords must be hashed using SHA-256 / bcrypt before DB insertion.

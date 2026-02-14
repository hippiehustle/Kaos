# SecureScanner

AI-powered NSFW content detection and file organization tool by **Kaos Forge**.

## Overview

SecureScanner detects and manages NSFW content in your files using advanced AI analysis. Upload files, scan directories, organize flagged content, and perform OSINT username lookups — all from a single application.

## Features

- **Smart Detection** — InceptionV3-based NSFW classification (~93% accuracy) with optional SentiSight.ai cloud detection
- **File Organization** — Automatically sort flagged content by category, date, or file type
- **OSINT Username Search** — Check usernames across 3,000+ sites (powered by Maigret database) with OSINTIndustries API integration
- **Detailed Reports** — Risk assessments, export capabilities, and scan statistics
- **Cross-Platform** — Web app, native Android (Kotlin/Compose), and Electron desktop

## Tech Stack

### Web App
| Layer | Technology |
|-------|------------|
| Frontend | React 18, Tailwind CSS, Radix UI, TanStack Query |
| Backend | Express.js, PostgreSQL, Drizzle ORM |
| Detection | TensorFlow.js + NSFWJS (InceptionV3) |
| Bundler | Vite |

### Android App
| Layer | Technology |
|-------|------------|
| UI | Kotlin, Jetpack Compose, Material 3 |
| DI | Hilt |
| Networking | Retrofit, OkHttp, Kotlinx Serialization |
| Storage | DataStore Preferences |
| Navigation | Navigation Compose |
| Min SDK | 26 (Android 8.0) |

## Getting Started

### Web App

```bash
# Install dependencies
npm install

# Push database schema
npm run db:push

# Start development server
npm run dev
```

The server runs on `http://localhost:5000`.

### Android App

1. Open `android/` in Android Studio
2. Sync Gradle
3. Set the server URL in Settings
4. Build and run

Or build from command line:

```bash
cd android
./gradlew assembleDebug
```

APK output: `android/app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

```
.
├── client/             # React frontend
│   └── src/
│       ├── pages/      # 13 page components
│       ├── components/ # UI components
│       ├── hooks/      # React hooks
│       └── lib/        # Utilities
├── server/             # Express.js backend
│   ├── routes.ts       # API endpoints
│   ├── storage.ts      # Database operations
│   └── nsfw-model.ts   # TensorFlow detection
├── shared/             # Shared schemas
├── android/            # Native Android app
│   └── app/src/main/java/com/securescanner/app/
│       ├── data/       # Models, API, DataStore, DI
│       ├── navigation/ # NavGraph, Screen routes
│       └── ui/         # Screens, components, theme
└── electron/           # Electron desktop app
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stats` | Scan statistics |
| POST | `/api/upload` | Upload files for scanning |
| POST | `/api/scan-sessions` | Create scan session |
| GET | `/api/scan-sessions` | List all sessions |
| GET | `/api/scan-results` | All scan results |
| GET | `/api/nsfw-results` | NSFW-only results |
| POST | `/api/organize-custom` | Organize files |
| GET | `/api/export/report` | Export JSON report |
| DELETE | `/api/scan-history` | Clear history |

## Configuration

- **Server URL** — Configurable in the Android app's Settings screen
- **SentiSight.ai** — Toggle cloud detection in the Admin panel
- **Admin Panel** — Unlock by tapping the shield icon 7 times on the About screen

## License

MIT

# Architecture

Reply is a Compose Multiplatform app (Android, iOS, Desktop) organised as a small set of Gradle
modules with a strict dependency direction: **apps → shared → features → core**.

```
androidApp / desktopApp / iosApp        platform entry points, DB + DataStore builders
        │
      :shared                            App, ReplyApp (shell), ReplyNavigator, ReplyAppState, Metro AppGraph
        │
:feature:home  :feature:email  :feature:compose  :feature:search  :feature:nav
        │
:core:designsystem                       theme, components, motion, cutout shapes, resources (fonts/icons)
:core:data                               models, stores, repository, Ktor API, image resolver, settings
:core:database  :core:datastore          Room (KMP) and Preferences DataStore with per-platform builders
```

## Layers

### Data (`:core:data`)
- **Single source of truth is Room.** `EmailStore` / `AccountStore` expose `StateFlow`s built from DAO
  `Flow`s (`combine` → `stateIn`), and write mutations back through DAOs. Nothing in the UI holds
  mail state of its own.
- **`ReplyRepository`** owns loading:
  1. `loadBundled()` seeds an empty database from the JSON packaged with the app (instant offline start),
  2. `refresh()` fetches the same JSON from GitHub through Ktor and inserts with `INSERT OR IGNORE`
     (user changes — stars, trash, active account — are never overwritten),
  3. `refreshIfStale()` throttles to once per minute (used on lifecycle resume).
  It publishes `SyncStatus` (`Idle / Syncing / Synced(at) / Failed(message, lastSync)`) and writes the last
  successful sync time to DataStore.
- **`ImageResolver`** turns image file names into Sketch URIs: Compose-resource URIs while running on bundled
  data, GitHub raw URLs after a successful sync, with the bundled drawable as `error()` fallback.
- **`SettingsRepository`** wraps Preferences DataStore (`themeMode`, `lastSyncEpochMillis`) as `Flow`s.
- **Injectable seams**: `ReplyApi` (interface; `KtorReplyApi` in production, fake in tests),
  `DispatcherProvider`, `kotlin.time.Clock`. Kermit is used for structured logging.

### Features (`:feature:*`)
- Screens are **stateless composables** taking state + lambdas (`HomeScreen(emails, onStarChanged, …)`),
  so they preview/test without ViewModels. Small route composables in `:shared` own the ViewModels.
- ViewModels expose `StateFlow`s via `stateIn(WhileSubscribed(5s), initialSnapshot)` — a synchronous
  first value avoids empty first frames; subscriptions stop when the screen leaves composition.
- `SearchViewModel` is a `Flow` pipeline: `query → map(trim) → debounce(250ms) → distinctUntilChanged →
  flatMapLatest(filter mail store)`.
- Gesture/motion state lives in `@Stable` holders (`ReboundingSwipeState`, `BottomNavDrawerState`,
  `ComposeDraft`) that are plain classes with unit tests.

### Shell (`:shared`)
- **`ReplyNavigator`** owns the Nav3 back stack (`rememberNavBackStack` — restored across process death via a
  polymorphic `NavKey` serializers module), pop detection, and container-transform state.
- **`ReplyAppState`** holds bar/FAB visibility, snackbar host, transform/exit animatables and the
  cross-cutting actions (undoable move-to-trash, retry sync, open compose).
- Screens are switched with a plain `AnimatedContent` (Nav3's `NavDisplay` cut push transitions short and
  its `contentKey` is a `String`); Material motion patterns are declared once in `MaterialMotion`.
- **Container transforms** (card → detail, FAB → compose, chip → recipient card) run in an overlay that
  draws its own copy of the start/end content while the real destination is invisible; the geometry is
  a pure function (`containerFrame`) with tests.

### Design system (`:core:designsystem`)
- `ReplyTheme` carries the Material 2 colour roles the sample uses (`primarySurface`, emphasis alphas,
  elevation overlays) and bridges a subset into Material 3 for stock components.
- Custom components mirror the original widgets 1:1 (cradled bottom app bar, animated-corner card,
  cutout drawer sheet, AVD-style FAB icon). Every emitting composable takes `modifier` first among optionals.

## Cross-cutting engineering practices
| Concern | Where |
|---|---|
| Static analysis | detekt + `io.nlopez` Compose rules (`config/detekt/detekt.yml`), `./gradlew detekt` — zero issues |
| Tests | Unit: motion math, swipe state, drawer physics, repository (fake API / fixed clock / offline), search debounce.  UI: headless screenshot + 16ms motion frames (`ScreenshotTest`) |
| CI | GitHub Actions: detekt → unit tests → assemble Android/Desktop; Dependabot for Gradle + Actions |
| Persistence | Room (schema exported), Preferences DataStore |
| Networking | Ktor 3 (OkHttp / Darwin engines), JSON via kotlinx.serialization, `text/plain` tolerant |
| Images | Sketch 4 (async decode, downsampling, memory/disk cache) |
| DI | Metro compile-time graph; platform inputs (`ReplyDatabase`, `DataStore`) via graph factory |
| Reversible actions | Move-to-trash / archive with snackbar Undo |
| Lifecycle | `LifecycleResumeEffect` → throttled refresh; `collectAsStateWithLifecycle` everywhere |
| Offline-first | Bundled seed → Room → remote top-up; failure keeps local data and surfaces status |

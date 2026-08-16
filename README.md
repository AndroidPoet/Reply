<h1 align="center">Reply</h1>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat"/></a>
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop-brightgreen.svg"/> <br>
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.0-blueviolet.svg?logo=kotlin"/>
  <img alt="Ktor" src="https://img.shields.io/badge/Ktor-3.5-blue.svg"/>
  <img alt="Room" src="https://img.shields.io/badge/Room-2.8%20KMP-blue.svg"/>
  <img alt="Sketch" src="https://img.shields.io/badge/Images-Sketch%204-blue.svg"/>
  <img alt="Compose Multiplatform" src="https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-blue.svg"/>
  <img alt="Navigation 3" src="https://img.shields.io/badge/Navigation-3-blue.svg"/>
  <img alt="DI" src="https://img.shields.io/badge/DI-Metro-orange.svg"/>
</p>

<p align="center">
📬 Reply is the <a href="https://material.io/design/material-studies/reply.html">Material Study</a> email app,
rebuilt from the ground up in Compose Multiplatform — running on Android, iOS and Desktop (JVM) from a
single multi-module Kotlin codebase. Every screen and component of the original
<a href="https://github.com/material-components/material-components-android-examples/tree/develop/Reply">MDC-Android Reply</a>
sample is ported 1:1 (dimensions, type scale, colour roles, shapes and motion), with the View-based
widgets replaced by hand-built Compose components.
</p>

## Screenshots

| Home | Navigation drawer | Account picker | Swipe to star |
|:---:|:---:|:---:|:---:|
| <img src="art/home_light.png" width="220"/> | <img src="art/drawer_light.png" width="220"/> | <img src="art/sandwich_light.png" width="220"/> | <img src="art/swipe_star.png" width="220"/> |

| Email (dark) | Compose (dark) | Search |
|:---:|:---:|:---:|
| <img src="art/email_dark.png" width="220"/> | <img src="art/compose_dark.png" width="220"/> | <img src="art/search_light.png" width="220"/> |

## Motion

Every transition of the Views app is reproduced with the same curves and timings
(`fast_out_slow_in`, 300 / 225 / 175ms, MDC's default container-transform thresholds,
`ViewDragHelper`'s quintic settle, `ItemTouchHelper`'s 250ms recover):

| Card → detail (`MaterialContainerTransform` + `MaterialElevationScale`) | FAB → compose (container transform, `Slide` return) | Drawer + account sandwich (`BottomSheetBehavior` physics) |
|:---:|:---:|:---:|
| <img src="art/motion_card_to_detail.gif" width="220"/> | <img src="art/motion_fab_to_compose.gif" width="220"/> | <img src="art/motion_drawer.gif" width="220"/> |

| Swipe to star (rebounding swipe, circular reveal) | Search (`MaterialSharedAxis` Z) | Mailbox switch (`MaterialFadeThrough`) |
|:---:|:---:|:---:|
| <img src="art/motion_swipe_star.gif" width="220"/> | <img src="art/motion_search.gif" width="220"/> | <img src="art/motion_mailbox_switch.gif" width="220"/> |

Motion is implemented in `:core:designsystem` → `motion/`: `MaterialMotion` (elevation scale, fade through,
shared axis Z, slide), `ContainerTransform` (a port of `MaterialContainerTransform`'s fit-mode /
fade-mode / threshold model), `Interpolators` (the platform interpolators), and `EditReplyIcon`
(the `avd_edit_to_reply` AnimatedVectorDrawable re-drawn on a Canvas).

## Running

- **Android** — `./gradlew :androidApp:assembleDebug`, or run the `androidApp` configuration in Android Studio.
- **Desktop** — `./gradlew :desktopApp:run`
- **iOS** — open `iosApp/iosApp.xcodeproj` in Xcode, set your signing **TEAM_ID** in
  `iosApp/Configuration/Config.xcconfig`, pick a simulator, and run.
- **Screenshots / motion frames** — `./gradlew :desktopApp:test --tests "*ScreenshotTest*"` renders every
  screen in both themes plus 16ms frames of each transition to `desktopApp/build/screenshots/`.

## What's ported

| Original (Views / MDC) | Compose Multiplatform |
|---|---|
| `Theme.Reply` / `Theme.Reply.Dark`, `TextAppearance.Reply.*` (Work Sans), `ShapeAppearance.Reply.*` | `ReplyTheme`, `ReplyColors`, `ReplyTypography`, `ReplyShapes` (`:core:designsystem`) |
| `BottomAppBar` with FAB cradle (`fabCradleMargin` 8dp, `fabCradleRoundedCornerRadius` 32dp) + `hideOnScroll` | `ReplyBottomAppBar` + `CutoutTopEdgeShape` (port of `BottomAppBarTopEdgeTreatment`), scroll-driven hide |
| `FloatingActionButton` with `asl_edit_reply` state list, `fab_show`/`fab_hide` animators | `ReplyFab` + `EditReplyIcon` (AVD morph), 175ms scale/opacity show-hide |
| `MaterialCardView` whose top-left corner morphs to 24dp when starred | `ReplyCard(topLeftCorner)` |
| `ReboundingSwipeActionCallback` + `EmailSwipeActionDrawable` (spring swipe, circular reveal, star bounce) | `Modifier.reboundingSwipe` + `EmailListItem` draw-behind |
| `BottomNavDrawerFragment`: `BottomSheetBehavior` (hideable, skipCollapsed, half ratio 0.6), `SemiCircleEdgeCutoutTreatment`, account "sandwich" | `BottomNavDrawer` + `BottomNavDrawerState` (anchored drag, nested scroll, sandwich animation) |
| `MenuBottomSheetDialogFragment` | `MenuBottomSheet` |
| `HomeFragment`, `EmailFragment` (masonry attachment grid), `ComposeFragment` (sender spinner, avatar chips, expanding recipient card), `SearchFragment` | `:feature:home`, `:feature:email`, `:feature:compose`, `:feature:search` |
| `MaterialContainerTransform` (card → detail, FAB → compose, chip → recipient card) | `ContainerTransform` overlay (`:core:designsystem/motion`) |
| `MaterialElevationScale`, `MaterialFadeThrough`, `MaterialSharedAxis(Z)`, `Slide` | `MaterialMotion` enter/exit transitions on the Nav3 back stack |
| `avd_edit_to_reply` FAB icon morph | `EditReplyIcon` (Canvas-drawn AVD timeline) |
| Dark-theme menu (Light / Dark / System default) | `ThemeMode` in `App` |

## Tech stack

- Kotlin Multiplatform, Compose Multiplatform (Material 3 primitives, custom Material 2–styled components).
- [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) — type-safe `NavKey` back stack, `rememberDecoratedNavEntries` (saveable state + entry-scoped ViewModels); screens are switched by an `AnimatedContent` that carries the Material transitions.
- Lifecycle & ViewModel — `org.jetbrains.androidx.lifecycle`.
- [Metro](https://github.com/ZacSweers/metro) — compile-time DI (`@Inject`, `@AssistedInject`, `@DependencyGraph`).
- [Sketch](https://github.com/panpf/sketch) — image loading (memory/disk cache, async decode, downsampling): photos and avatars are
  Compose-resource URIs when the bundled data is shown and GitHub raw URLs once the remote data arrives, with the bundled
  drawable as `error()` fallback.
- Compose Resources — Work Sans fonts, vector icons, and the bundled copies of the photos.
- [Room KMP](https://developer.android.com/kotlin/multiplatform/room) (`:core:database`, bundled SQLite driver, KSP) — the local
  source of truth: seeded from the packaged JSON on first launch, topped up from GitHub afterwards (`INSERT OR IGNORE`, so
  stars / trash / active account survive restarts and refreshes); stores observe DAO `Flow`s.
- [DataStore](https://developer.android.com/kotlin/multiplatform/datastore) (`:core:datastore`) — theme mode and last-sync time as `Flow`s.
- Sync status (`SyncStatus` Idle/Syncing/Synced/Failed) drives a 2dp progress line, a Retry snackbar when offline, and a
  "Synced 2 min ago" caption in the drawer (ticking `produceState` clock).
- Flows: DAO flows → `combine` → `stateIn(WhileSubscribed)` in ViewModels; search is `debounce` + `distinctUntilChanged` +
  `flatMapLatest` over the mail store with live results.
- [Ktor](https://ktor.io) client + kotlinx.serialization — the sample data lives as JSON in this repo
  (`core/data/src/commonMain/composeResources/files/*.json`), bundled with the app for an instant offline start and
  refreshed from GitHub raw on launch (`ReplyRepository`). `EmailStore` / `AccountStore` are in-memory `StateFlow`s.

## Architecture

```
:shared               # App shell: App, ReplyApp, ReplyNavigator (Nav3 back stack + Material transitions), TransformOverlay, ReplyBottomBar, Metro AppGraph
:core:datastore       # Preferences DataStore (per-platform file locations)
:core:database        # Room entities/DAOs/database + per-platform builders (Android / desktop file / iOS documents, in-memory for tests)
:core:data            # Models, EmailStore/AccountStore over Room, ReplyRepository (bundled JSON seed + Ktor refresh), ImageResolver
:core:designsystem    # ReplyTheme, palette/type/shape/motion, custom components, cutout shape, fonts/icons
:feature:home         # Mailbox list, swipe-to-star, long-press menu
:feature:email        # Email detail with attachment grid
:feature:compose      # Compose/reply screen
:feature:search       # Search suggestions
:feature:nav          # Bottom navigation drawer + account picker
:androidApp / :desktopApp / iosApp
```

## Credits

Design and assets from the [Material Studies — Reply](https://material.io/design/material-studies/reply.html)
and the [material-components-android-examples](https://github.com/material-components/material-components-android-examples)
repository (Apache 2.0). Work Sans by Wei Huang (OFL).

## License

```
Copyright 2026 AndroidPoet

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

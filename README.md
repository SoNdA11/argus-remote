# Argus Remote

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-9%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.10-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Expressive-757575?logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Kotlin Coroutines](https://img.shields.io/badge/Coroutines-1.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/docs/coroutines-overview.html)
[![DataStore](https://img.shields.io/badge/DataStore-Preferences-3DDC84?logo=android&logoColor=white)](https://developer.android.com/topic/libraries/architecture/datastore)
[![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![JUnit](https://img.shields.io/badge/JUnit-4.13-25A162?logo=junit5&logoColor=white)](https://junit.org)
[![Bluetooth HID](https://img.shields.io/badge/Bluetooth-HID%20Keyboard-0082FC?logo=bluetooth&logoColor=white)](https://www.bluetooth.com)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue)](LICENSE)

Turn an Android phone into a Bluetooth keyboard for presentations.

Argus Remote registers the phone as a standard Bluetooth HID keyboard. Pair it
with a computer once and the phone's buttons become real key presses on the
host, so any focused application receives them: Reveal.js, Slidev, Marp,
PowerPoint, Google Slides, LibreOffice Impress, or any viewer that navigates
with arrow keys.

There is no companion app, no account, no network traffic, and no cloud
service. The phone talks to the computer only over Bluetooth.

## Features

- **Real keyboard input.** A boot-protocol HID descriptor, so the host sees a
  keyboard and not a custom device. Works with any operating system Bluetooth
  stack.
- **Large, glove-friendly controls.** Tap for next/previous, hold to skip a
  whole slide.
- **Presentation timer.** Elapsed, target and remaining time, with an editable
  target (default 17:55, range 01:00–180:00) persisted on the device.
- **Deck shortcuts.** Black screen, fullscreen, overview and theme toggle.
- **Screen and feedback.** Keep-screen-on while the remote is open, and haptic
  confirmation on every delivered key.
- **Offline.** No internet permission is requested or used.

## Tech stack

| Layer | Technology |
| --- | --- |
| Language | ![Kotlin](https://img.shields.io/badge/-Kotlin-7F52FF?logo=kotlin&logoColor=white) |
| Platform | ![Android](https://img.shields.io/badge/-Android%209%2B-3DDC84?logo=android&logoColor=white) |
| UI | ![Compose](https://img.shields.io/badge/-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white) ![Material 3](https://img.shields.io/badge/-Material%203-757575?logo=materialdesign&logoColor=white) |
| Async | ![Coroutines](https://img.shields.io/badge/-Coroutines-7F52FF?logo=kotlin&logoColor=white) ![Flow](https://img.shields.io/badge/-Flow-7F52FF?logo=kotlin&logoColor=white) |
| Storage | ![DataStore](https://img.shields.io/badge/-DataStore-3DDC84?logo=android&logoColor=white) |
| Transport | ![Bluetooth](https://img.shields.io/badge/-Bluetooth%20HID-0082FC?logo=bluetooth&logoColor=white) |
| Build | ![Gradle](https://img.shields.io/badge/-Gradle-02303A?logo=gradle&logoColor=white) ![AGP](https://img.shields.io/badge/-AGP%208.13-3DDC84?logo=android&logoColor=white) |
| Tests | ![JUnit](https://img.shields.io/badge/-JUnit%204-25A162?logo=junit5&logoColor=white) ![Lint](https://img.shields.io/badge/-Android%20Lint-3DDC84?logo=android&logoColor=white) |

## Architecture

The project follows ports and adapters (hexagonal) split into two Gradle
modules.

```
core/                        Pure Kotlin, no Android dependencies
  domain/model               KeyStroke, SlideCommand, Host, KeyboardStatus, ...
  domain/port                KeyboardPort, SettingsPort, ClockPort, HapticsPort
  domain/service             Key map shared by every command
  application                PresentationTimer, SendSlideCommand

app/                         Android adapters and UI
  adapter/hid                HID keyboard adapter, descriptor and reports
  adapter/settings           DataStore settings
  adapter/time               Monotonic clock
  adapter/haptics            Vibration feedback
  adapter/ui                 Compose screens and ViewModel
  AppContainer               Manual composition root
```

Rules that keep the boundary clean:

- `core` contains no `android.*` import. The domain decides *what* to send; the
  app decides *how*.
- Adapters implement core interfaces. `AppContainer` is the only place that
  constructs concrete adapters.
- The timer depends on `ClockPort`, so it is tested with a fake clock.
- `SendSlideCommand` is the single path from a UI action to a HID report, which
  keeps haptics and key mapping consistent.

## Requirements

- JDK 21
- Android SDK with platform 36 and build-tools 36
- A phone with the Bluetooth HID device profile (every phone since Android 9
  ships it)

Set the SDK location in `local.properties`:

```properties
sdk.dir=/path/to/Android/Sdk
```

## Build and install

```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew test lintDebug assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The verification gate is:

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew test lintDebug assembleDebug
```

Tests cover the key map, the HID report encoding and descriptor, the command
use case, and the timer state machine. The APK is a debug build and is not
signed for distribution.

## Using the app

1. Open the app and grant the Bluetooth permissions.
2. Turn Bluetooth on.
3. Tap **Enable keyboard mode**. The phone now advertises the HID keyboard
   service.
4. Tap **Make phone discoverable** and pair from the computer within 120
   seconds (see below).
5. Select the paired computer and tap **Connect**.
6. Tap **Open remote**.

Keep the presentation focused on the computer. Press **Start** on the timer when
the talk begins; edit the target in **Settings** (any value from `01:00` to
`180:00`, persisted on the device).

## Pairing from Linux (BlueZ)

```bash
bluetoothctl
[bluetooth]# power on
[bluetooth]# agent on
[bluetooth]# default-agent
[bluetooth]# scan on
```

Wait for the phone to appear (its Bluetooth name), then:

```bash
[bluetooth]# pair AA:BB:CC:DD:EE:FF
[bluetooth]# trust AA:BB:CC:DD:EE:FF
[bluetooth]# connect AA:BB:CC:DD:EE:FF
[bluetooth]# scan off
```

The phone must be in discoverable mode during the scan. If the computer was
paired to the phone before keyboard mode existed, remove the pairing on both
sides and pair again — otherwise the host keeps the old device class and no
keyboard input arrives.

To confirm the host sees a keyboard:

```bash
bluetoothctl info AA:BB:CC:DD:EE:FF    # Connected: yes, UUID: Human Interface Device
grep -A6 "Uniq=<phone-bluetooth-mac>" /proc/bus/input/devices
```

The phone keeps its `phone` device class, so the icon stays `phone` even when
the keyboard works. The proof is a new input device named after the phone with
`Bus=0005` and handlers `sysrq kbd eventN`.

These are the exact steps validated on Arch Linux with BlueZ 5.87
(kernel HIDP + uhid): after pairing, the kernel creates the keyboard and every
remote button arrives with the expected keysym and modifier.

## Pairing from Windows (10 / 11)

Windows has no `bluetoothctl`: pairing is done through the Settings UI, and the
phone is accepted as a keyboard automatically once the HID service connects.

1. On the phone: open the app, grant Bluetooth permissions, turn Bluetooth on,
   tap **Enable keyboard mode**, then **Make phone discoverable**.
2. On Windows: `Settings → Bluetooth & devices → Add device → Bluetooth`.
3. Wait for the phone's Bluetooth name to appear and click it.
4. Windows may show a PIN or ask you to confirm a passkey on the phone —
   accept it on both sides. (HyperOS / MIUI phones ask for a 6-digit passkey
   confirmation; other skins vary.)
5. Once paired, open the app again, select the computer in the list and tap
   **Connect**, then **Open remote**.

If the computer was paired to the phone before keyboard mode existed, remove the
pairing on both sides first (`Settings → Bluetooth & devices → (phone) →
Remove device` on Windows, and "Forget" on the phone) and pair again —
otherwise Windows keeps the old device class and no keys arrive.

To confirm Windows sees a keyboard:

- `Settings → Bluetooth & devices` shows the phone as a connected device.
- Open `Device Manager` (right-click the Start button) and look under
  `Keyboards` / `Human Interface Devices` for an entry named after the phone.
- Open Notepad (or any text field) and press the phone's **Next** button: a
  `→` should be typed. If nothing happens, the HID channels did not open —
  disconnect and reconnect from the app's setup screen.

These steps target Windows 10 (22H2) and Windows 11. The phone keeps its
`phone` device class, so Windows may still list it under phones; the proof that
the keyboard works is the key test above, not the icon.

> Status: pairing guide added from the Bluetooth HID specification and the
> Linux-validated descriptor. End-to-end verification on a Windows host is
> pending user confirmation.

## Key map

The default map targets Reveal.js and other web slide frameworks. Every
computer receives a plain keyboard, so the same keys drive most presentation
software.

| Control | Key sent | Typical behavior |
| --- | --- | --- |
| Next (tap) | `→` | Next fragment or slide |
| Previous (tap) | `←` | Previous fragment or slide |
| Next (hold) | `Alt+→` | Next slide, skipping fragments |
| Previous (hold) | `Alt+←` | Previous slide |
| First slide | `Shift+←` | Jump to the first slide |
| Last slide | `Shift+→` | Jump to the last slide |
| Black screen | `B` | Toggle black screen |
| Full screen | `F` | Toggle fullscreen |
| Overview | `Esc` | Slide overview |
| Theme | `T` | Toggle theme |

`Alt` and `Shift` combinations follow the Reveal.js and Slidev conventions.
Applications with different shortcuts still receive the keys, so the deck keeps
control over what they mean.

## Troubleshooting

**Keyboard mode cannot be enabled.** Check that Bluetooth permission was
granted and that Bluetooth is on. If the system prompt no longer appears, use
**Open app settings**. A phone without the HID device profile reports
*No HID* and cannot be used.

**The computer lists the phone but no keys arrive.** Remove the pairing on both
sides, put the phone back in keyboard mode, and pair again. After connecting,
check that the host created a keyboard device:

```bash
grep -A6 "Uniq=<phone-bluetooth-mac>" /proc/bus/input/devices
```

If no such block exists, the host did not open the HID channels; reconnect from
the phone's setup screen.

**The key is sent but nothing changes.** The target window must be focused, and
the application must handle the key. Click once on the slide area before
starting.

**Keys stop arriving after reinstalling the app or re-pairing.** The computer
may keep the previous HID session. Disconnect on both sides, then connect again
from the phone's setup screen.

**A snackbar reports "No computer is connected".** Reconnect from the setup
screen; the host may have dropped the link while the phone was idle.

**Timer keeps running in the background.** This is intended: the clock is
monotonic and unaffected by the app lifecycle. Reset it between rehearsals.

**Windows asks for a PIN and pairing fails.** Some Windows builds require a
manual PIN during pairing. Use the PIN shown on the phone, or pair from the
phone side (`Settings → Bluetooth → (computer) → Pair`) which lets Windows
choose the PIN.

**Windows pairs but the phone is not under Keyboards.** Windows may keep the
phone under "Other devices" until the HID service connects. Disconnect and
reconnect from the app's setup screen; if it persists, remove the pairing on
both sides and pair again while the phone is in discoverable mode.

**Windows blocks the connection as an "unknown device".** Make sure the phone
is discoverable and that Windows Bluetooth is set to allow new devices. On
managed/corporate machines, Bluetooth may be disabled by policy.

## Limitations

- HID input is one-way, so the remote cannot show the current slide, the total
  number of slides, or a per-slide pace.
- The timer is a global elapsed/target indicator, not a per-slide schedule.
- The app must stay in the foreground for the Bluetooth HID registration to
  stay alive; switching away for a long time can drop the connection.
- The APK is a debug build; no release signing, obfuscation, or Play Store
  packaging is configured.

## Roadmap

- [x] Bluetooth HID keyboard for Android 9+
- [x] Presentation timer with editable target
- [x] Deck shortcuts and hold-to-skip
- [x] Windows host: pairing guide (Windows 10/11)
- [ ] Windows host: end-to-end validation on a physical machine
- [ ] macOS host: pairing guide
- [ ] Configurable key bindings per application
- [ ] Remember the last host and reconnect automatically
- [ ] Foreground service so the connection survives long background periods
- [ ] Signed release build and F-Droid packaging

## License

GPL-3.0. See [`LICENSE`](LICENSE).

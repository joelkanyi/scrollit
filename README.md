# scrollit

Scroll your phone from your watch.

Rotate the crown on your Wear OS watch and the page moves on your phone. Anticlockwise
scrolls down, clockwise scrolls back up. Swiping the watch face works too. It scrolls
whatever is on screen, in any app, because it drives the phone through an accessibility
gesture instead of talking to one app.

I built it to read long articles and docs on the phone without holding it.

## How it works

Three pieces and one message path between them.

- **Watch** (`ScrollSender`) reads the crown and sends a scroll distance to the phone.
- **Phone** (`ScrollReceiverService`) receives that distance over the Wearable message API.
- **Phone** (`ScrollAccessibilityService`) replays it as a swipe on whatever app is open.

Small scrolls are batched over a 30 ms window, so a fast flick becomes a single message
instead of a flood of them. The swipe runs down the center of the screen, slow enough that
Android reads it as a scroll and not a tap.

## Setup

1. Install the phone app and the watch app.
2. On the phone, open scrollit and tap **Enable Accessibility Service**. Android only lets
   an app inject touches into other apps through an accessibility service, so this step is
   required.
3. Open scrollit on the watch. The phone shows Connected once it finds the watch.

The phone app runs on Android 7.0 (API 24) and up. The watch app needs Wear OS on API 30
and up.

## Usage

Rotate the crown, or swipe up and down on the watch face. That is the whole app.

Crown direction lives in one place, `WearApp.kt`. The sign the crown reports differs
between watches, so if it feels reversed on yours, flip the sign on the `sendScroll` call.

## License

```
Copyright 2026 Joel Kanyi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

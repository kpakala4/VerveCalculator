# Verve Calculator

An Android calculator app that demonstrates integrating PubNative's HyBid SDK with both banner and interstitial ads. The interstitial is configured to appear after every successful calculation, while a persistent banner remains on screen.

## HyBid Configuration

- **App token:** `dde3c298b47648459f8ada4a982fa92d`
- **Zone ID:** `1`

## Project Structure

- `HybridApp` initialises the HyBid SDK and enables test mode.
- `MainActivity` hosts the calculator UI, loads the banner ad, and manages the interstitial lifecycle.
- Layout resources include a simple calculator keypad with a `HyBidBannerAdView`.

## Running the App

1. Open the project in Android Studio (Giraffe or newer is recommended).
2. Ensure you have the Android SDK 34 installed.
3. Sync Gradle to download dependencies.
4. Deploy the app to an emulator or physical device with internet access.

The HyBid SDK is initialised in test mode so ads will display the provider's test creatives.

## Screenshots

To capture screenshots for submission, build and run the app, perform a few calculations, and capture the UI showing both the banner and the interstitial when it appears.

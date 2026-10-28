# BOSS X Power Android — source project

এটি আপনার `BOSS_X_POWER_AUDIO_BLUE_WHITE_NOISE_GATE.zip` থেকে তৈরি করা Android WebView wrapper project। এটি Android Studio-তে খুলে APK build করার জন্য। এই পরিবেশে Android SDK/Gradle build setup না থাকায় APK ফাইল তৈরি হয়েছে বলে দাবি করা হচ্ছে না।

## কী আছে
- `https://web.whatsapp.com/` লোড করার WebView
- Microphone/Camera runtime permission handling
- Reload এবং Desktop/Mobile user-agent toggle
- মূল Chrome extension-এর সব ফাইল `original-extension/` ফোল্ডারে reference হিসেবে রাখা

## গুরুত্বপূর্ণ সীমাবদ্ধতা
- মূল ZIP একটি Chromium Extension; এর `chrome` APIs ও WhatsApp page injection Android WebView-তে স্বয়ংক্রিয়ভাবে চলে না। তাই original mic gain/EQ/noise-gate controls এই APK-তে কার্যকর হবে না।
- Desktop user-agent শুধু ওয়েবসাইটকে desktop browser হিসেবে পরিচয় দেওয়ার চেষ্টা করে; এটি Chrome extension support যোগ করে না।
- WhatsApp Web Group Calling Android WebView-তে সমর্থিত হবে এমন নিশ্চয়তা নেই। Call UI না এলে বা call load error এলে অফিসিয়াল WhatsApp Android app ব্যবহার করুন।
- WhatsApp login/QR এবং কলের জন্য ইন্টারনেট লাগবে। কখনো নিজের QR/session অন্যকে দেবেন না।

## APK build (কম্পিউটার/Android Studio)
1. Android Studio-তে এই পুরো project folder খুলুন।
2. Gradle sync শেষ হতে দিন; Android SDK Platform 35 ইনস্টল করুন।
3. Build → Build APK(s) চাপুন।
4. সফল হলে `app/build/outputs/apk/debug/app-debug.apk` পাওয়া যাবে।

## ফোনে build
শুধু ফোন ব্যবহার করলে Android IDE/build service-এ এই project ZIP upload করে Gradle Android project হিসেবে build করতে হবে। কোনো অনির্ভরযোগ্য সাইটে WhatsApp QR, session বা password দেবেন না।

## GitHub দিয়ে ফোন থেকেই APK build
1. এই ZIP extract করুন (ফোনের Files app-এ Extract অপশন ব্যবহার করুন)।
2. GitHub-এ নতুন repository বানান, যেমন `BOSS-X-POWER-ANDROID`।
3. repository-তে project-এর ভেতরের সব ফাইল/ফোল্ডার upload করুন—`app`, `.github`, `build.gradle`, `settings.gradle`, `gradle.properties` ইত্যাদি। ZIP ফাইলটিকে শুধু upload করলে build হবে না; ZIP extract করে তার ভেতরের contents upload করতে হবে। `.github/workflows/android.yml` ফাইলটি অবশ্যই থাকবে।
4. GitHub repository-তে **Actions** tab খুলুন। `Build Android APK` workflow বেছে **Run workflow** চাপুন (অথবা `main` branch-এ push হলে build নিজে শুরু হবে)।
5. সবুজ tick এলে workflow run খুলুন, নিচে **Artifacts** থেকে `BOSS-X-POWER-debug-apk` চাপুন। এতে APK-সহ ZIP নামবে। ZIP extract করে `app-debug.apk` ইনস্টল করুন। ফোনে “Install unknown apps” অনুমতি চাইতে পারে।

**সীমাবদ্ধতা:** build সফল হলেও WhatsApp Web-এর Group Call Android WebView-তে চলবেই—এমন নিশ্চয়তা নেই। এই project Chrome extension চালায় না; তাই extension-এর audio/noise-gate controls APK-তে স্বয়ংক্রিয়ভাবে কাজ করবে না।

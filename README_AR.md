# أذكاري 2027 (Azkari 2027 — Android 16 / Redmi Note 15 Pro 5G)

## كيف تحصل على ملف الـ APK الجاهز للتثبيت في دقيقتين (بدون Android Studio نهائياً)
المشروع جاهز ومزود بملف بناء سحابي تلقائي (`.github/workflows/build-apk.yml`) ومفتاح توقيع Release جاهز (`app/azkari2027-release.jks`):

1. أنشئ مستودعاً (Repository) مجانياً على **GitHub** (يمكن جعله Private).
2. فك ضغط `Azkari_2027_Android16.zip` وارفع محتوياته إلى المستودع (تأكد من وجود مجلد `.github/workflows/build-apk.yml`).
3. افتح تبويب **Actions** في GitHub، ستجد مهمة **Build Azkari 2027 APK (Android 16)** بدأت تلقائياً (أو اضغط **Run workflow**).
4. بعد دقيقتين تقريباً، افتح نتيجة البناء وحمّل ملف **`Azkari-2027-Android16-APK`** من قسم **Artifacts**.
5. انقل `Azkari-2027-Android16-Signed.apk` إلى هاتفك **Redmi Note 15 Pro 5G** وثبّته مباشرة!

## أو عبر Android Studio على الكمبيوتر
- افتح المجلد في Android Studio (مع SDK 36).
- اختر `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

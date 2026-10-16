# Cytaty TTC — Android

Aplikacja otwiera https://cytat.nsip.ovh/ z obecnym logowaniem i wszystkimi funkcjami strony. Wymaga internetu i Androida 8 lub nowszego. Hasła nie są zapisane w aplikacji. Sesja i preferencja motywu pozostają w jej danych.

## Pobieranie

Gotowy `Cytaty-TTC.apk` jest w Releases → Aplikacje Cytaty TTC oraz w artefaktach workflow Android APK. Prywatne repozytorium wymaga zalogowania na konto z dostępem.

Jest to APK do instalacji ręcznej podpisany kluczem debug. Kolejny build może wymagać odinstalowania poprzedniej wersji (usuwa to sesję i preferencje). Do trwałej dystrybucji należy skonfigurować stały klucz podpisu.

## Budowanie

GitHub Actions buduje APK i wykonuje Android Lint po każdym pushu na main. Build można też uruchomić ręcznie: Actions → Android APK → Run workflow.

Lokalnie otwórz katalog android w Android Studio. Alternatywnie użyj JDK 17, Gradle 8.9 i Android SDK 35, następnie w katalogu android uruchom:

```
gradle :app:assembleDebug :app:lintDebug
```

Aplikacja używa systemowego WebView. Walidacja HTTPS jest włączona, zewnętrzne odnośniki otwierają przeglądarkę. Aplikacja nie ma natywnego dostępu do plików ani mostka JavaScript. Zmiany strony pojawiają się bez aktualizacji APK.

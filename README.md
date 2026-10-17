# Cytaty TTC — Android

Aplikacja otwiera https://cytat.nsip.ovh/ i zachowuje logowanie oraz motyw. Android 8 lub nowszy.

## Pobieranie i aktualizacje

https://github.com/TTCenjoyer/Cytaty-ttc-apk/releases/download/mobile-latest/Cytaty-TTC.apk

Od wersji 1.2.0 aplikacja sprawdza nowe wersje przy uruchomieniu i powrocie do aplikacji, najwyżej raz na 6 godzin po udanym sprawdzeniu. Pokazuje propozycję pobrania nowej wersji. Instalację zawsze zatwierdza użytkownik w Androidzie; przy pierwszej aktualizacji może być wymagane zezwolenie na instalowanie aplikacji z tego źródła.

APK jest pobierane po HTTPS, weryfikowane przez SHA-256, nazwę pakietu, wyższy numer wersji oraz zgodność certyfikatu podpisu z zainstalowaną aplikacją. Brak internetu nie blokuje strony ani nie powoduje komunikatu przy każdym uruchomieniu. Plik jest udostępniany instalatorowi przez FileProvider ograniczony do katalogu aktualizacji w prywatnej pamięci podręcznej.

Starsze wersje były podpisane tymczasowymi kluczami debug. Przejście z nich na 1.2.0 wymaga jednorazowego odinstalowania starej aplikacji (usuwa logowanie i preferencje). Kolejne wydania podpisuje ten sam stały klucz, więc nie wymagają odinstalowywania.

## Stały podpis w GitHub Actions

W Settings → Secrets and variables → Actions dodaj repozytoryjny sekret `ANDROID_SIGNING_JSON`. Jego JSON zawiera pola `keystore_base64` (PKCS12, alias `cytaty-ttc`) i `password`. Nigdy nie zapisuj tego sekretu ani prywatnego klucza w repozytorium lub w logach. Trzymaj kopię poza GitHubem. Workflow nie publikuje niepodpisanego APK, gdy sekretu brakuje.

Workflow najpierw buduje aplikację i uruchamia Android Lint. Następnie odtwarza klucz z sekretu do pliku tymczasowego, buduje i sprawdza wariant release, publikuje APK oraz `update.json` z numerem wersji i SHA-256. Tymczasowy klucz jest usuwany również po błędzie. Przy każdym nowym wydaniu zwiększ `versionCode` oraz `versionName` w `android/app/build.gradle`.

Zmiany strony są widoczne bez aktualizacji APK. Aplikacja nie ma dostępu do plików strony ani mostka JavaScript; weryfikacja TLS pozostaje włączona.

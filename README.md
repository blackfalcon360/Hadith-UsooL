# اصولِ حدیث — Usul al-Hadith 📖

A structured, offline course in **mustalah al-hadith** (the terminology and methodology of hadith), written in **Urdu with Arabic quotations** and an English summary for every lesson. Interface: **Urdu (default) and English**. Portrait only. No permissions needed.

## What is inside
- **24 lessons in 8 units**: foundations and compilation · isnad and matn, methods of receiving hadith · mutawatir and ahad (mashhur, 'aziz, gharib; authority of the single report) · sahih, hasan, da'if (and acting on weak reports) · continuity of the chain (marfu'/mawquf/maqtu', 'an'ana — Muslim vs al-Bukhari, mursal, mu'dal, munqati', tadlis) · the narrator (justice, precision, ranks of jarh and ta'dil — Ibn Abi Hatim and Ibn Hajar) · defects (shadhdh, munkar, 'illa, mudraj, mudtarib, maqlub, mawdu') · practical method (i'tibar, mutaba'a, shahid, takhrij, tiers of hadith books, conflicting reports and abrogation).
- Every lesson: Arabic definitions as written by the scholars, an Urdu explanation, **the positions of named scholars where they differ** (e.g. Abu Hanifa, Malik, al-Shafi'i, Ahmad, Muslim, al-Bukhari, Ibn al-Salah, al-Nawawi, Ibn Hajar, Ibn Taymiyyah, Ibn Hazm, the Hanafi usul scholars), and **references to classical and contemporary books** (book and chapter/type).
- **Glossary** of 65 terms (Arabic, Urdu, English), searchable, each linked to its lesson.
- **Chain checker**: a guided exercise that asks what a muhaddith asks — is the chain connected? are the narrators just? precise? any shudhudh? any hidden 'illa? any supporting routes? — and names the resulting category with an explanation and source. It is a learning aid, not a tool to grade a real hadith.
- **Quiz**: 34 multiple-choice questions with explanations, by unit or all units, with your best score saved.
- Progress tracking, bookmarks (★), continue-where-you-stopped, text size, copy/share a lesson. **Sources and reading list** in Settings.

## Please note
- This is educational material, not a fatwa, and it does not authenticate or weaken any particular hadith. Refer to qualified scholars for rulings.
- References give book and chapter/type; page and volume numbers differ between editions, so check them against your copy.
- The material was prepared from the standard works listed in Settings; it should be reviewed by a qualified scholar before being distributed widely.

Android 7.0 (API 24) or newer. Text typed in boxes is black on white.

Brought to you By: Black Falcon 🦅 (shown in the lower right corner)

## Build
Push to GitHub -> Actions -> "Build APK" -> download `HadithUsul-APK`.
Local: `gradle assembleDebug` (JDK 17, Gradle 8.7+).
The course text lives in `app/src/main/assets/content_*.txt` (plain UTF-8, easy to correct or extend).

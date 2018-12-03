# Module app

# Package com.wordsdict.android.billing

All classes related to Google Play Billing

# Package com.wordsdict.android.data

All classes holding data sources for Words. This includes a Wordset Room database, Merriam-Webster API
and associated Room database, Firestore collections and documents (users/, users/{userId}/words, words/), SharedPreferences
for both global and and user tied shared preferences and in-memory SymSpell spelling corrector.

All data sources are abstracted behind high-level Repository classes.

# Package com.wordsdict.android.di

All Dagger 2 classes

# Package com.wordsdict.android.service

Android services and related classes that run Words functionality

# Package com.wordsdict.android.ui

All Android UI related Activities, Fragments, Adapters and other front-end related functinality

#Package com.wordsdict.android.util

Custom views and miscellaneous utility classes
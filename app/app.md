# Module app

# Package space.narrate.words.android.billing

All classes related to Google Play Billing

# Package space.narrate.words.android.data

All classes reading, writing or retrieving data for Words. This includes a Wordset Room database, 
a Merriam-Webster API service, a Merriam-Webster Room database for stored API responses, 
Firestore User, Firestore Word (per User), Firestore GlobalWord and an in-memory SymSpell 
spelling corrector.

All raw data objects and access are abstracted behind a Store. Stores are then grouped 
and abstracted behind Repositories. 

# Package space.narrate.words.android.di

All Dagger 2 classes

# Package space.narrate.words.android.service

Android services and related classes that run Words functionality

# Package space.narrate.words.android.ui

All Android UI related Activities, Fragments, Adapters and other front-end related functinality

#Package space.narrate.words.android.util

Custom views and miscellaneous utility classes
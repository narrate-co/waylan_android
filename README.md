# Waylan

A dictionary that focuses on execution, ergonomics and experience concepts

![Light banner](assets/board_light.png?raw=true "Light banner")

<a href="https://play.google.com/store/apps/details?id=space.narrate.words.android" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="90"/></a>

## Structure

Waylan is broken into modules. For a high level overview of each module, see the comments in settings.gradle. 

## Notes

For documentation, run ./gradlew dokka and view the javadoc under app/build/javadoc.

## Build

This project depends on Firebase. If building from source, you'll need to set up a Firebase project, enable Firestore and Firebase Auth (anonymous & email) and download and include Firebase's google-servies.json.

This project also depends on the Merriam-Webster API. You'll need an API key which can be obtained from https://dictionaryapi.com. Create a file in the project's root folder called keys.properties with the line `merriamWebster.key="<YOUR_KEY_HERE>"`. 

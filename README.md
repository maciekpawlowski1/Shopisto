# Shopisto

## Used language:
* Java

## Used technologies:
### Firebase:
* Firebase Realtime Database
* Firebase Authentication
### Multithreading:
* Firebase Tasks
* AsyncTask
### Offline database:
* SQLite
### Other:
* Voice input
* Possibilities to share via SMS or other apps

## When created:
* I've written it in August/September 2021. All process of creating it took me 1 month.

## Main challenges:
* Lists shared with other users. It would be easy if I use valueChangeListener in RealtimeDatabase on all list/lists but it would use a lot of network resources
 and simultanously connections with Firebase (and in Firebase it costs). I used some tricks to manage it

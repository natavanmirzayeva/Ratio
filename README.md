# Udacity Capstone Project

##Description 

   If you want to reach for the best-loved tools the only address you need is Ratio. In our application you are the determining role. Due to you and all Ratio lovers we can analyze and share with people the most fascinating, popular, top rated and most loved movies, tv series or books. 
   You can:
       * See the newest or the most loved and popular movies, tv series or books.
       * Create and follow your Ratio collection by rating to your favourite or disliked movies, tv series or books.
       * Get the fundamental informations about them.
       * Search and see the popularity of the tools you seek for. 

Intended User

   This application belongs to all users which want to watch the most favourite movies or tv series and to read the most demanding books    so far.

   Features
   Browse newest movies, tv series and books.
   Browse the level of appreciation of movies, tv series and books.
   Rate and see the movies, tv series and books rated by the user himself.
   Get the fundamental information of movies, tv series and books.


Key Considerations

   How will your app handle data persistence? 

   App will use Content Provider & Firebase Database  for preserving local data. Also the application will use Shared Preferences for maintaning widgets and Sync Adapter for updating data in its cache.
   Describe any edge or corner cases in the UX.

   The application should  written exclusively  in the Java Programming Language.
   The application should correctly preserve and restore user or app state on orientation change.
   The application should not close,freeze on any targeted device.
   The application uses stable release versions of all libraries, Gradle and Android Studio. 

   Describe any libraries you’ll be using.

   Item                      Version
   Android Studio            3.0
   CompileSdk Version        26
   MinSdk Version            21
   Support AppCompat         26.1.0
   Support Design            26.1.0
   Firebase-Core             16.0.1
   Firebase-Auth             16.0.1
   Firebase Database         16.0.1
   Google Analytics          3.17
   Google AdMob              15.0.0
   Picasso                   2.71828
   Butter Knife              8.8.1
   Retrofit 2                2.4.0
   OkHttp3                   3.11.0
   RxJava 2                  2.1.16
   Gson                      2.8.5
   Gradle                    3.0.0


   Describe how you will implement Google Play Services or other external services.

   The application will use Google Analytics and Google AdMob which are depending on Google Play Services.

Required Tasks
   Task 1: Project Setup

   Create android project:
   creating a new project in Android Studio
   configuring libraries by adding them to the app build.gradle file’s dependencies

   Task 2: Implement UI for Each Activity and Fragment

   Built UI for:
   Main Activity
     Create ViewPager Adapter
     Use Navigation Bar Drawer
   Categories Fragment
   Create Book Fragment
   Create Movie Fragment
   Create Serial Fragment
   RatioCollection Fragment
   TopRated Fragment
   Profile Fragment
   Login Activity
   Register Activity 
   Search Fragment

   Task 3: Create Models for JSON mapping & find their API adress

   The Java classes which will be created:
   Movie
   https://www.themoviedb.org/
   Book
   https://developers.google.com/books/
   Serial
   https://www.tvmaze.com/

   Task 4: API

   Provide all API requests by using Retrofit 2 and mapping them by using Gson:
   Get all movies,books and tv series
   Get specific  Movie, Book or Serial by their title


   Task 5: Authentication

   Provide all users save their sessions by using Firebase Authenticaton service:
   Create a new user by email and password and also save them to Firebase Database with additional informations
   Login user by email and password


   Task 6: Database

   For handling all local data the application will be used Firebase Database and Content Provider for storing. Database requests :
   Get Top Rated movies, books and tv series by using their rate number field
   Get Ratio collection of the user by user own id and id of each collection
   On the absenting of internet connection get top 10 newest movies, books and 
               tv series from SQLite Db by using Content Provider and  also use Loaders for updating displayed items after loading new data.


   Task 7: Google Play Services

   Implement Google Analytics and Google Admob Services.


   Task 8: Adding advertisement

   Adding advertisement to the application using Google Admob Service.


   Task 9: Material and Adaptive Design

   Make layout designs of the application compatible also for tablets. And design screens according to material design principles.


   Task 10: Make last arrangements

   Provide a widget for  top rated collection item.   
   Move all strings into strings.xml.
   Enable RTL layout support.
   Provide support for accesibillity.


# Deliveroo / Rider App

## Building and releasing

All of the driverapp code lives in the driverapp-android directory under source control. The directory structure follows the suggested layout for Android apps as specified by the Google Android + Gradle documentation. There are different app variants, or flavors, which correspond to the different server environments (STAGING, PROD). The src/ directory contains a main/ directory that is the home to all the code and also directories that are specific to an app flavor which override the settings in the main/ directory.

Run the following commands:

```
  $ git clone https://<your-git-user>@github.com/deliveroo/driverapp-android.git
  $ cd driverapp-android
  $ git checkout staging
  $ git submodule init
  $ git submodule update
  $ ./gradlew install
```

It is recommended to use the latest version of AndroidStudio for developing on the driverapp.  From this IDE you can build, debug, or run any app flavor on an emulator or device.  

You can also utilize the Gradle command-line for building and releasing the app as well.  The follow command will build everything in all flavors.

```
  $ ./gradlew build
```

The results are put in build/outputs/apk/ directory.  To build a specific flavor use the assembleXXX task, for instance for the test environment:

```
  $ ./gradlew assembleStagingEnvDebug
```

and for the production environment:

```
  $ ./gradlew assembleProdEnvRelease
```

## Signing release candidates

The keystore used to sign the application is in the dev.jks file, commited to git.
The passwords for the keystore and the keys are in the gradle.properties file, also commited to git

## Continuous integration

We are currently using BuddyBuild to generate, sign and deploy our builds: https://dashboard.buddybuild.com/apps/58adbc0171e59b0100088e34

Upon every pull request to any branch or push to the staging or deploy branches, BuddyBuild automatically kicks in, builds the code and runs all tests. It relies on two shell script files to do additional operations: buddybuild_postclone.sh and buddybuild_postbuild.sh (commited to git). 

The apk's are uploaded to fabric:

[Staging](https://fabric.io/deliveroo2/android/apps/com.deliveroo.driverapp.test)

[Production](https://fabric.io/deliveroo2/android/apps/com.deliveroo.driverapp)

They are also uploaded to our amazon S3 bucket:

[Staging](https://test.deliveroo.co.uk/admin/app_packages)

[Production](https://deliveroo.co.uk/admin/app_packages)

## String translations

We use PhraseApp to store our strings and ask for translations: https://phraseapp.com/accounts/deliveroo/projects/driver/overview

We also use an Android Studio plugin to push up new strings to PhraseApp and to pull down new translations: just go to the Plugins section in Android Studio and search for PhraseApp.

## Other usefull links:

###Firebase

[Staging](https://console.firebase.google.com/project/riderapp-test/analytics/app/android:com.deliveroo.driverapp.test/overview)
[Production](https://console.firebase.google.com/project/riderapp-production/analytics/app/android:com.deliveroo.driverapp/overview)

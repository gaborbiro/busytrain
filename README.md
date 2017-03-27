# Deliveroo / Rider App

staging: [![BuddyBuild staging](https://dashboard.buddybuild.com/api/statusImage?appID=58adbc0171e59b0100088e34&branch=staging&build=latest)](https://dashboard.buddybuild.com/apps/58adbc0171e59b0100088e34/build/latest?branch=staging)

deploy: [![BuddyBuild deploy](https://dashboard.buddybuild.com/api/statusImage?appID=58adbc0171e59b0100088e34&branch=deploy&build=latest)](https://dashboard.buddybuild.com/apps/58adbc0171e59b0100088e34/build/latest?branch=deploy)

## Building and releasing

All of the rider app code lives in the driverapp-android directory under source control.
There are different app variants, or flavors, which correspond to the different server environments (STAGING, PROD).

Run the following commands:

```
  $ git clone https://<your-git-user>@github.com/deliveroo/driverapp-android.git
  $ cd driverapp-android
  $ git checkout staging
  $ git submodule init
  $ git submodule update
  $ ./gradlew installStagingEnvDebug
```

To create the production build:

```
  $ ./gradlew assembleProdEnvRelease
```

It is recommended to use the latest version of AndroidStudio for developing on the rider app.
From this IDE you can build, debug, or run any app flavor on an emulator or device.

The results are put in build/outputs/apk/ directory.

## Signing release candidates

The keystore used to sign the application is in the dev.jks file, committed to git.
The passwords for the keystore and the keys are in the gradle.properties file, also committed to git

BuddyBuild uses it's own identical copy of the keystore file, not the one in git.

## Environment setup

### String translations

We use PhraseApp to store our strings and ask for translations: https://phraseapp.com/accounts/deliveroo/projects/driver/overview

We also use the Android Studio plugin to push up new strings and to pull down new translations: 
just go to the Plugins section in Android Studio settings and search for 'PhraseApp'.

### Code Style

It is important for all team members to use the same code formatting rules.
Import the CodeStyle.xml (committed to git) at Settings -> Editor -> Code Style -> Manage -> Import

You can also use a keymap Macro to automatically optimize imports and reformat code.
To set it up, first find out the keyboard shortcuts for both OptimizeImports and ReformatCode 
(can be different based on your Settings -> Keymap settings).
Then do Edit -> Macros -> Start Macro Recording. Press the keyboard shortcuts and then Edit -> Macros -> Stop Macro Recording.
Then add the newly created Macro in a new keyboard shortcut (eg Ctrl + S) from the keymaps list in Android Studio settings.

### Installing the Lombok plugin

You can often see in the code annotations like @Data or @Builder. Those are from the Lombok third-party library.
Lombok would work without the plugin, but Android Studio would complain about it.
just go to the Plugins section in Android Studio settings and search for 'Lombok'.

## Continuous integration

We are currently using BuddyBuild to generate, sign and deploy our builds: https://dashboard.buddybuild.com/apps/58adbc0171e59b0100088e34

Upon every pull request for any branch, BuddyBuild automatically kicks in, builds the code and runs all tests. 
For the staging and deploy branches it also deploys the apk's.
It relies on two shell script files to do the deployments: buddybuild_postclone.sh and buddybuild_postbuild.sh (committed to git).

The apk's are deployed to fabric: [Staging](https://fabric.io/deliveroo2/android/apps/com.deliveroo.driverapp.test) and 
[Production](https://fabric.io/deliveroo2/android/apps/com.deliveroo.driverapp)

They are also deployed to our amazon S3 bucket: [Staging](https://test.deliveroo.co.uk/admin/app_packages) and 
[Production](https://deliveroo.co.uk/admin/app_packages)

After the build in the amazon bucket has been tested, someone will flip the switch on it and go live.
We have the option of critical update (riders are blocked from using the app until they update) and soft update (riders can check for updates).

We do not currently deploy to any app stores.

## Working with git

http://deliveroo.engineering/guidelines/git/

Our main development branch is "staging" (not master), this is the one you should always be checking out.

## Working with Charles to capture outgoing/incoming packets and fake server api error responses

https://deliveroo.atlassian.net/wiki/display/QA/Charles+Proxy

Other useful hosts you can add to Charles for tracking:
- ```*herokuapp.com``` - Login requests are sent to the herokuapp identity service
- ```*crashlytics*```
- ```*instabug*```
- ```*maps.googleapis.com```
- ```*newrelic*```
- ```*zendesk*```

## Other useful links:

- Firebase - analytics, [Staging](https://console.firebase.google.com/project/riderapp-test/analytics/app/android:com.deliveroo.driverapp.test/overview) and [Production](https://console.firebase.google.com/project/riderapp-production/analytics/app/android:com.deliveroo.driverapp/overview)
- [Instabug](https://dashboard.instabug.com/applications/deliveroocouk-f15ef8fa-6db7-49d4-93ef-fb2d0e4bd561) - in-app messaging and error reporting for riders
- New Relic - analytics, [Staging](https://rpm.newrelic.com/accounts/881102/mobile/33174541) and [Production](https://rpm.newrelic.com/accounts/881102/mobile/33176581)
- [Zendesk](https://driveroouk.zendesk.com/hc/en-us) - FAQ pages

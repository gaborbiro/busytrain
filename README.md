Building and releasing

All of the driverapp code lives in the driverapp-android directory under source control.  The directory structure follows the suggested layout for Android apps as specified by the Google Android + Gradle documentation.  There are different app variants, or flavors, which correspond to the different server environments (LOCAL, DEV, TEST, DEMO, PROD).  The src/ directory contains a main/ directory that is the home to all the code and also directories that are specific to an app flavor which override the settings in the main/ directory.

A prerequisite to building this project is building the roo-java-common project.

Check out that project and run:

  $ ./gradlew install

from in the top-level directory of that project.  This will install some common libraries for Java and Android to a local Maven repository on your machine.

It is recommended to use the current version of AndroidStudio for developing on the driverapp.  From this IDE you can build, debug, or run any app flavor on an emulator or device.  

You can also utilize the Gradle command-line for building and releasing the app as well.  The follow command will build everything in all flavors.

  $ ./gradlew build

The results are put in build/outputs/apk/ directory.  To build a specific flavor use the assembleXXX task, for instance for the dev environment:

  $ ./gradlew assembleDevEnv

and for the production environment:

  $ ./gradlew assembleProdEnv

There are also additions in the build.gradle file that create tasks for installing and running a particular app flavor.  For instance, here are some tasks for testing your app on a device or in the emulator for your own local server environment:

  $ ./gradlew installLocalEnv
  $ ./gradlew uninstallLocalEnv
  $ ./gradlew startLocalEnv
  $ ./graldew stopLocalEnv


Signing release candidates

There is one keystore that holds all of the signing keys for the Android applications.  Pull down this keystore from dev.deliveroo.com at /srv/keystore/deliveroo.jks.  Make a directory called jks/ in the top-level driverapp-android directory and put the keystore there.

Then, it is required that you put the passwords for the keystore and the keys in a file at ~/.gradle/gradle.properties, a sample of the contents of this file is shown below:

    ROO_JKS_STORE_PASSWORD={ask-greg-for-this}
ROO_JKS_KEY_PASSWORD={ask-greg-for-this}
ROO_DRIVERAPP_JKS_KEY_PASSWORD={ask-greg-for-this}


Google-specific API access

Visit our project in the developer console.  From here you can sign up for access to other Google APIs and set the credentials for accessing their APIs from the driverapp.  To use the Google Maps integration, it is required to have the SHA1 signature from the signing certificate of the app listed along with the API key.

Unit testing

There are a number of utilities in place to make unit testing easier for Android code without the need to run the tests on an emulator or physical device.  The current codebase makes use of Robolectric to simulate the Android environment in unit tests, Mockito for mocking out objects easily, and a test-specific Guice module that aids in making mocks for a bunch of singleton objects and services used throughout the app.

In order to properly use all of these technologies within a test there is some boilerplate that is necessary for each test.  Here is a recommended template for starting a new test:

package {package_containing_test_class};

import com.deliveroo.driverapp.testing.DriverappTestsModule;
import com.google.inject.Guice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class {ClassUnderTest}Test {

    private final DriverappTestsModule module = 
            new DriverappTestsModule();
    private {ClassUnderTest} testObject = new {ClassUnderTest}();

    @Before
    public void injectMocks() {
        Guice.createInjector(module).injectMembers(testObject);
    }

    @After
    public void checkMocks() {
        Mockito.validateMockitoUsage();
    }

    @Test
    public void somethingThatIsTestable() throws Exception {
    // Add expectations
    // Run the tests
    // Verify the restults
    }
}

Running unit tests currently requires launching the tests from the Gradle command-line.  This is due to a limitation in the current version of Android Studio which may be fixed in the future (having a debugger for some tests would be very useful!).

Since there are many flavors for the driverapp, the standard tests task in Gradle will run all unit tests for all flavors.  To keep the test run iteration process as short as possible, it's recommended to just run the test on one flavor and inspect the results that way.  There isn't a lot of logic that is different between the flavors, it is mostly just server configuration and choice of icons that change.

To run all tests for the localEnv flavor, execute the following from the top-level driverapp-android directory:

  $ ./gradlew testLocalEnvDebug

The results will be stored in build/test-report/localEnv/debug/ as an HTML file holding any potential stack traces and stdout/stderr output.

To run just a single test class, use the Grade --tests flag and specify a regular expression for the tests to run.  For instance, to run all tests for the CheckLocationServicesRunnableTest, execute:

  $ /gradlew testLocalEnvDebug \
      --tests *CheckLocationServicesRunnableTest

The same regular expression matching can also be applied to run a single test within a test class, for instance:

  $ /gradlew testLocalEnvDebug \
      --tests *CheckLocationServicesRunnableTest*Enabled

will only run the test called locationServicesEnabled() in the CheckLocationServicesRunnableTest class.

Testing location awareness (MockLocation)

To test a few key features of the app, you may want to inject custom locations into the app.  This is mainly used for triggering geofence enter events for assignment relocation and arrival acknowledgment requests.  There are two steps to follow to enable injecting custom mock locations into the driverapp:

Turn on mock locations in the driverapp.  This can be done by setting the use_mock_locations boolean resource value to true.  An\example of setting this for an app variant is found in src/localEnv/values/environment.xml.  Rebuild and install this app on your device.


Install the MockLocation app to your device (this has not been tested in an emulator because it relies on GooglePlayServices being installed which can be tricky).  This app is included in the driverapp source in the testing/mocklocation directory.  This app can also be deployed through AndroidStudio or Gradle but only has one flavor

Once both apps are installed, from the MockLocation app, single tap on any location.  This will now be the location emitted to the driverapp.  Single tap on another location will replace the maker in the MockLocation app and that new location will be sent to the driverapp.  There is some lag as locations are transferred between the two apps every 10 seconds.

The MockLocation app also has a search functionality built into the top menu.  Search for any postcode or landmark and the app will zoom into that location, but will not place a marker.  This is a useful way to zoom around to pinpoint exactly where you'd like to set a mock location. 


# hmpps-ppud-automation-api

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-ppud-automation-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-ppud-automation-api)

[![GitHub Actions](https://github.com/ministryofjustice/hmpps-ppud-automation-api/actions/workflows/pipeline.yml/badge.svg)](https://github.com/ministryofjustice/hmpps-ppud-automation-api)

[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-ppud-automation-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-ppud-automation-api)

[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-ppud-automation-api-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

This is an API project that exposes an interface for integrating with the
Public Protection Unit Database (PPUD). Rather than speak to the PPUD database,
it uses Selenium to automate the PPUD UI in order to perform its operations.

## Runbook
The [runbook](docs/runbooks/RUNBOOK.md) can be found in this repo in the docs/runbooks folder.

## Prerequisites

Docker desktop needs to be installed if you're looking to run locally or test with redis running.

Firefox needs to be installed. When running in a Docker container, the service uses the latest
[Extended Support Release (ESR) version of Firefox](https://www.mozilla.org/en-GB/firefox/enterprise/),
so it is advisable to use that version for local testing.

## Environment Variables

The project uses the following environment variables, which need to be set in order to run the tests or run the
application.

* PPUD_USERNAME - your username for logging in to PPUD. For the Internal Test instance of PPUD, the user
  should belong to the Recall 1 team and be Level 5.
* PPUD_PASSWORD - password for your PPUD account
* PPUD_ADMIN_USERNAME - a username for logging in to PPUD to perform admin functions such as retrieving reference data
  values. For the Internal Test instance of PPUD, the user can be the same as the non-admin user.
* PPUD_ADMIN_PASSWORD - password for admin PPUD account

The following additional environment variables can be used if required.

* PPUD_URL - the URL of the PPUD application. If not set, defaults to "https://internaltest.ppud.justice.gov.uk"
* AUTOMATION_HEADLESS - set this to false if you want to view browser interactions. If not set, defaults to true.
* AUTOMATION_FIREFOX_BINARY - set this to the path of your Firefox binary if it is not automatically detected (probably not needed locally)

## Feature Flags

This project uses [Flipt](https://www.flipt.io/) to control the availability of certain features.
Feature flags allow us to turn on or off parts of a service in production, decoupling "releases" from "deployments".

Feature flags are managed in the [Flipt dashboard](https://feature-toggles.hmpps.service.justice.gov.uk).
You'll need to be in the `ministryofjustice` organisation to access it.

To add a feature flag to your code:

1. Create a new boolean flag in
   the [dev](https://feature-toggles-dev.hmpps.service.justice.gov.uk), [preprod](https://feature-toggles-preprod.hmpps.service.justice.gov.uk),
   and [prod](https://feature-toggles.hmpps.service.justice.gov.uk) dashboard, within the `consider-a-recall` environment.
2. Update your code to inject the `FeatureFlagService` service, and call `enabled("<key>")`. Example:§

```kotlin
@Service
class MyService(private val featureFlagsService: FeatureFlagService) {
    fun myMethod() {
        if (featureFlagsService.enabled("my-flag")) {
            // Feature is enabled, do something
        } else {
            // Feature is disabled, do something else
        }
    }
}
```

There are three configuration values for Flipt:
* FLIPT_URL - the URL of the Flipt server
* FLIPT_API_KEY - the API key to access Flipt
* FLIPT_DEFAULT_FLAG_VALUE - the default value to return if Flipt is unavailable

The first two are stored as secrets in each environment, and are accessed from the Helm chart. The default value is set 
in both the Helm chart and the application.yml file. This is usually set to false to avoid accidentally enabling features
when Flipt is unavailable, but allows us to override this for the dev environment and local development if required (in
both of these we're more likely to want to enable features earlier than in the other environments).

For more information about Flipt, check out the [documentation](https://docs.flipt.io).


## Running Local Instance

Run the following to start Redis for caching

```
docker-compose up redis
```

Start the service in IntelliJ

The service will use the Auth service in the Dev environment, so tokens can be obtained
by using the client ID and secret for a calling service (i.e. make-recall-decision-api).

## Testing

The project uses a couple of testing levels - unit testing and integration testing.

Unit tests can be run without external dependencies.

The integration tests connect to a running instance of PPUD in order to verify that
the automation works correctly.

### Running Tests

Run the following script to run all the integration and unit tests locally:

```
./gradlew check
```

or to run linting followed by all tests, similar to running in CI, use:

```
./build.sh
```

By default, the integration tests run with a NoOp cache, as configured in
[application-test.yml](src/test/resources/application-test.yml). If you want to run the tests with redis, you can do so
by removing said configuration value (the spring.cache.type one) and running the following to start Redis for caching:

```
docker-compose up redis
```

### Things to Note About the Tests
In order to inject a level of independence and avoid clashes with other tests that
might be running, the integration tests create offenders to use in each test.

These offenders can be identified because their family name is "FamilyName-<UUID>", 
where <UUID> is an identifier for a particular test run.  At the end of the test run,
the matching offenders are deleted automatically.

Occasionally, something may happen that means the offenders do not get deleted. This would
typically be running tests locally and stopping them before they get a chance to tidy up.
Perhaps when stepping through to debug something.

Therefore, every so often, it's worth checking that there aren't any "FamilyName-<UUID>" 
offenders in PPUD, and deleting any that do exist.  They may cause test failures at some
point if there is a prison number clash.

### Debugging through screenshots
In some cases, you'll find the service fails because the screen it is interacting with
doesn't have an element it expects. It isn't always clear from the error stack what the
issue is or how to solve it, in which cases it can be useful to have a screenshot of the
screen right before the error happens. Since the screen is interacted with automatically
by Selenium, this needs to be done programmatically.

To take a screenshot, you need to have a hold of the root html element. You can do this in
one of two ways:
1. Declare a WebElement in the relevant class as follows:
```
    @FindBy(tagName = "html")
    private lateinit var htmlTag: WebElement
```
2. Add the following line of code to the relevant method:

```
    val htmlTag = driver.findElement(By.tagName("html")))
```

Then, before the line of code that fails, add the following:

```
    val scrFile: File = htmlTag.getScreenshotAs(OutputType.FILE)
    val destFile = File(Paths.get("test-results/<screenshot-name>.png").toString())
    scrFile.copyTo(destFile, true)
```

If running the code from IntelliJ, it will store the screenshot in a 'test-results' folder in
the project folder. Alternatively, you can store it in the temp folder (not sure where that
will be, it'll depend on the environment):

```
    val tempDir = System.getProperty("java.io.tmpdir")
    val destFile = File(Paths.get(tempDir, "$fileName.png").toString())
```

## Selenium, Webdriver and Browser

The service is set up to use Firefox as the browser, along with its associated
WebDriver implementation, geckodriver.  It isn't necessary to install geckodriver
explicitly as this is managed using [WebDriverManager](https://github.com/bonigarcia/webdrivermanager).  
WebDriverManager detects and installs the correct driver for the installed browser
version.

When running in a Docker container, the "Extended Support Release (ESR)" version
of Firefox is used, which is a few versions behind the latest version and changes
less frequently.  In the docker container, the path to the browser binary needs to
be set in the environment variable `AUTOMATION_FIREFOX_BINARY`.  This is not normally
necessary locally.

Chrome has also been used for the service locally on a Mac, but this could not be
installed in the docker container because a matching installation was not available.
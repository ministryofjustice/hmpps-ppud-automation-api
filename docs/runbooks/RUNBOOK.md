# PPUD Automation API Runbook

## Service or system overview

### Business overview

This provides an API interface for integrating with the HMPPS PPUD application (Public Protection Unit Database).
PPUD is used by PPCS (Public Protection Casework Section) in order to process requests for people on probation to be recalled to prison.  
This API allows the Consider a Recall service to facilitate that process by automating certain aspects.

### Technical overview

This is an internal API based on Springboot, using Kotlin as the main language and based on the HMPPS Kotlin template.  
It is deployed in kubernetes (the HMPPS Cloud Platform) using the configuration found in [helm_deploy](./../../helm_deploy).

### Service Level Agreements (SLAs)

Office hours (Mon-Fri, 09:00-17:00), best efforts.

### Service owner

The `make-recall-decision` team develops and runs this service.

Contact the [#consider-a-recall](https://mojdt.slack.com/archives/C01D6R49H34) channel on slack.

### Contributing applications, daemons, services, middleware

- Springboot application based on [hmpps-template-kotlin](https://github.com/ministryofjustice/hmpps-template-kotlin).
- [GitHub Actions](https://github.com/features/actions) for CI/CD.

## System characteristics

### Hours of operation

Available 24/7 in production.

In lower environments, the service is shut down out of hours, in line with standard HMPPS practice.

### Infrastructure design

The application runs on the [HMPPS Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/) within 
the `hmpps-ppud-automation-<tier>` namespaces (where `tier` is `dev`, `preprod` or `prod`).

The application runs as a deployment named `hmpps-ppud-automation-api`.  There are no other dependencies deployed within
the namespaces.

There is a dependency on Elasticache using Redis. Configuration values for this can be found in 
the [Helm values.yaml file](../../helm_deploy/hmpps-ppud-automation-api/values.yaml).  This is set up as per the normal
Cloud Platform approach.

The API is made available externally from the cluster via an Ingress.

See the `values-<tier>.yaml` files in the [helm_deploy](../../helm_deploy) directory for the current configuration of 
each tier.

### Security and access control

In order to gain access to the `hmpps-ppud-automation-<tier>` namespaces in kubernetes you will need to be a member of 
the [ministryofjustice](https://github.com/orgs/ministryofjustice) GitHub organisation and a member of 
the [making-recall-decision](https://github.com/orgs/ministryofjustice/teams/making-recall-decision) (GitHub) team. 
Once joined, you should have access to the cluster within 24 hours.

You will need to follow the [Cloud Platform User Guide](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#how-to-use-kubectl-to-connect-to-the-cluster) to set up your access from there - use instructions for connecting to the `live` cluster.

#### PPUD Credentials

The system makes use of 2 users in PPUD.  One for the "normal" operations of booking a 
recall and another for admin operations such as retrieving the reference data.

These users are set up in PPUD as follows:
Normal user - Level 2, belongs to "Recall Team" team ("Recall 1" in Internal Test)
Admin user - Level 5, belongs to "Performance Management" team.

Further details can be found in [Setting Up Users in PPUD in Confluence](https://dsdmoj.atlassian.net/wiki/x/DQBREwE).

As noted elsewhere in this document, user credentials are stored in the following
environment variables:

- `PPUD_USERNAME`
- `PPUD_PASSWORD`
- `PPUD_ADMIN_USERNAME`
- `PPUD_ADMIN_PASSWORD`

That means that if passwords/usernames are changed they will need updating in the 
following locations:

- Local development machine if not using developer username/password
- [GitHub Actions pipeline repository secrets](https://github.com/ministryofjustice/hmpps-ppud-automation-api/settings/secrets/actions) (not environment secrets)
- Kubernetes secrets for the appropriate environment (see Secrets management below)

### Throttling and partial shutdown

If there is an issue with the service where it is causing load on downstream services, and it needs to be shutdown quickly, the following command will reduce the number of pod replicas to zero:

```
kubectl -n hmpps-ppud-automation-<tier> scale deployment hmpps-ppud-automation-api --replicas=0
```

We do not currently have a strategy in place to throttle requests.

### Environmental differences

Infrastructure wise, all three tiers are identical, but `prod` has the following differences:

- It will have more pod replicas of the main application deployment.
- As this is live data, you **must** be SC cleared if you need log into the cluster and interact with the application pods or data exposed. You **do not** however need to be SC cleared to make changes to the application and deploy via the CI pipelines.

## System configuration

### Configuration management

- Infrastructure is configured via [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments).
- Application configuration is via [helm_deploy](../../helm_deploy).

### Secrets management

Secrets are stored within the `hmpps-ppud-automation-<tier>` namespaces in kubernetes.

Secrets with information from [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments) will be managed via the terraform code in there.

The contents of the `hmpps-ppud-automation-api` secret are handled in the following way:

- `API_CLIENT_ID` - managed externally via tooling in the `hmpps-auth` project.
- `API_CLIENT_SECRET` - managed externally via tooling in the `hmpps-auth` project.
- `APPLICATIONINSIGHTS_CONNECTION_STRING` - managed externally via the [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments) tooling.
- `PPUD_USERNAME` - managed manually through Kubernetes secrets (or as GitHub repository secrets for integration tests if the dev details are the ones that have changed)
- `PPUD_PASSWORD` - managed manually through Kubernetes secrets (or as GitHub repository secrets for integration tests if the dev details are the ones that have changed)
- `PPUD_ADMIN_USERNAME` - managed manually through Kubernetes secrets (or as GitHub repository secrets for integration tests if the dev details are the ones that have changed)
- `PPUD_ADMIN_PASSWORD` - managed manually through Kubernetes secrets (or as GitHub repository secrets for integration tests if the dev details are the ones that have changed)

Instructions on managing the Kubernetes secrets manually can be found in the [Cloud Platform User Guide - Adding Secrets to an application](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/deploying-an-app/add-secrets-to-deployment.html#adding-secrets-to-an-application)

## System backup and restore

No data is stored (other than Redis caching) so backups are not performed.

## Monitoring and alerting

### Log aggregation solution

Please see [Confluence](https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/4537352193/PPUD+Automation+UI+subsystem+-+Technical+Architecture#Logging-and-Alerting) for more details.

### Log message format

Currently, the ELK solution cannot correctly process/transform structured/JSON logging, so a `log4j` single-line output 
is currently preferred.

### Metrics

Please see [Confluence](https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/4537352193/PPUD+Automation+UI+subsystem+-+Technical+Architecture#Logging-and-Alerting) for more details.

### Health checks

#### Health of dependencies

`/health` (i.e. https://hmpps-ppud-automation-api.hmpps.service.justice.gov.uk/health) checks and reports the health of 
all services and components that the application depends upon. An HTTP 200 response code indicates that everything is healthy.

#### Health of service

`/health/liveness` (i.e. https://hmpps-ppud-automation-api.hmpps.service.justice.gov.uk/health/liveness) indicates that 
the application is started up, but does not indicate that it is ready to process work. An HTTP 200 response code 
indicates that the application has started.

`/health/readiness` (i.e. https://hmpps-ppud-automation-api.hmpps.service.justice.gov.uk/health/readiness) indicates that
the application is ready to handle requests as it has checked its connections to all dependencies are working. 
An HTTP 200 response code indicates that the application has started and is ready to handle requests.

## Operational tasks

### Deployment

We use GitHub Actions to manage deployments (see [.github/workflows/pipeline.yml](../../.github/workflows/pipeline.yml) and
[.github/workflows/deploy_to_env.yml](../../.github/workflows/deploy_to_env.yml) for the full configuration):

- Built docker images are pushed to [ghcr.io](https://ghcr.io/ministryofjustice/hmpps-ppud-automation-api).
- Deployment to kubernetes uses helm.

### Troubleshooting

Please see [Confluence](<https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/3987210241/Monitoring+Operability#Debugging-an-Application-That-Fails-to-Start>) for some generic troubleshooting notes.

## Maintenance tasks

### Identified vulnerabilities

We scan the currently deployed docker containers daily with [trivy](https://github.com/aquasecurity/trivy). 
If any `HIGH` or `CRITICAL` vulnerabilities are identified the team is notified in the
[#make-recall-decisions-dev](https://mojdt.slack.com/archives/C03B57W0ALT) Slack channel. 

**These issues should be fixed as soon as possible.**

## UI Automation

The service is unusual in its operation in that rather than interacting directly 
with a database, it interacts with the user interface of the PPUD web application.  

This is achieved through using Selenium, which is a tool usually used for automated testing.
The practices used in the code are very similar to those practices that might be used
when testing.  Predominantly, this means that a Page Object Model approach has been
adopted.  This means that the HTML contents of a page are encapsulated with a Page Object,
i.e. a class that deals with that page specifically.  The Page Objects expose methods
for operations that can be performed on that page - for example "`createOffender`" on the
`NewOffenderPage`.  The button presses and field entries that are required to achieve
that are hidden inside the `NewOffenderPage`.

### Troubleshooting

Because of the nature of the interaction, errors may occur that are not expected by
the PPUD Automation API.  Sometimes these may be because something has gone wrong and
an expected control is not available on the "screen".  An error like this will
manifest itself as a 500 response and be logged appropriately.  Here is an example:

`org.openqa.selenium.NoSuchElementException: Unable to locate element: #Login1_UserName`

This means that the HTML element with the ID of `Login1_UserName` cannot be found.
This could be for several reasons:
* An unexpected page is being shown that does not contain the expected control
* An unexpected alert or dialog has been triggered
* There is a mistake/typo in the ID in the PPUD Automation API
* The PPUD application has been changed and the ID renamed
* The PPUD application has been changed and the control no longer exists on the page

The issue will need investigating in order to determine which of the above applies. It will
more than likely be reproducible in the automated tests, provided that appropriate data
can be injected.

Help with using Selenium and troubleshooting can be found in the [Selenium Documentation](https://www.selenium.dev/documentation/webdriver/). 

#### Step-by-Step Process

If a user has experienced a failure to book a recall to PPUD, the process for determining what
exactly has gone wrong would be something along the following lines:

1. Go to App Insights and run the "Failed Requests" query from below.
This will return any requests that have been unsuccessful.  The `resultCode` in the 
results will give some indication of the problem - e.g. 400 for an invalid request
or 500 for an exception.
2. Copy the `operation_id` from the failed request that you are interested in and 
click "Transaction Search" in the left hand navigation menu.  Paste the previously
copied `operation_id` into the search terms box and press enter (make sure that the
time range is set appropriately). 
3. The results will show each App Insights entry associated with that 
`operation_id`. Click one of the entries, then click "View Timeline" and you will
be presented with a timeline of the whole request.  From here, you will be able to
determine what has gone wrong.
4. Any exception will be coloured red. Click on the exception and view the stack
trace.
5. The exception message itself might be enough to determine what has gone wrong,
but if not, examine the stack trace.  You will be able to determine what the problem
was from the method that has failed and/or the specific line number in the method.
The specific line number is often useful when it highlights the specific HTML control
being accessed.

#### Useful App Insights Queries

* Failed Requests - return failed requests, ignoring "clutter"
```
requests
   | where cloud_RoleName == "hmpps-ppud-automation-api"
   | where success == "False"
   | where name !has "Health"
   | where name !has "info"
   | where name !has "SubscribingRunnable"
```
* Failed Requests with Exceptions - failed requests with associated exceptions
```
requests
| where cloud_RoleName == "hmpps-ppud-automation-api"
| where success == "False"
| where name !has "Health"
| where name !has "info"
| where name !has "SubscribingRunnable"
| join kind=leftouter exceptions on $left.operation_Id == $right.operation_Id
| project timestamp, name, resultCode, duration, operation_Id, type, outerMessage 
```
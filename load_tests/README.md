PPUD Automation Load Test
-------------------------

Performs a load test against the PPUD Automation API by performing a number of recalls in parallel. 

It does this by making sequential sets of the requests against the PPUD Automation API which
- Retrieve all the reference data
- Searches for an offender record
- Creates a new offender record
- Adds a sentence to the offender
- Adds a release to the sentence
- Adds a recall to the release
- Retrieves the recall
- Adds PartA, License, Precons, Pre Sentence, OASys and Charge Sheet documents to the recall.

Instructions
------------

- Install jmeter into the jmeter directory, so that the jmeter executable is available at ./jmeter/apache-jmeter-<version>/bin/jmeter. This has been tested with jmeter5.5
- Copy the user.properties.example file to user.properties and Modify so that it matches your test environment and requirements
- You can run the test in a headless mode using the run-car-load-test.sh script. This will put the results in the results directory with timestamped files

Notes
-----

- Dynamic data (eg offender CRN,name etc) is stored in the .csv file. This all synthetic data.
- The document upload uses the same document each time. 
- There are a number of calls to the token service. This is really just a canary to quickly establish whether failures are related to VPN issues, which at time of writing were quite prevalent. The only solution is to refresh your vpn and run again.

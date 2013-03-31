# Setting up development environment

## Clone
First use [git](http://git-scm.com/) to clone this repo:

    git clone {TODO: add stash URL}
    cd quickfixj-bridge-parent

## Build
UMF is built with [Maven](http://maven.apache.org/).

    mvn clean install

The above will build the prototype and run all unit tests.

## Run Bank services (open new Terminal tab)
    cd quickfixj-bank
    mvn exec:java

## Run Bridge services (open new Terminal tab)
    cd quickfixj-bridge
    mvn exec:java

## Run Venue services (open new Terminal tab)
    cd quickfixj-venue
    mvn exec:java

## Explore
* Monitors the console logs output by the three applications
* Venue simulator is going to periodically subscribe to different symbols, receive quotes and cancel RFQs
* Bridge is passing messages between the venue and the bank
* Bank simulator generates quotes in response to RFQ requests

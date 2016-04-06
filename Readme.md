ReDBox Base API Framework Using Camel 
===========================
[![Build Status](https://travis-ci.org/redbox-mint-contrib/redbox-api-base.svg?branch=master)](https://travis-ci.org/redbox-mint-contrib/redbox-api-base)

#Intro
This is a wrapper to Camel's RouteBuilder, that allows for configurable and scripted routes. No more building, just download  the war/jar and point it to your configuration.

#Design goals

- Create a quick and easy way to deploy a Camel Integration based application, in a scripted manner, as a war file or as a fat jar.
- Provide facilities to deal with parsing HTTP multipart form data uploads

#Configuration

- Set an 'redboxApiConfig' that points to a GroovSlurper config. It expects the config file to have at least one entry: "buildRoute", that points to a groovy script which will be used to create the routes. If you specify a "baseDir", it will be prefixed to the "buildRoute" script path. Because the configuration is in groovy, you can do things like set "baseDir" to the directory where 'redboxApiConfig' is:

   `baseDir = new File(System.getProperty('redboxApiConfig')).getParent() + '/'`

  When the 'buildRoute' script is executed, the following is available on its bindings:
    - routeBuilder: the Camel RouteBuilder instance used for creating routes
    - config:  the Config object
    - log: the logging object
  
- If you use the HTTP multipart parser, set a 'config.upload.procDir' where uploads are copied to.
- You may also set `env` variable to control which environment the configuration system will load.

#Deployment

Deployment options: as war file or as a fat jar. Tested with Tomcat 8. Tomcat 7 not supported.

#Building

To build this project use

    mvn clean install install
    
Pre-requisites:
  - mvnvm
    
    $ brew install mvnvm

To run this project from within Maven use

    mvn -Denv=development -DredboxApiConfig=<path to your config> exec:java

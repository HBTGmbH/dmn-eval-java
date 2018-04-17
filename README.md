[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

# About

dmn-eval-java is a [DMN](http://www.omg.org/spec/DMN/1.1/) engine to evaluate decision tables.
It is designed to be embedded in Java applications. If you require a standalone DMN engine,
have a look at [dmn-nodejs-server](https://github.com/HBTGmbH/dmn-nodejs-server) instead.

Please note that this implementation is still in an alpha stage. A mature and professionally supported
DMN engine is available from [Camunda](http://www.camunda.org). dmn-eval-java however supports the DMN
specification (more precisely, its expression language FEEL) to a higher degree than the decision engine by Camunda,
allowing more flexibility and more powerful expressions in decision tables.

Under the hood, dmn-eval-java uses the Javascript library [dmn-eval-js](https://github.com/HBTGmbH/dmn-eval-js)
and executes it using Nashorn. Refer to the documentation of that library for information which elements of DMN
decision tables are supported. 

# Usage

## Add a dependency on dmn-eval-java

So far, dmn-eval-java is not available in the Central Repository (OSS), but the idea is to publish is there
as soon as it reaches a certain level of maturity. Until then, you need to clone its sources and build it on your
own, to add it to your own repository. You will need Gradle and Java >= 1.9 to build dmn-eval-java.

To add dmn-eval-java to your own Java code, add the following dependency:

Gradle:
```
compile group: 'de.hbt', name: 'dmn-eval-java', version: '1.0.0-SNAPSHOT'
``` 

Maven:
```
<depdendency>
    <group>de.hbt</group>
    <artifactId>dmn-eval-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</depdendency>
``` 

## Use dmn-eval-java

dmn-eval-java exposes a DecisionService as central class to manage and execute decisions. So far there is no
Spring, CDI, EJB or whichever dependency injection integration. This might come in future versions, though.

### Register decisions

Decisions are registered under their unique identifier. This is the value that is given in the DMN definition.
If you try to register a decision under a different identifier, this will fail with a DecisionRegistrationException.

```
String decisionId = "approveLoan";
String pathToDmnXmlFile = "./approveLoan.xml";
DecisionService decisionService = new DecisionService();
Future<Decision> decisionFuture = decisionService.registerDecision(decisionId, pathToDmnXmlFile);
Decision decision = decisionFuture.get(10, TimeUnit.SECONDS);
```

### Execute decisions

To execute a decision, create an input object, which may either be a Map<String, Object> or a POJO. If there is
an input expression "foo.bar" in the decision table, then the value of the expression is obtained from looking
up "foo" in the map, and from the returned value (which may be another map or a POJO), "bar" is resolved. If the
input object is a POJO, the getter "getFoo()" is called, and from the returned value (which may be a map or another POJO),
"bar" is resolved. If no such key or getter method exists, the decision execution does not fail, but the input expression
is resolved to "undefined". Read the documentation of dmn-eval-js for a discussion of undefined values.

```
String decisionId = "approveLoan";
 
// your input for decision execution goes in here
ApproveLoanInput input = new ApproveLoanInput();
// set the input values
input.setValue(...);
 
DecisionEvaluationResult<ApproveLoanResult> output = decisionService.evaluateDecision(decisionId, input, ApproveLoanResult.class);
```









# Cucumber Test Framework & Plugin for SBT

<p align="left">
<img src="https://travis-ci.org/lewismj/cucumber.svg?branch=master"/>
<img src="https://maven-badges.herokuapp.com/maven-central/com.waioeka.sbt/cucumber-runner_2.11/badge.svg"/>
</p>

## Summary

This project contains a Cucumber test framework (runner) for sbt. There is also a plugin that provides a new sbt command.

1. **_Cucumber Runner_** A new *test framework* for sbt. It runs Cucumber tests as part of a unit test run (i.e. `sbt test`).

2.  A [plugin](plugin.md)  provides a new command `sbt cucumber`. It allows you to run Cucumber tests independently of unit tests.
  

Unless you have a specific requirement to run Cucumber tests outside of unit test framework, use the test framework
rather than the plugin.

The plugin can be used if you want a separate command to run Cucumber tests and have your normal test framework
ignore Cucumber tests. _The framework does not depend on the plugin_.


Waffle board [here][1]

## Notes

**0.0.8+** Cucumber Test Framework (_runner_)


1. **(n.b. change from previous verions)**  Use **CucumberSpec** as the base class for your Cucumber test suite now. See the cucumber runner example.

2. You can specify the Cucumber arguments via your `build.sbt` file, as follows:

```scala
val framework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
testFrameworks += framework

testOptions in Test += Tests.Argument(framework,"--monochrome")
testOptions in Test += Tests.Argument(framework,"--glue","")
testOptions in Test += Tests.Argument(framework,"--plugin","pretty")
testOptions in Test += Tests.Argument(framework,"--plugin","html:/tmp/html")
testOptions in Test += Tests.Argument(framework,"--plugin","json:/tmp/json")
```

In your class definition, use:

```scala
class MyCucumberTestSuite extends CucumberSpec
```

## Dependency Information

```scala
libraryDependencies += "com.waioeka.sbt" %% "cucumber-runner" % "0.0.8"
```


## Background

Many Scala projects will use FlatSpec for their BDD like testing. Some teams prefer the separation of Feature files from the code. 
There are two core projects, each has an example project illustrating the usage. 

## Contact

Michael Lewis: lewismj@waioeka.com

## Cucumber Runner

The _runner_ is a library that you can add as a dependency, if you want the Cucumber tests to run as part of a normal unit test run. That is, when you run `sbt test`. Your `build.sbt` file must reference the test framework as follows:

```scala
val framework = new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
testFrameworks += framework

// Configure the arguments.
testOptions in Test += Tests.Argument(framework,"--glue","")
testOptions in Test += Tests.Argument(framework,"--plugin","pretty")
testOptions in Test += Tests.Argument(framework,"--plugin","html:/tmp/html")
testOptions in Test += Tests.Argument(framework,"--plugin","json:/tmp/json")
```

Note, the runner will expect feature files in the `test/resources` directory. If your feature files are stored elsewhere, add that location to the 'unmanagedClasspath', e.g.

```scala
unmanagedClasspath in Test += baseDirectory.value / "src/test/features"
```


### Cucumber Runner Example

The project _example_ illustrates how to setup and use the _runner_. To integrate BDD testing into your unit test framework.
As shown below, using the runner and plugin, you can now run `sbt test`.

```scala
package com.waioeka.sbt

import com.waioeka.sbt.runner.CucumberSpec
import cucumber.api.scala.{ScalaDsl, EN}
import org.scalatest.Matchers

class CucumberTestSuite extends CucumberSpec


/** MultiplicationSteps */
class MultiplicationSteps extends ScalaDsl with EN with Matchers  {
  var x : Int = 0
  var y : Int = 0
  var z : Int = 0

  Given("""^a variable x with value (\d+)$""") { (arg0: Int) =>
    x = arg0
  }

  Given("""^a variable y with value (\d+)$""") { (arg0: Int) =>
    y = arg0
  }

  When("""^I multiply x \* y$""") { () =>
    z = x * y
  }

 Then("""^I get (\d+)$""") { (arg0: Int) =>
   z should be (arg0)
 }
}
```


```
> test
[info] ExampleSpec:
[info] - An empty Set should have size 0
@my-test
Feature: Multiplication
  In order to avoid making mistakes
  As a dummy
  I want to multiply numbers

  Scenario: Multiply two variables  # Multiplication.feature:7
    Given a variable x with value 2 # MultiplicationSteps.scala:44
    And a variable y with value 3   # MultiplicationSteps.scala:48
    When I multiply x * y           # MultiplicationSteps.scala:52
    Then I get 6                    # MultiplicationSteps.scala:56

1 Scenarios (1 passed)
4 Steps (4 passed)
0m0.081s


1 Scenarios (1 passed)
4 Steps (4 passed)
0m0.081s

[info] CucumberTestSuite .. passed
[info] ScalaTest
[info] Run completed in 422 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] CucumberTest
[info] Tests: succeeded 1, failed 0
[info] Passed: Total 2, Failed 0, Errors 0, Passed 2
[success] Total time: 1 s, completed 02-Apr-2017 23:16:53
>
```


[1]:	https://waffle.io/lewismj/cucumber

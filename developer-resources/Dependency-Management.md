<!-- SPDX-License-Identifier: CC-BY-4.0 -->
<!-- Copyright Contributors to the ODPi Egeria project. -->

# Dependency Management

## General rules

* Try and use standard Java & existing dependencies where possible - consider carefully if a new dependency is needed.
* Always define the dependency at the lowest level pom.xml where it's needed.
* Use a current non-beta version of a dependency.
* check build output carefully for any dependency warnings & errors.
* Do not add any exceptions to the existing rules without discussion with other maintainers.

## Understanding dependencies

running `mvn dependency:tree` is a useful way to understand what dependencies (direct and transitive) a module has.

## Adding a new Dependency

* Check if the dependency is already listed in the top level pom.
* If not add a section such as follows within the `<dependencyManagement>` section of the pom:
```
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <scope>compile</scope>
    <version>${kafka.version}</version>
</dependency>
```
This declaration only means that IF a dependency is used, these are the defaults to use - most critically including version, though scope is a useful default to add too - for example if the dependency is only for tests.

* Add the dependency to the `<dependency>` section of your POM:
```
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
        </dependency>
```
Note that the version is not included - it will be picked up from `<dependencyManagement>`.

Now build with `mvn clean install` which will include some checks for correct useage of dependencies - see below.

### More on scopes

Many dependencies will be of scope 'compile' (used for build and runtime), or 'test' (for test tools). Refer to https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope .

## Build time checks

The top level pom defines checks that are run in reference to dependencies

* the maven dependency plugin [analyze-only](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-only-mojo.html) goal is used to check that any dependencies referred to in the object code are declared as dependencies, and that any not used are not. Any discrepancies will be reported as part of the build. Occasionally exceptions may be required, generally for dependencies that are only needed at runtime.
* the maven dependency plugin [analyze-dep-mgt](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-dep-mgt-mojo.html) goal is used to check all dependencies declared are of the same version as that in dependencyManagement in the top level pom
* the maven enforcer plugin [enforce](https://maven.apache.org/enforcer/maven-enforcer-plugin/enforce-mojo.html) goal is used with  the following rules
* [reactorModuleConvergence](https://maven.apache.org/enforcer/enforcer-rules/reactorModuleConvergence.html) checks for correct parent/child relationships and inconsistency
* [requireUpperBoundDeps](https://maven.apache.org/enforcer/enforcer-rules/requireUpperBoundDeps.html) checks that minimum versions are satisified for all transitive dependencies.

If any of these checks fail an appropriate message will be displayed and the build will fail.

In some cases where incompatable versions are reported, it may be due to transitive dependencies - for example a component the egeria code doesn't depend on directly, but only indirectly. The path to resolve the version could result in different versions being used - or at least attempted, then failing. To resolve this a reference can be added in `<dependencyManagement>` to specify the version to use.

## Security scans

Egeria dependencies are scanned for potential CVEs automatically in two main ways:

* Github [scans dependencies](https://help.github.com/en/articles/about-security-alerts-for-vulnerable-dependencies) for known CVEs.
* A weekly [Nexus CLM scan](https://nexus-iq.wl.linuxfoundation.org/assets/index.html#/reports/odpi-egeria/) is run.

The maintainers will review these regularly and action any required changes through issues and pull requests.

Egeria code itself is also scanned for vulnerabilities using [Sonar](https://sonarcloud.io/dashboard?id=odpi_egeria) .

Additionally any developer can perform similar checks by running:

```mvn clean install -DfindBugs```

This will run (and create a file for each module)
 * [spotBugs](https://spotbugs.github.io/spotbugs-maven-plugin/index.html) including [findsecbugs](https://find-sec-bugs.github.io/) (spotBugsXml.xml)
 * [pmd](https://maven.apache.org/plugins/maven-pmd-plugin/) (pmd.xml)
 * [OWASP dependency checker](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/) (dependency-check-report.html)

Note that the scan may take a long time - an hour or more for all checks.
----
License: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/),
Copyright Contributors to the ODPi Egeria project.

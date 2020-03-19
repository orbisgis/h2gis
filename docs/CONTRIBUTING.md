# Contributing

Before contributing via a pull request, please create an issue describing 
your changes to start a discussion.

## Pull Request

In you pull request indicate the following information : 
 - The modified modules.
 - A description of the changes.
Before creating a pull request, ensure that :
 - Your code is :
    - Documented (source and test).
    - Covered by tests.
    - Respecting our coding style.
 - The documentation (markdown file, wiki etc) is updated.
 
### Coding style

In this section is described the coding style of this project.

#### Architecture

Each module is divided in two part, the API and the implementation.
The packages name for a module names `Spatial library` should be 
`org.orbisgis.spatiallibraryapi` and `org.orbisgis.spatiallibrary` with 
space removed. All the interfaces should start with a `I` like 
`ISpatialInterface`.

#### Classes

Each class should start with a header which template can be found 
[here](HEADER.md). The author declaration should be follow the pattern 
`@author Name Structure`.

The parameters and the methods should be annotated with the `@Nullable` or `@NotNull` annotations. The not null 
parameters should be checked with the utility method `CheckUtils.checkNotNull(Object)` or 
`CheckUtils.checkNotNull(Object, String)` (which throws an `InvalidParameterException` in case of null value) on the 
very beginning of the method.

#### Tests

Tests should written in Groovy and Java (same test for each languages) 
and all the test methods should be documented. The test classes should 
start with :
``` java
/**
 * Test class dedicated to {@link BaseClass} interface/class.
 *
 * @author ...
 * @author ...
 */
```
and all the test methods should start with :
``` java
/**
 * Test the {@link class#methodToTest1} and {@link class#methodToTest2} and ... method(s)/constructor(s).
 */
```

The test classes name should have this pattern `nameOfTheTargetClass + Test` and the test methods should have this 
pattern : `nameOfTheTargetMethod + Test`

The framework used for test is 
[JUnit 5](https://junit.org/junit5/docs/current/user-guide/). 
Each class named `AClass` should have its test class named 
`AClassTest`.
Test should follow the following pattern :
 - Initialization of the data
 - Checking using the assertions that the data are well set
 - Processing of the data with the method to test
 - Checking using the assertion the output of the method to test.

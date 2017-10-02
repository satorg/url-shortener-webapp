### URL Shortener Web Application

Technology stack used: SBT, Scala, PlayFramework with Slick database connector.
To compile and run the application:

1. Install [SBT](http://www.scala-sbt.org/download.html)

2. Go to the project folder and run:

    ```bash
    $ sbt run
    ```

3. To run tests use:

    ```bash
    $ sbt test
    ```

By default, the application uses H2 in-memory database, so no external dependencies are required to run the application.
H2 is used because it is very simple to configure with PlayFramework, and it is just enough for this sample application.

The current approach takes an URL from a user and then stores this URL into database with auto-generated ID field.
Then the created ID is used to build the short URL. The ID is base-64 encoded to hide the fact that is is just a counter/sequential number. Also it should let URLs be even shorter when bit counter values are reached.

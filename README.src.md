Getting Started: Accessing Data with GemFire
============================================

What you'll build
-----------------

This guide walks you through the process of building an application with GemFire's data fabric using the powerful Spring Data GemFire library to store and retrieve POJOs.

What you'll need
----------------

 - About 15 minutes
 - {!include#prereq-editor-jdk-buildtools}

## {!include#how-to-complete-this-guide}


<a name="scratch"></a>
Set up the project
------------------

{!include#build-system-intro}

{!include#create-directory-structure-hello}

### Create a Maven POM

    {!include:initial/pom.xml}

This guide also uses log4j with certain log levels turned up so you can see what GemFire and Spring Data GemFire are doing.

    {!include:initial/src/main/resources/log4j.properties}


<a name="initial"></a>
Define a simple entity
------------------------
GemFire is a data fabric. It maps data into regions, and it's possible to configure distributed regions across multiple nodes. However, for this guide you use a local region so you don't have to set up anything extra.

In this example, you store Person objects with a few annotations.

    {!include:complete/src/main/java/hello/Person.java}

Here you have a `Person` class with two attributes, the `name` and the `age`. You also have a single constructor to populate the entities when creating a new instance.

> Note: In this guide, the typical getters and setters have been left out for brevity.

Notice that this class is annotated `@Region("hello")`. When GemFire stores the class, a new entry is created inside that specific region. This class also has `name` marked with `@Id`. This is for internal usage to help GemFire track the data.

The next important piece is the person's age. Later in this guide, you use it to fashion some queries.

The convenient `toString()` method will print out the person's name and age.

Create simple queries
----------------------------
Spring Data GemFire focuses on storing data in GemFire. It also inherits powerful functionality from the Spring Data Commons project, such as the ability to derive queries. Essentially, you don't have to learn the query language of GemFire; you can simply write a handful of methods and the queries are written for you.

To see how this works, create an interface that queries `Person` nodes.

    {!include:complete/src/main/java/hello/PersonRepository.java}
    
`PersonRepository` extends the `CrudRepository` interface and plugs in the type of values and keys it works with: `Person` and `String`. Out-of-the-box, this interface comes with many operations, including standard CRUD (change-replace-update-delete).

You can define other queries as needed by simply declaring their method signature. In this case, you add `findByName`, which essentially seeks nodes of type `Person` and find the one that matches on `name`.

You also have:
- `findByAgeGreaterThan` to find people above a certain age
- `findByAgeLessThan` to find people below a certain age
- `findByAgeGreaterThanAndAgeLessThan` to find people in a certain range

Let's wire this up and see what it looks like!

Create an application class
---------------------------
Here you create an Application class with all the components.

    {!include:complete/src/main/java/hello/Application.java}

In the configuration, you need to add the `@EnableGemFireRepositories` annotation.

A GemFire cache is required, to store all data. For that, you have Spring Data GemFire's convenient `CacheFactoryBean`.

> **Note:** In this guide, the cache is created locally using built-in components and an evaluation license. For a production solution, Spring recommends the production version of GemFire, where you can create distributed caches and regions across multiple nodes.

Remember how you tagged `Person` to be stored in `@Region("hello")`? You define that region here with `LocalRegionFactoryBean<String, Person>`. You need to inject an instance of the cache you just defined while also naming it `hello`.

> **Note:** The types are `<String, Person>`, matching the key type (`String`) with the value type (`Person`).

Finally, you autowire an instance of `PersonRepository` that you just defined. Spring Data GemFire will dynamically create a concrete class that implements that interface and will plug in the needed query code to meet the interface's obligations.

Store and fetch data
-------------------------
The `public static void main` method includes code to create an application context and then define people.

In this case, you are creating three local `Person`s, **Alice**, **Baby Bob**, and **Teen Carol**. Initially, they only exist in memory. After creating them, you have to save them to GemFire.

Now you run several queries. The first looks up everyone by name. Then you execute a handful of queries to find adults, babies, and teens, all using the age attribute. With the logging turned up, you can see the queries Spring Data GemFire writes on your behalf.

Build the application
------------------------

To build this application, you need to add some extra bits to your pom.xml file.

```xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-shade-plugin</artifactId>
				<version>2.1</version>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>shade</goal>
						</goals>
						<configuration>
							<transformers>
								<transformer
									implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
									<mainClass>hello.Application</mainClass>
								</transformer>
							</transformers>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```

With the `maven-shade-plugin` added in, you can now generate a runnable jar file:

    mvn package
    
Run the application
-----------------------
Run your service with `java -jar` at the command line:

    java -jar target/gs-accessing-data-GemFire-complete-0.1.0.jar
    
You should see something like this (with other stuff like queries as well):
```
Before linking up with GemFire...
	Alice is 40 years old.
	Baby Bob is 1 years old.
	Teen Carol is 13 years old.
Lookup each person by name...
	Alice is 40 years old.
	Baby Bob is 1 years old.
	Teen Carol is 13 years old.
Adults (over 18):
	Alice is 40 years old.
Babies (less than 5):
	Baby Bob is 1 years old.
Teens (between 12 and 20):
	Teen Carol is 13 years old.
```

That isn't everything you'll see. With the debug levels of Spring Data GemFire turned up, you also see some mixed in log statements, giving you a glimpse of the query language used with GemFire. You'll find more about the query language in other Getting Started guides.

Summary
-------
Congratulations! You set up an embedded GemFire server, stored simple entities, and developed quick queries.

[zip]: https://github.com/springframework-meta/gs-accessing-data-GemFire/archive/master.zip

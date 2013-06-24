Getting Started: Accessing Data with Gemfire
============================================

What you'll build
-----------------

This guide will walk you through the process of building an application using Gemfire's data fabric.

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

This guide also uses log4j with certain log levels turned up so you can see what Gemfire and Spring Data are doing.

    {!include:initial/src/main/resources/log4j.properties}


<a name="initial"></a>
Defining a simple entity
------------------------
Gemfire is a data fabric. It maps data into regions. It's possible to configure distributed regions across multiple nodes, but for this guide you'll use a local region so you don't have to setup anything extra.

In this example, you'll store some Person objects with a few annotations.

    {!include:complete/src/main/java/hello/Person.java}

Here you have a `Person` class with two attributes, the `name` and the `age`. You have two constructors, an empty one as well as one for the attributes.

> In this guide, the typical getters and setters have been left out for brevity.

You'll notice this class is annotated `@Region("hello")`. When Gemfire stores it, it will result in the creation of a new entry inside that specific region. THis class also has `name` marked with `@Id`. This is for internal usage to help Gemfire track the data.

The next important piece is person's age. We'll use it to fashion some queries further down in the guide.

Finally, you have a convenient `toString()` method to print out the person's name and age.

Creating some simple queries
----------------------------
Spring Data Gemfire is focused on storing data in Gemfire. But it inherits much functionality from the Spring Data Commons project. This includes it's powerful ability to derive queries. Essentially, you don't have to learn the query language of Gemfire, but can simply write a handful of methods and the queries are written for you.

To see how this works, create an interface that is focused on querying `Person` nodes.

    {!include:complete/src/main/java/hello/PersonRepository.java}
    
`PersonRepository` extends the `CrudRepository` class and plugs in the type of the values and keys it works with: `Person` and `String`. Out-of-the-box, this interface comes with a lot of operations, including standard CRUD (change-replace-update-delete) operations.

But you can define other queries as needed by simply declaring their method signature. In this case, you added `findByName`, which essentially will seek nodes of type `Person` and find the one that matches on `name`.

You also have:
- `findByAgeGreaterThan` to find people above a certain age
- `findByAgeLessThan` to find people below a certain age
- `findByAgeGreaterThanAndAgeLessThan` to find people in a certain range

Let's wire this up and see what it looks like!

Wiring the application components
---------------------------------
You need to create an Application class with all the components.

    {!include:complete/src/main/java/hello/Application.java}

In the configuration, you need to add the `@EnableGemfireRepositories` annotation.

One piece that's required is a Gemfire cache. That is the target place to store all data. For that, you have Spring Data Gemfire's convenient `CacheFactoryBean`.

> **Note:** In this guide, the cache is created locally using built-in components and an evaluation license. For a production solution, it's recommended to get the production version where you can create distributed caches and regions that spread across multiple nodes.

Remember how you tagged `Person` to be stored in `@Region("hello")`? You need to define that region here. You can do it with the handy `LocalRegionFactoryBean<String, Person>`. We need to inject an instance of the cache we just defined while also naming it `hello`.

> **Note:** The types are `<String, Person>`, matching the key type (`String`) with the value type (`Person`).

Finally, you autowire an instance of `PersonRepository` that you just defined up above. Spring Data Gemfire will dynamically create a concrete class that implements that interface and will plugin the needed query code to meet the interface's obligations.

Storing and Fetching Data
-------------------------
The `public static void main` includes code to create an application context and then define some people.

In this case, you  are creating three local `Person`s, **Alice**, **Baby Bob**, and **Teen Carol**. Initially, they only exist in memory. After creating them, you have to save them to Gemfire.

Now you run several queries. The first looks up everyone by name. Then we execute a handful of queries to find adults, babies, and teens, all using the age attribute. With the logging turned up, you can see the queries Spring Data Gemfire writes on your behalf.

Building the Application
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

With the `maven-shade-plugin` added in, this is all you need to generate a runnable jar file.

    mvn package
    
Running the Application
-----------------------
Run your service with `java -jar` at the command line:

    java -jar target/gs-accessing-data-gemfire-complete-0.1.0.jar
    
You should see something like this (with other stuff like queries as well):
```
Before linking up with Gemfire...
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

With the debug levels of Spring Data Gemfire turned up, you are also getting a glimpse of the query language used with Gemfire. This guide won't delve into that, but if you like, you can investigate that in some of the other Getting Started Guides.

Summary
-------
Congratulations! You just setup an embedded Gemfire server, stored some simple entities, and developed some quick queries.

[zip]: https://github.com/springframework-meta/gs-accessing-data-gemfire/archive/master.zip

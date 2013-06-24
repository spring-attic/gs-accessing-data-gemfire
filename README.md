Getting Started: Accessing Data with Gemfire
============================================

What you'll build
-----------------

This guide will walk you through the process of building an application using Gemfire's data fabric.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi

How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/getting-started), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/{@project-name}.git`
 - cd into `{@project-name}/initial`
 - Jump ahead to [Create a resource representation class](#initial).

**When you're finished**, you can check your results against the code in `{@project-name}/complete`.


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Getting Started with Maven](../gs-maven/README.md) or [Getting Started with Gradle](../gs-gradle/README.md).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-acessing-data-gemfire-initial</artifactId>
    <version>0.1.0</version>

    <dependencies>
        <dependency>
        	<groupId>org.springframework.data</groupId>
        	<artifactId>spring-data-gemfire</artifactId>
        	<version>1.3.0.RELEASE</version>
        </dependency>
        <dependency>
        	 <groupId>org.slf4j</groupId>
        	 <artifactId>slf4j-log4j12</artifactId>
        	 <version>1.7.5</version>
        </dependency>
        <dependency>
        	<groupId>com.gemstone.gemfire</groupId>
        	<artifactId>gemfire</artifactId>
        	<version>7.0.1</version>
        </dependency>
    </dependencies>
    
    <!-- TODO: remove once bootstrap goes GA -->
    <repositories>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
		<repository>
			<id>gemstone</id>
			<url>http://dist.gemstone.com.s3.amazonaws.com/maven/release/</url>
		</repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

This guide also uses log4j with certain log levels turned up so you can see what Gemfire and Spring Data are doing.

`src/main/resources/log4j.properties`
```properties
# Set root logger level to DEBUG and its only appender to A1.
log4j.rootLogger=WARN, A1

# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

log4j.category.org.springframework=INFO
log4j.category.org.springframework.data.gemfire=DEBUG
```


<a name="initial"></a>
Defining a simple entity
------------------------
Gemfire is a data fabric. It maps data into regions. It's possible to configure distributed regions across multiple nodes, but for this guide you'll use a local region so you don't have to setup anything extra.

In this example, you'll store some Person objects with a few annotations.

`src/main/java/hello/Person.java`
```java
package hello;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.Region;

@Region("hello")
public class Person {

	@Id public String name;
	public int age;

	@PersistenceConstructor
	public Person(String name, int age) {
		this.name = name; 
		this.age = age;
	}
	
	@Override
	public String toString() {
		return name + " is " + age + " years old.";
	}

}
```

Here you have a `Person` class with two attributes, the `name` and the `age`. You have two constructors, an empty one as well as one for the attributes.

> In this guide, the typical getters and setters have been left out for brevity.

You'll notice this class is annotated `@Region("hello")`. When Gemfire stores it, it will result in the creation of a new entry inside that specific region. THis class also has `name` marked with `@Id`. This is for internal usage to help Gemfire track the data.

The next important piece is person's age. We'll use it to fashion some queries further down in the guide.

Finally, you have a convenient `toString()` method to print out the person's name and age.

Creating some simple queries
----------------------------
Spring Data Gemfire is focused on storing data in Gemfire. But it inherits much functionality from the Spring Data Commons project. This includes it's powerful ability to derive queries. Essentially, you don't have to learn the query language of Gemfire, but can simply write a handful of methods and the queries are written for you.

To see how this works, create an interface that is focused on querying `Person` nodes.

`src/main/java/hello/PersonRepository.java`
```java
package hello;

import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, String> {
	
	Person findByName(String name);

	Iterable<Person> findByAgeGreaterThan(int age);
	
	Iterable<Person> findByAgeLessThan(int age);
	
	Iterable<Person> findByAgeGreaterThanAndAgeLessThan(int age1, int age2);
}
```
    
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

`src/main/java/hello/Application.java`
```java
package hello;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import com.gemstone.gemfire.cache.GemFireCache;

@Configuration
@EnableGemfireRepositories
public class Application {
	
	@Bean
	CacheFactoryBean cacheFactoryBean() {
		return new CacheFactoryBean();
	}
	
	@Bean
	LocalRegionFactoryBean<String, Person> localRegionFactory(final GemFireCache cache) {
		return new LocalRegionFactoryBean<String, Person>() {{
			setCache(cache);
			setName("hello");
		}};
	}
	
	@Autowired
	PersonRepository repository;

	public static void main(String[] args) throws IOException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
		
		Person alice = new Person("Alice", 40);
		Person bob = new Person("Baby Bob", 1);
		Person carol = new Person("Teen Carol", 13);
		
		System.out.println("Before linking up with Gemfire...");
		for (Person person : new Person[]{alice, bob, carol}) {
			System.out.println("\t" + person);
		}
		
		PersonRepository personRepository = ctx.getBean(PersonRepository.class);
				
		personRepository.save(alice);
		personRepository.save(bob);
		personRepository.save(carol);
		
		System.out.println("Lookup each person by name...");
		for (String name: new String[]{alice.name, bob.name, carol.name}) {
			System.out.println("\t" + personRepository.findByName(name));
 		}
		
		System.out.println("Adults (over 18):");
		for (Person person : personRepository.findByAgeGreaterThan(18)) {
			System.out.println("\t" + person);
		}
		
		System.out.println("Babies (less than 5):");
		for (Person person : personRepository.findByAgeLessThan(5)) {
			System.out.println("\t" + person);
		}

		System.out.println("Teens (between 12 and 20):");
		for (Person person : personRepository.findByAgeGreaterThanAndAgeLessThan(12, 20)) {
			System.out.println("\t" + person);
		}

		ctx.close();
		
	}
	
}
```

In the configuration, you need to add the `@EnableGemfireRepositories` annotation.

One piece that's missing is a Gemfire cache. That is the target place to store all data. For that, you have Spring Data Gemfire's convenient `CacheFactoryBean`.

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
Congratulations! You just setup an embedded Gemfire server, stored some simple, related entities, and developed some quick queries.

[zip]: https://github.com/springframework-meta/gs-accessing-data-gemfire/archive/master.zip

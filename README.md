Getting Started: Accessing Data with GemFire
============================================

What you'll build
-----------------

This guide walks you through the process of building an application with GemFire's data fabric using the powerful Spring Data GemFire library to store and retrieve POJOs.

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

This guide also uses log4j with certain log levels turned up so you can see what GemFire and Spring Data GemFire are doing.

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
Define a simple entity
------------------------
GemFire is a data fabric. It maps data into regions, and it's possible to configure distributed regions across multiple nodes. However, for this guide you use a local region so you don't have to set up anything extra.

In this example, you store Person objects with a few annotations.

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

Here you have a `Person` class with two attributes, the `name` and the `age`. You also have a single constructor to populate the entities when creating a new instance.

> Note: In this guide, the typical getters and setters have been left out for brevity.

Notice that this class is annotated `@Region("hello")`. When GemFire stores the class, a new entry is created inside that specific region. This class also has `name` marked with `@Id`. This is for internal usage to help GemFire track the data.

The next important piece is the person's age. Later in this guide, you use it to fashion some queries.

The convenient `toString()` method will print out the person's name and age.

Create simple queries
----------------------------
Spring Data GemFire focuses on storing data in GemFire. It also inherits powerful functionality from the Spring Data Commons project, such as the ability to derive queries. Essentially, you don't have to learn the query language of GemFire; you can simply write a handful of methods and the queries are written for you.

To see how this works, create an interface that queries `Person` nodes.

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

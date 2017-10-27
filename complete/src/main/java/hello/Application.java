package hello;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

@PeerCacheApplication(name = "DataGemFireApplication", logLevel = "error")
@EnableEntityDefinedRegions
@EnableGemfireRepositories
@SuppressWarnings("unused")
public class Application {

    public static void main(String[] args) throws IOException {
        new SpringApplicationBuilder(Application.class).web(false).run(args);
    }

    @Bean
    ApplicationRunner run(PersonRepository personRepository) {

        return args -> {

            Person alice = new Person("Alice", 40);
            Person bob = new Person("Baby Bob", 1);
            Person carol = new Person("Teen Carol", 13);

            System.out.println("Before accessing data in GemFire...");

            asList(alice, bob, carol).forEach(person -> System.out.println("\t" + person));

            personRepository.save(alice);
            personRepository.save(bob);
            personRepository.save(carol);

            System.out.println("Lookup each person by name...");

            asList(alice.getName(), bob.getName(), carol.getName())
              .forEach(name -> System.out.println("\t" + personRepository.findByName(name)));

            System.out.println("Adults (over 18):");

            stream(personRepository.findByAgeGreaterThan(18).spliterator(), false)
              .forEach(person -> System.out.println("\t" + person));

            System.out.println("Babies (less than 5):");

            stream(personRepository.findByAgeLessThan(5).spliterator(), false)
              .forEach(person -> System.out.println("\t" + person));

            System.out.println("Teens (between 12 and 20):");

            stream(personRepository.findByAgeGreaterThanAndAgeLessThan(12, 20).spliterator(), false)
              .forEach(person -> System.out.println("\t" + person));
        };
    }
}

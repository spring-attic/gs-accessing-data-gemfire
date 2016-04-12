package hello;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import com.gemstone.gemfire.cache.GemFireCache;

@Configuration
@EnableGemfireRepositories
@SuppressWarnings("unused")
public class Application implements CommandLineRunner {

    @Bean
    Properties gemfireProperties() {
        Properties gemfireProperties = new Properties();
        gemfireProperties.setProperty("name", "DataGemFireApplication");
        gemfireProperties.setProperty("mcast-port", "0");
        gemfireProperties.setProperty("log-level", "config");
        return gemfireProperties;
    }

    @Bean
    CacheFactoryBean gemfireCache() {
        CacheFactoryBean gemfireCache = new CacheFactoryBean();
        gemfireCache.setClose(true);
        gemfireCache.setProperties(gemfireProperties());
        return gemfireCache;
    }

    @Bean
    LocalRegionFactoryBean<String, Person> helloRegion(final GemFireCache cache) {
        LocalRegionFactoryBean<String, Person> helloRegion = new LocalRegionFactoryBean<>();
        helloRegion.setCache(cache);
        helloRegion.setClose(false);
        helloRegion.setName("hello");
        helloRegion.setPersistent(false);
        return helloRegion;
    }

    @Autowired
    PersonRepository personRepository;

    @Override
    public void run(String... strings) throws Exception {
        Person alice = new Person("Alice", 40);
        Person bob = new Person("Baby Bob", 1);
        Person carol = new Person("Teen Carol", 13);

        System.out.println("Before linking up with Gemfire...");
        for (Person person : new Person[] { alice, bob, carol }) {
            System.out.println("\t" + person);
        }

        personRepository.save(alice);
        personRepository.save(bob);
        personRepository.save(carol);

        System.out.println("Lookup each person by name...");
        for (String name : new String[] { alice.name, bob.name, carol.name }) {
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
    }

    public static void main(String[] args) throws IOException {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebEnvironment(false);
        application.run(args);
    }

}

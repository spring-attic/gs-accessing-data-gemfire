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

package centennial.comp231.smartresumebackend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import centennial.comp231.smartresumebackend.POJO.RegistrationInfo;
import centennial.comp231.smartresumebackend.repos.RegistrationRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmartResumeBackendApplicationTests {

	@Autowired
    private RegistrationRepository userRepository;
	
	 @Before
	    public void setUp() throws Exception {
	        RegistrationInfo user1= new RegistrationInfo("alice@gmail.com", "Alice", "Applicant");
	        RegistrationInfo user2= new RegistrationInfo("Bob@gmail.com", "Bob", "Applicant");
	        //save user, verify has ID value after save
	        assertNull(user1.getEmail());
	        assertNull(user2.getEmail());//null before save
	        this.userRepository.save(user1);
	        this.userRepository.save(user2);
	        assertNotNull(user1.getEmail());
	        assertNotNull(user2.getEmail());
	    }
	 
	@Test
	public void testFetchData() {
		/*Test data retrieval*/
        RegistrationInfo userA = userRepository.findByEmail("Bob@gmail.com");
        assertNotNull(userA);
        assertEquals("Bob@gmail.com", userA.getEmail());
        /*Get all products, list should only have two*/
        Iterable<RegistrationInfo> users = userRepository.findAll();
        int count = 0;
        for(@SuppressWarnings("unused") RegistrationInfo x : users){
            count++;
        }
        assertEquals(count, 2);
    }

}


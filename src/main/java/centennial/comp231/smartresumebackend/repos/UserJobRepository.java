package centennial.comp231.smartresumebackend.repos;
import org.springframework.data.repository.CrudRepository;
import centennial.comp231.smartresumebackend.POJO.UserJob;

public interface UserJobRepository extends CrudRepository<UserJob,  Long> {
	UserJob findByEmail(String email);
	UserJob findByJobId(long jobId);
	UserJob findById(long id);
	UserJob findByEmailAndJobId(String email, long jobId);
}

package centennial.comp231.smartresumebackend.repos;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import centennial.comp231.smartresumebackend.POJO.UserJob;

public interface UserJobRepository extends CrudRepository<UserJob,  Long> {
	List<UserJob> findByEmail(String email);
	List<UserJob> findByJobId(long jobId);
	UserJob findById(long id);
	UserJob findByEmailAndJobId(String email, long jobId);
}

package centennial.comp231.smartresumebackend.repos;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import centennial.comp231.smartresumebackend.POJO.Job;

public interface JobRepository extends CrudRepository<Job, Long> {
	Job findByJobId(long id);
	List<Job> findByJobTitle(long id);
	List<Job> findByCompanyName(String companyName);
	List<Job> findByPostedUserEmail(String jobPostingPosterUserEmail);
    List<Job> findByPostingDate (String jobPostingDate);
    List<Job> findByPostingExpiryDate (String postingExpiryDate);
    
    @Query("SELECT j FROM Job j")
    List<Job> findAllJobs();
    
    List<Job> findByPostingExpiryDateGreaterThanEqual(String todayDate);
    
    @Query(value = "SELECT * FROM Job LEFT JOIN UserJob ON Job.jobId=UserJob.jobId WHERE UserJob.email = :email", nativeQuery = true)
    List<Job> findByJoinEmailAddress(@Param("email") String email);
}

package centennial.comp231.smartresumebackend.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import centennial.comp231.smartresumebackend.POJO.CandidateProfile;
import centennial.comp231.smartresumebackend.POJO.Job;
import centennial.comp231.smartresumebackend.repos.IQueryService;
import centennial.comp231.smartresumebackend.repos.JobRepository;
import centennial.comp231.smartresumebackend.repos.UserJobRepository;

@Service
public class QueryService implements IQueryService {

	@Autowired
	JobRepository jobRepository;
	@Autowired
	UserJobRepository userJobRepository;
	@Autowired
    EntityManagerFactory emf;

	@Override
	public List<Job> findJobsAppliedByJoinJobAndUserJob(String email) {
		EntityManager em = emf.createEntityManager();
		Query query = em.createQuery("Select j from Job j inner join UserJob uj on j.jobId=uj.jobId where uj.email=:email");
		query.setParameter("email", email);
		@SuppressWarnings("unchecked")
        List<Job> list =(List<Job>)query.getResultList();
        System.out.println("Student Name :");
        em.close();
        return list;
	}

	@Override
	public List<CandidateProfile> findCandidatesAppliedByJoinCandidateProfileAndUserJob(long jobId) {
		EntityManager em = emf.createEntityManager();
		Query query = em.createQuery("Select c from CandidateProfile c inner join UserJob uj on c.email=uj.email where uj.jobId="+jobId);
		//query.setParameter("jobid", jobId);
		@SuppressWarnings("unchecked")
        List<CandidateProfile> list =(List<CandidateProfile>)query.getResultList();
        em.close();
        return list;
	}
}

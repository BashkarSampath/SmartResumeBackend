package centennial.comp231.smartresumebackend.repos;

import java.util.List;

import centennial.comp231.smartresumebackend.POJO.CandidateProfile;
import centennial.comp231.smartresumebackend.POJO.Job;

public interface IQueryService {
    List<Job> findJobsAppliedByJoinJobAndUserJob(String email);
    List<CandidateProfile> findCandidatesAppliedByJoinCandidateProfileAndUserJob(long jobId);
}

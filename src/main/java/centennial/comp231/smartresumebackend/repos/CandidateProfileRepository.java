package centennial.comp231.smartresumebackend.repos;

import org.springframework.data.repository.CrudRepository;

import centennial.comp231.smartresumebackend.POJO.CandidateProfile;
import centennial.comp231.smartresumebackend.POJO.UserJob;

public interface CandidateProfileRepository extends CrudRepository<CandidateProfile, UserJob> {
    CandidateProfile findByName(String name);
    CandidateProfile findByEmail(String email);
}

package centennial.comp231.smartresumebackend.repos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import centennial.comp231.smartresumebackend.POJO.RegistrationInfo;

public interface RegistrationRepository extends CrudRepository<RegistrationInfo,  Long> {
	  RegistrationInfo findByEmail(String email);
	  RegistrationInfo findById(long id);
	  List<RegistrationInfo> findByRole(String role);
}
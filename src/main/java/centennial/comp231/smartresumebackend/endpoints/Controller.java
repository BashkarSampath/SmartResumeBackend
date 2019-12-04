package centennial.comp231.smartresumebackend.endpoints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import centennial.comp231.smartresumebackend.POJO.CandidateProfile;
import centennial.comp231.smartresumebackend.POJO.Job;
import centennial.comp231.smartresumebackend.POJO.RegistrationInfo;
import centennial.comp231.smartresumebackend.POJO.Response;
import centennial.comp231.smartresumebackend.POJO.UserJob;
import centennial.comp231.smartresumebackend.repos.CandidateProfileRepository;
import centennial.comp231.smartresumebackend.repos.JobRepository;
import centennial.comp231.smartresumebackend.repos.RegistrationRepository;
import centennial.comp231.smartresumebackend.repos.UserJobRepository;
import centennial.comp231.smartresumebackend.service.AppPDFParser;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class Controller {

	@Autowired
	RegistrationRepository registrationRepository;

	@Autowired
	CandidateProfileRepository profileRepository;

	@Autowired
	UserJobRepository userJobRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	AppPDFParser appPDFParser;
	//	
	//	@Autowired
	//    private HttpServletRequest request;

	Map<String, RegistrationInfo> userMap = new HashMap<>();
	Map<String, List<Job>> userJobMap = new HashMap<>();
	Map<String, Job> allJobs = new HashMap<>();
	Map<String, CandidateProfile> candidateProfileMap = new HashMap<>();
	Gson gson = new Gson();
	static int globalJobId = 1;

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String registerCandidate(@RequestBody String payload) {
		RegistrationInfo registrationInfo = gson.fromJson(payload, RegistrationInfo.class);
		RegistrationInfo existingUser = registrationRepository.findByEmail(registrationInfo.getEmail().toLowerCase());
		if (existingUser!=null) {
			Response response = new Response(null, "User: " + registrationInfo.getEmail() + " already registered. Please use reset password if you forgot your password");
			return gson.toJson(response);
		} else {
			registrationInfo.setEmail(registrationInfo.getEmail().toLowerCase());
			registrationRepository.save(registrationInfo);
			CandidateProfile candidate = new CandidateProfile();
			candidate.setEmail(registrationInfo.getEmail());
			profileRepository.save(candidate);
			Response response = new Response(registrationInfo, "Registration Successful!");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginCandidate(@RequestBody String payload) {
		RegistrationInfo loginInfo = gson.fromJson(payload, RegistrationInfo.class);
		RegistrationInfo existingUser = registrationRepository.findByEmail(loginInfo.getEmail().toLowerCase());
		if(existingUser!=null)
		{
			log.info("User found");
			if(existingUser.getPassword().equals(loginInfo.getPassword())) {
				log.info("Password match success");
				Response response = new Response(existingUser, "User: " + loginInfo.getEmail() + " login successful.");
				return gson.toJson(response);
			} else {
				Response response = new Response(null, "User: " + loginInfo.getEmail() + " provided incorrect password.");
				return gson.toJson(response);
			}
		}
		else {
			Response response = new Response(null, "User: " + loginInfo.getEmail() + " is not registered. Please use Sign Up to register first.");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/deleteaccount", method = RequestMethod.DELETE)
	public String deleteAccount(@RequestBody String payload) {
		RegistrationInfo deleteAcc = gson.fromJson(payload, RegistrationInfo.class);
		CandidateProfile existingProfile = profileRepository.findByEmail(deleteAcc.getEmail().toLowerCase());
		RegistrationInfo existingRegistry = registrationRepository.findByEmail(deleteAcc.getEmail().toLowerCase());
		if(existingRegistry!=null)
		{
			registrationRepository.delete(existingRegistry);
			log.info("Login Registry Deleted");
			if(existingProfile!=null) {
				profileRepository.delete(existingProfile);
				log.info("Profile record deleted");
			}
			Response response = new Response(null, "User: " + deleteAcc.getEmail() + " has been deleted.");
			return gson.toJson(response);
		} else {
			Response response = new Response(null, "User: " + deleteAcc.getEmail() + " is not registered.");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/updatepassword", method = RequestMethod.PUT)
	public String updatePassword(@RequestBody String payload) {
		RegistrationInfo updatePass = gson.fromJson(payload, RegistrationInfo.class);
		RegistrationInfo existingRegistry = registrationRepository.findByEmail(updatePass.getEmail().toLowerCase());
		if (existingRegistry!=null) {
			existingRegistry.setPassword(updatePass.getPassword());
			registrationRepository.save(existingRegistry);
			Response response = new Response(userMap.get(updatePass.getEmail()), "Password updated successfully!");
			return gson.toJson(response);
		} else {
			Response response = new Response(null, "User: " + updatePass.getEmail() + " is not registered.");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/updateProfile", method = RequestMethod.POST)
	public String updateProfile(@RequestBody String payload) {
		CandidateProfile candidateProfile = gson.fromJson(payload, CandidateProfile.class);
		RegistrationInfo existingRegistry = registrationRepository.findByEmail(candidateProfile.getEmail().toLowerCase());
		CandidateProfile existingProfile = profileRepository.findByEmail(existingRegistry.getEmail().toLowerCase());
		if (existingRegistry!=null && existingProfile!=null) {
			existingProfile.setEmail(candidateProfile.getEmail()!=null? candidateProfile.getEmail().toLowerCase(): existingProfile.getEmail());
			existingProfile.setName(candidateProfile.getName()!=null? candidateProfile.getName().toLowerCase(): existingProfile.getName());
			existingProfile.setPhone(candidateProfile.getPhone()!=null? candidateProfile.getPhone().toLowerCase(): existingProfile.getPhone());
			existingProfile.setAddress(candidateProfile.getAddress()!=null? candidateProfile.getAddress().toLowerCase(): existingProfile.getAddress());			
			Response response = new Response(profileRepository.save(existingProfile), "User: " + existingProfile.getEmail() + "'s profile registered.");
			return gson.toJson(response);
		} else {
			Response response = new Response(null, "User: " + candidateProfile.getEmail() + " is not registered.");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/getprofile", method = RequestMethod.POST)
	public String getProfile(@RequestBody String payload) {
		RegistrationInfo registrationInfo = gson.fromJson(payload, RegistrationInfo.class);
		CandidateProfile existingProfile = profileRepository.findByEmail(registrationInfo.getEmail().toLowerCase());
		if (existingProfile!=null) {
			Response response = new Response(existingProfile, null);
			return gson.toJson(response);
		} else {
			Response response = new Response(null, "User: " + registrationInfo.getEmail() + " is not registered.");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/addjob", method = RequestMethod.POST)
	public String addJob(@RequestBody String payload) {
		Job job = gson.fromJson(payload, Job.class);
		if (job!=null) {
			try {
				jobRepository.save(job);
				Response response = new Response(job, "Job successfully added.");
				return gson.toJson(response);
			}catch (TransactionSystemException e) {
				Response response = new Response(job, "All fields required");
				return gson.toJson(response);
			}
			catch(Exception ex) {
				Response response = new Response(job, ex.toString());
				return gson.toJson(response);
			}
		} else {
			Response response = new Response(job, "All fields required");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/viewjobs", method = RequestMethod.POST)
	public String viewJobs() {
		//Set<String> jobIds = allJobs.keySet();
		List<Job> allJobList =jobRepository.findAllJobs();
		if(allJobList!=null) {
			Response response = new Response(allJobList,"");
			return gson.toJson(response);
		}
		else {
			Response response = new Response(null,"No job found");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/applyjob", method = RequestMethod.POST)
	public String applyJob(@RequestBody String payload) {
		UserJob userJob = gson.fromJson(payload, UserJob.class);
		if(userJob!=null && jobRepository.findByJobId(userJob.getJobId()) !=null) {
			UserJob appliedJob = userJobRepository.findByEmailAndJobId(userJob.getEmail(), userJob.getJobId());
			if (appliedJob==null) {
				Response response = new Response(userJobMap.get(userJob.getEmail()), "Congratulations! You have applied to " + allJobs.get(userJob.getJobId()).getJobTitle());
				return gson.toJson(response);
			}
			else{
				Response response = new Response(null, "You have already applied to this Job!");
				return gson.toJson(response);
			}
		} else {
			Response response = new Response(userJob, "Invalid job id or user email");
			return gson.toJson(response);
		}
	}

@RequestMapping(value = "/appliedjobs", method = RequestMethod.POST)
public String appliedJobs(@RequestBody String payload) {
	RegistrationInfo registrationInfo = gson.fromJson(payload, RegistrationInfo.class);
	if (userMap.containsKey(registrationInfo.getEmail())) {
		List<Job> jobsForUser = userJobMap.get(registrationInfo.getEmail());
		return gson.toJson(jobsForUser);
	} else {
		Response response = new Response(registrationInfo.getEmail(), "Email not registered");
		return gson.toJson(response);
	}
}

@RequestMapping(value = "/candidatesforjob", method = RequestMethod.POST)
public String candidatesForJob(@RequestBody String payload) {
	Job job = gson.fromJson(payload, Job.class);
	if (allJobs.containsKey(job.getJobId())) {
		List<CandidateProfile> candidatesAppliedForJob = new ArrayList<>();
		Set<String> allUsersWhoAppliedAnyJob = userJobMap.keySet();
		for (String userEmail : allUsersWhoAppliedAnyJob) {
			List<Job> jobsForUser = userJobMap.get(userEmail);
			for (Job currentJob : jobsForUser) {
				//					if (currentJob.getJobId().equals(job.getJobId())) {
				//						candidatesAppliedForJob.add(candidateProfileMap.get(userEmail));
				//					}
			}
		}
		return gson.toJson(candidatesAppliedForJob);
	} else {
		Response response = new Response(job.getJobId(), "Job ID does not exist");
		return gson.toJson(response);
	}
}


@GetMapping("/")
public ResponseEntity<?> home(){
	return new ResponseEntity<Object>("Home", HttpStatus.OK);
}


@RequestMapping(value="/multipleFilesUpload" , method=RequestMethod.POST, 
consumes="multipart/form-data", produces="application/json")  
public ResponseEntity<?> mutipleFileUpload(HttpServletRequest req, 
		@RequestParam(value="file" , required = false) MultipartFile[] files) throws IOException{
	for (MultipartFile file : files) {
		File f= new File(file.getOriginalFilename());
		try {
			System.err.println(f.getName());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	return null;
}

@RequestMapping(value="/uploadResume" , method=RequestMethod.POST, 
consumes="multipart/form-data", produces="application/json")  
public ResponseEntity<?> uploadResume(HttpServletRequest req, 
		@RequestParam(value="file" , required = false) MultipartFile file) throws Exception{
	try {
		System.err.println(file.getOriginalFilename());
		String path = transferFileAndReturnPath(file);
		return new ResponseEntity<String>("This is mock. Will be uploaded to " +path, HttpStatus.OK);
	} catch (IllegalStateException e) {
		e.printStackTrace();
	}
	return null;
}

private String transferFileAndReturnPath(MultipartFile file) throws Exception {
	if (!file.isEmpty()) {
		try {
			String uploadsDir = "\\uploads\\";

			//gets System TEMP path
			//String realPathtoUploads =  request.getServletContext().getRealPath(uploadsDir);

			String realPathtoUploads =  Paths.get("").toAbsolutePath().toString()+uploadsDir;
			if(! new File(realPathtoUploads ).exists())
			{
				new File(realPathtoUploads).mkdir();
			}

			log.info("realPathtoUploads = {}", realPathtoUploads);
			String orgName = file.getOriginalFilename();
			String filePath = realPathtoUploads + orgName;
			//File dest = new File(filePath);
			//file.transferTo(dest);
			return filePath;
		}catch(Exception ex) {
			log.error(ex.toString());
			throw ex;
		}}
	else
		throw new Exception("No files selected");
}

//	@RequestMapping(value="/uploadJobDescription" , method=RequestMethod.POST, 
//			consumes="multipart/form-data", produces="application/json")  
//	public ResponseEntity<?> uploadJobDescription(HttpServletRequest req, 
//			@RequestParam(value="file" , required = false) MultipartFile file) throws Exception{
//		try {
//			System.err.println(file.getOriginalFilename());
//			return new ResponseEntity<>(appPDFParser.readPDF(file), HttpStatus.OK);
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}





}

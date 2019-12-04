package centennial.comp231.smartresumebackend.endpoints;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

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
import centennial.comp231.smartresumebackend.service.QueryService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
	QueryService queryService;
	
	@Autowired
	AppPDFParser appPDFParser;

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
			Response response = new Response(null,"No jobs found");
			return gson.toJson(response);
		}
	}

	@RequestMapping(value = "/applyjob", method = RequestMethod.POST)
	public String applyJob(@RequestBody String payload) {
		UserJob userJob = gson.fromJson(payload, UserJob.class);
		Job availableJob = jobRepository.findByJobId(userJob.getJobId());
		if(userJob!=null && userJob.getEmail().trim()!=""&&  userJob.getJobId()!=0 && availableJob !=null) {
			UserJob appliedJob = userJobRepository.findByEmailAndJobId(userJob.getEmail(), userJob.getJobId());
			if (appliedJob==null) {
				UserJob newApplication = new UserJob();
				newApplication.setEmail(userJob.getEmail().toLowerCase().trim());
				newApplication.setJobId(userJob.getJobId());
				userJobRepository.save(newApplication);
				Response response = new Response(userJobMap.get(userJob.getEmail()), 
						"Congratulations! You have applied to " 
								+availableJob.getJobTitle() +
								" with id " + availableJob.getJobId());
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
	if (registrationInfo!=null && registrationInfo.getEmail().trim()!="") {
		List<Job> appliedJobs = queryService.findJobsAppliedByJoinJobAndUserJob(registrationInfo.getEmail().toLowerCase().trim());
		return gson.toJson(new Response(appliedJobs,""));
	} else {
		Response response = new Response(registrationInfo.getEmail(), "Email not registered");
		return gson.toJson(response);
	}
}

@RequestMapping(value = "/candidatesforjob", method = RequestMethod.POST)
public String candidatesForJob(@RequestBody String payload) {
	UserJob userjob = gson.fromJson(payload, UserJob.class);
	Job jobExists = null;
	if(userjob!=null && userjob.getJobId()!=0)
	{
		jobExists = jobRepository.findByJobId(userjob.getJobId());
		if(jobExists!=null) {
			List<CandidateProfile> candidatesAppliedForJob = queryService.findCandidatesAppliedByJoinCandidateProfileAndUserJob(userjob.getJobId());
			return gson.toJson(candidatesAppliedForJob);
		}else {
			Response response = new Response(userjob.getJobId(), "No one applied for the job id");
			return gson.toJson(response);
		}	
	} else {
		Response response = new Response(userjob.getJobId(), "Job ID does not exist");
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

@Autowired
CloudBlobContainer cloudBlobContainer;

@RequestMapping(value="/uploadResume" , method=RequestMethod.POST,
consumes="multipart/form-data", produces="application/json")
public ResponseEntity<?> uploadResume(HttpServletRequest req, 
		@RequestParam(value="file" , required = true) MultipartFile file, @RequestParam(value="email" , required = true) String email) throws Exception{
	URI uri = null;
	CloudBlockBlob blob = null;
	CandidateProfile candidate = profileRepository.findByEmail(email.toLowerCase().trim());
	try {
		String ofilename = email.toLowerCase().trim().replace("@", "").replace(".", "").concat(".pdf"); //replaced for unique file name file.getOriginalFilename();
		System.err.println(ofilename);
		blob = cloudBlobContainer.getBlockBlobReference(ofilename);
		blob.upload(file.getInputStream(), -1);
		uri = blob.getUri();
		candidate.setResumeLink(uri.toASCIIString());
		profileRepository.save(candidate);
	} catch (URISyntaxException e) {
		e.printStackTrace();
	} catch (StorageException e) {
		e.printStackTrace();
	}catch (IOException e) {
		e.printStackTrace();
	}
	return new ResponseEntity<String>("{\"path\":\""+uri.toASCIIString()+"\"}", HttpStatus.OK);
}

@AllArgsConstructor
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode @ToString
class ScoreRequest {  
	 public List<ScoreRequestItem> ScoreRequestItems;
}

@AllArgsConstructor
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode @ToString
class ScoreRequestItem{
	String email;
	String resumePath;
	long jobId;
}

@Autowired
RestTemplate restTemplate;

@PostMapping("/getScoresForJob")
public ResponseEntity<?> getApplicantScoreForThisJob(@RequestBody String payload){
	Job job =  gson.fromJson(payload, Job.class);
	if(job!=null && job.getJobId()!=0  && job.getPostedUserEmail()!=null &&job.getPostedUserEmail().trim()!="") {
		Job jobInDb = jobRepository.findByJobId(job.getJobId());
		if(jobInDb!=null && jobInDb.getPostedUserEmail().trim().compareToIgnoreCase(job.getPostedUserEmail().trim().toLowerCase())==0) {
			List<CandidateProfile> candidateProfiles = queryService.findCandidatesAppliedByJoinCandidateProfileAndUserJob(jobInDb.getJobId());
			if(candidateProfiles!=null) {
				ScoreRequest scoreRequest = new ScoreRequest();
				scoreRequest.ScoreRequestItems = new ArrayList<ScoreRequestItem>();
				for(CandidateProfile cprofile: candidateProfiles) {
					ScoreRequestItem item = new ScoreRequestItem(cprofile.getEmail(), cprofile.getResumeLink(), jobInDb.getJobId());
					scoreRequest.ScoreRequestItems.add(item);
				}
				String requestJson = new Gson().toJson(scoreRequest);
				System.out.println(requestJson);
				try {
					//String url = "http://127.0.0.1:5000/api/getscores";
					String url = "https://team6py.pythonanywhere.com/api/getscores";
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
					String answer = restTemplate.postForObject(url, entity, String.class);
					System.out.println(answer);

					return new ResponseEntity<String>(answer, HttpStatus.OK); 
				}catch (Exception ex) {
					// TODO: handle exception
					return new ResponseEntity<String>(ex.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				}

			}
			else {
				Response response = new Response(null, "No Applicants for this job");
				return new ResponseEntity<String>(gson.toJson(response), HttpStatus.OK); 
			}
		}else {
			Response response = new Response(null, "Job Id does not exist or wrong email provided");
			return new ResponseEntity<String>(gson.toJson(response), HttpStatus.OK); 
		}
	}
	else {
		Response response = new Response(null, "Information not enough");
		return new ResponseEntity<String>(gson.toJson(response), HttpStatus.OK); 
	}
	//return null;
}
//@RequestMapping(value="/uploadResume" , method=RequestMethod.POST, 
//consumes="multipart/form-data", produces="application/json")  
//void n() {
//	try {
//		URI uri = new UploadBlob().upload(file);
//		System.err.println(file.getOriginalFilename());
//		System.out.println("URI: "+ uri);
//		String path = transferFileAndReturnPath(file);
//		return new ResponseEntity<String>("This is mock. Will be uploaded to " +path+" with uri "+uri, HttpStatus.OK);
//	} catch (IllegalStateException e) {
//		e.printStackTrace();
//	}
//	return null;
//}

//private String transferFileAndReturnPath(MultipartFile file) throws Exception {
//	if (!file.isEmpty()) {
//		try {
//			String uploadsDir = "\\uploads\\";
//
//			//gets System TEMP path
//			//String realPathtoUploads =  request.getServletContext().getRealPath(uploadsDir);
//
//			String realPathtoUploads =  Paths.get("").toAbsolutePath().toString()+uploadsDir;
//			if(! new File(realPathtoUploads ).exists())
//			{
//				new File(realPathtoUploads).mkdir();
//			}
//
//			log.info("realPathtoUploads = {}", realPathtoUploads);
//			String orgName = file.getOriginalFilename();
//			String filePath = realPathtoUploads + orgName;
//			//File dest = new File(filePath);
//			//file.transferTo(dest);
//			return filePath;
//		}catch(Exception ex) {
//			log.error(ex.toString());
//			throw ex;
//		}}
//	else
//		throw new Exception("No files selected");
//}

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

package centennial.comp231.smartresumebackend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Async;
import org.springframework.web.multipart.MultipartFile;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UploadBlob implements WatchingInputStream.ProgressListener {
	//	 public void upload() throws Exception
	//	    {
	//	        CloudStorageAccount account = CloudStorageAccount.parse("DefaultEndpointsProtocol=https;AccountName= ....");
	//	        CloudBlobClient client = account.createCloudBlobClient();
	//	        CloudBlobContainer container = client.getContainerReference("files");
	//	        CloudBlockBlob blob = container.getBlockBlobReference("template_62269_xQ0qqGGG71BB3y99n11x.zip");
	//	    
	//		    File sourceFile = new File("/Users/dino/Downloads/template_62269_xQ0qqGGG71BB3y99n11x.zip");
	//	        FileInputStream inputStream = new FileInputStream(sourceFile);
	//	       
	//	        blob.upload(watchingInputStream, sourceFile.length());
	//	    }

	@Autowired
	CloudBlobContainer cloudBlobContainer;

	public URI upload(MultipartFile multipartFile){
		URI uri = null;
		CloudBlockBlob blob = null;
		try {
			String ofilename = multipartFile.getOriginalFilename();
			System.err.println(ofilename);
			blob = cloudBlobContainer.getBlockBlobReference(ofilename);
			blob.upload(multipartFile.getInputStream(), -1);
			uri = blob.getUri();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return uri;
	}

	@Override
	public void onAdvance(long at, long length) {
		double percentage = (double)at / (double)length;
		System.out.println(percentage);
	}
}

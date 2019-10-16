package centennial.comp231.smartresumebackend.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class AppConfig {
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver()
	{
	    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
	    multipartResolver.setMaxUploadSize(20848820);
	    return multipartResolver;
	}
	
	
}

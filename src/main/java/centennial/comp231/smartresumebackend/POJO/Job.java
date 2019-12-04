package centennial.comp231.smartresumebackend.POJO;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@AllArgsConstructor
@Entity
public class Job {
	
	@Id
	@Column(unique = true, nullable = false,length = 8)
	@GeneratedValue(strategy=GenerationType.AUTO)
    private long jobId;
	@NotNull
    private String jobTitle;
	@NotNull
    private String jobDescription;
	@NotNull
    private String companyName;
	@NotNull
    private String postedUserEmail;
	@NotNull
    private String postingDate;
	@Nullable
    private String postingExpiryDate;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}

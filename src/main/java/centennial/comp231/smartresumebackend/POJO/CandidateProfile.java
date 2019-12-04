package centennial.comp231.smartresumebackend.POJO;

import java.io.Serializable;

import javax.persistence.*;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CandidateProfile implements Serializable {	
	private static final long serialVersionUID = 1L;
	@Id
    private String email;
    private String name;
    private String address;
    private String phone;
    private String resumeLink;
    private String score;
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

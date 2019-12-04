package centennial.comp231.smartresumebackend.POJO;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @EqualsAndHashCode @Getter @Setter
@AllArgsConstructor
@Entity
@Table(name = "UserJob")
public class UserJob implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@Column(unique = true, nullable = false,length = 8)
	@GeneratedValue(strategy=GenerationType.AUTO)
	long id;
	@NotNull
    private String email;
	@NotNull
    private long jobId;
 
	@Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
package centennial.comp231.smartresumebackend.POJO;

import java.io.Serializable;

import javax.persistence.Entity;
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

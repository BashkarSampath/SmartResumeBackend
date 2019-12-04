package centennial.comp231.smartresumebackend.POJO;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
@Getter @Setter
public class RegistrationInfo {

	@Id
    private String email;
    private String password;
    private String role;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

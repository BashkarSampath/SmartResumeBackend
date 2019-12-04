package centennial.comp231.smartresumebackend.POJO;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter @Setter
public class Response {
    Object object;
    String message;

    public Response() {
        this.object = new Object();
        this.message = "empty response";
    }  
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

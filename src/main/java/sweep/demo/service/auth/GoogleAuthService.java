package sweep.demo.service.auth;
import java.util.Map;

public interface GoogleAuthService {
    String getAccessToken (String code);
    Map<String, Object> getUserInfo(String accessToken);
}

package name.webdizz.fabric8.ci.cd.ping;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DynamicPingController {

    @Autowired
    private Environment environment;

    @RequestMapping("/ping")
    public String ping() {
        return String.format("Hello, I'm a Pinger @%s of %s generation!", environment.getProperty("kubernetes.namespace"), environment.getProperty("app.version"));
    }
}

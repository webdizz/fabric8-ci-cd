package name.webdizz.fabric8.ci.cd.ping;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DynamicPingController {

    @Autowired
    private Environment environment;

    @RequestMapping("/ping")
    public String ping() {
        String message = String.format("Hello, I'm a Pinger @%s:%s of %s generation!", environment.getProperty("hostname"), environment.getProperty("kubernetes.namespace"), environment.getProperty("app.version"));
        log.info(message);
        return message;
    }
}

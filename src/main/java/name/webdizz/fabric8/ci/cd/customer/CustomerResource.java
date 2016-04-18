package name.webdizz.fabric8.ci.cd.customer;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CustomerResource {

    private RestTemplate restTemplate;

    @Value("${customer.service.url:http://customer-app-fabric8-ci-cd.vagrant.f8/}")
    private String externalServicePath = "http://customer-service/customer";

    public CustomerResource(){
      restTemplate = new RestTemplate();
    }

    @RequestMapping("/customer")
    @HystrixCommand(fallbackMethod = "helloFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")
    })
    public String hello() {
        return restTemplate.getForObject(externalServicePath, String.class);
    }

    public String helloFallback() {
        return "Hello Fallback";
    }
}

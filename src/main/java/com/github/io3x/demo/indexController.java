package com.github.io3x.demo;

import com.github.io3x.app.func;
import com.github.io3x.app.sboot.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/demo/index")
public class indexController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/demotest",method = {RequestMethod.GET,RequestMethod.POST})
    public R demotest(HttpServletRequest request) {
        String ticket = func.randStr(16);
        return R.ok().put(ticket);
    }
}

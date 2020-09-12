package com.github.io3x.api;

import com.github.io3x.api.classes.funcApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/")
public class docController {
    @RequestMapping(value = "/",method = {RequestMethod.GET,RequestMethod.POST})
    public  String init(Model model,HttpServletRequest request) {
        String op = request.getParameter("op");
        Map docm = funcApi.Docs(op);
        if(docm!=null&&docm.size()>0) {
            model.addAttribute("op",op);
            model.addAttribute("docs",funcApi.Docs(op));
            /*model.asMap().putAll(new HashMap<String, Object>(){{
                put("op",op);
                put("docs", funcApi.Docs(op));
            }});*/
            return "/api/doc/doc";
        } else {
            return "/api/doc/index";
        }

    }
}

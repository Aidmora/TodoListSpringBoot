package madstodolist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/cuenta")
    public String cuenta() {
        return "account";
    }
}

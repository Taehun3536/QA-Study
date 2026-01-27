package com.ohgiraffers.qa.controller;

import com.ohgiraffers.qa.model.User;
import com.ohgiraffers.qa.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 로그인 화면
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                        @RequestParam String password,
                        HttpSession session) {

        Optional<User> userOpt = userService.login(loginId, password);

        if (userOpt.isEmpty()) {
            return "redirect:/user/login-fail";
        }

        User findUser = userOpt.get();
        session.setAttribute("user", findUser);

        return "redirect:/board/list";
    }

    @GetMapping("/login-fail")
    public String loginFail() {
        return "login-fail";
    }

    @GetMapping("/join")
    public String joinForm() {
        return "join";
    }

    @PostMapping("/join")
    public String join(@ModelAttribute User user, Model model) {

        boolean success = userService.join(user);

        if (!success) {
            // 실패 원인 메시지 내려주기
            model.addAttribute("errorMessage", "이미 존재하는 아이디입니다.");

            // 사용자가 입력한 값 유지
            model.addAttribute("loginId", user.getLoginId());
            model.addAttribute("nickname", user.getNickname());

            return "join"; // join.html 다시 렌더링
        }

        return "redirect:/user/login";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}

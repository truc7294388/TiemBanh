package com.bakeryshop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/contact")

public class ContactController {

    @PostMapping("/send")
    @ResponseBody
    public Map<String, Object> sendContact(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Lưu phản hồi vào database hoặc gửi email
            // contactService.saveFeedback(name, email, subject, message);

            // 2. Trả về thông báo thành công
            response.put("success", true);
            response.put("message", "Tin nhắn đã được gửi thành công!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi gửi phản hồi!");
        }

        return response;
    }
}

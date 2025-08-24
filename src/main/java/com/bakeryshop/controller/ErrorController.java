package com.bakeryshop.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "Không tìm thấy trang");
                model.addAttribute("message", "Trang bạn đang tìm kiếm không tồn tại.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "Lỗi máy chủ");
                model.addAttribute("message", "Đã xảy ra lỗi trong quá trình xử lý. Vui lòng thử lại sau.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "Truy cập bị từ chối");
                model.addAttribute("message", "Bạn không có quyền truy cập trang này.");
            } else {
                model.addAttribute("error", "Đã xảy ra lỗi");
                if (message != null) {
                    model.addAttribute("message", message.toString());
                } else if (exception != null) {
                    model.addAttribute("message", exception.toString());
                } else {
                    model.addAttribute("message", "Vui lòng thử lại sau hoặc liên hệ với quản trị viên.");
                }
            }
        } else {
            model.addAttribute("status", 500);
            model.addAttribute("error", "Đã xảy ra lỗi");
            model.addAttribute("message", "Vui lòng thử lại sau hoặc liên hệ với quản trị viên.");
        }

        return "error";
    }
} 
package io.github.spartateam6.commercepaymentsystem;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoPageController {

    @GetMapping("/products")
    public String products() {
        return "forward:/demo/products.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/demo/login.html";
    }

    @GetMapping("/cart")
    public String cart() {
        return "forward:/demo/cart.html";
    }

    @GetMapping("/orders")
    public String orders() {
        return "forward:/demo/order-list.html";
    }

    @GetMapping("/order-confirm")
    public String orderConfirm() {
        return "forward:/demo/order-confirm.html";
    }

    @GetMapping("/product-detail")
    public String productDetail() {
        return "forward:/demo/product-detail.html";
    }

    @GetMapping("/points")
    public String points() {
        return "forward:/demo/point-history.html";
    }

    @GetMapping("/my-page")
    public String myPage() {
        return "forward:/demo/my-page.html";
    }

    @GetMapping("/pay")
    public String pay() {
        return "forward:/demo/pay/index.html";
    }

    @GetMapping("/subscription")
    public String subscription() {
        return "forward:/demo/pay/subscription.html";
    }
}
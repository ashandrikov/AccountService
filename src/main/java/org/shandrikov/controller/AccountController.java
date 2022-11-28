package org.shandrikov.controller;

import org.shandrikov.dto.PaymentInDTO;
import org.shandrikov.entity.Payment;
import org.shandrikov.entity.User;
import org.shandrikov.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {
    @Autowired
    AccountService accountService;

    @PostMapping("/acct/payments")
    public Map<String, String> uploadPayrolls(@RequestBody List<PaymentInDTO> paymentList){
        accountService.uploadPayments(paymentList);
        return Map.of("status", "Added successfully!");
    }

    @PutMapping("/acct/payments")
    public Map<String, String> changePayment(@RequestBody PaymentInDTO payment){
        return accountService.updatePayment(payment);
    }

    @GetMapping("/empl/payment")
    public Object showPayroll(@RequestParam(required = false) String period, @AuthenticationPrincipal User user){
        if (period == null){
            return accountService.showPaymentsForEmployee(user);
        }
        return accountService.showPaymentForPeriod(period, user);
    }

    @GetMapping("/acct/payments")
    public List<Payment> showAllPayrolls(){
        return accountService.getAllPayments();
    }
}

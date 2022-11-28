package org.shandrikov.service;

import org.shandrikov.dto.PaymentInDTO;
import org.shandrikov.dto.PaymentOutDTO;
import org.shandrikov.entity.Payment;
import org.shandrikov.entity.User;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

public interface AccountService {
    List<Payment> getAllPayments();
    void uploadPayments(List<@Valid PaymentInDTO> paymentList);
    Map<String, String> updatePayment(@Valid PaymentInDTO paymentDTO);
    List<PaymentOutDTO> showPaymentsForEmployee(User user);
    PaymentOutDTO showPaymentForPeriod(String period, User user);
}

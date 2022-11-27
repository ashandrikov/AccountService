package org.shandrikov.mapper;

import org.shandrikov.dto.PaymentInDTO;
import org.shandrikov.dto.PaymentOutDTO;
import org.shandrikov.entity.Payment;
import org.shandrikov.entity.User;

public class PaymentMapper {
    public static Payment convertDtoToEntity(PaymentInDTO paymentInDTO){
        return Payment.builder()
                .email(paymentInDTO.getEmail().toLowerCase())
                .period(paymentInDTO.getPeriod())
                .salary(paymentInDTO.getSalary())
                .build();
    }

    public static PaymentOutDTO convertEntityToDTO(User user, Payment paymentEntity){
        long sal = paymentEntity.getSalary();
        return PaymentOutDTO.builder()
                .name(user.getName())
                .lastname(user.getLastname())
                .period(paymentEntity.getPeriod())
                .salary(String.format("%d dollar(s) %d cent(s)", sal / 100, sal % 100))
                .build();
    }
}

package org.shandrikov.service;

import org.shandrikov.dto.PaymentInDTO;
import org.shandrikov.dto.PaymentOutDTO;
import org.shandrikov.entity.Payment;
import org.shandrikov.entity.User;
import org.shandrikov.exception.EmployeeNotFoundException;
import org.shandrikov.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.shandrikov.repository.AccountRepository;
import org.shandrikov.repository.UserRepository;

import javax.validation.Valid;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.shandrikov.util.StringPool.PAYMENT_NOT_UNIQUE;


@Service
@Validated
public class AccountService {

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserRepository userRepository;

    public List<Payment> getAllPayments() {
        return accountRepository.findAll();
    }

    public void uploadPayments(List<@Valid PaymentInDTO> paymentList) {
        checkListValidPayments(paymentList);
        try {
            accountRepository.saveAll(paymentList.stream()
                    .map(PaymentMapper::convertDtoToEntity)
                    .toList());
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PAYMENT_NOT_UNIQUE);
        }
    }

    private void checkListValidPayments(List<PaymentInDTO> paymentDTOList) {
        for (PaymentInDTO item : paymentDTOList) {
            userRepository.findByEmail(item.getEmail())
                    .orElseThrow(EmployeeNotFoundException::new);
        }
    }

    public void updatePayment(@Valid PaymentInDTO paymentDTO) {
        checkValidPayment(paymentDTO);
        Payment payment = PaymentMapper.convertDtoToEntity(paymentDTO);
        Optional<Payment> paymentByPeriod = accountRepository.findByEmailAndPeriod(payment.getEmail(), payment.getPeriod());
        if (paymentByPeriod.isPresent()){
            paymentByPeriod.get().setSalary(paymentDTO.getSalary());
            accountRepository.save(paymentByPeriod.get());
        }
    }

    private void checkValidPayment(PaymentInDTO payment) {
        userRepository.findByEmail(payment.getEmail()).orElseThrow(EmployeeNotFoundException::new);
    }

    public List<PaymentOutDTO> showPaymentsForEmployee(UserDetails userDetails) {
        Optional<User> userById = userRepository.findByEmail(userDetails.getUsername());
        List<Payment> paymentById = accountRepository.findAllByEmail(userDetails.getUsername());
        List<PaymentOutDTO> resultSet = paymentById.stream()
                .map(el -> PaymentMapper.convertEntityToDTO(userById.get(), el))
                .toList();
        return resultSet.stream()
                .sorted(Comparator.comparing(PaymentOutDTO::getPeriod).reversed())
                .toList();
    }

    public PaymentOutDTO showPaymentForPeriod(String period, UserDetails userDetails) {
        YearMonth periodYM;
        try {
            periodYM = YearMonth.of(Integer.parseInt(period.substring(3)), Integer.parseInt(period.substring(0, 2)));
        } catch (DateTimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong month in params");
        }
        Optional<User> userById = userRepository.findByEmail(userDetails.getUsername());
        Optional<Payment> paymentByPeriod = accountRepository.findByEmailAndPeriod(userDetails.getUsername(), periodYM);
        if (paymentByPeriod.isPresent() && userById.isPresent()){
            return PaymentMapper.convertEntityToDTO(userById.get(), paymentByPeriod.get());
        }
        return null;
    }
}

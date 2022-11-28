package org.shandrikov.service.impl;

import org.shandrikov.dto.PaymentInDTO;
import org.shandrikov.dto.PaymentOutDTO;
import org.shandrikov.entity.Payment;
import org.shandrikov.entity.User;
import org.shandrikov.exception.EmployeeNotFoundException;
import org.shandrikov.util.mapper.PaymentMapper;
import org.shandrikov.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import java.util.Map;
import java.util.Optional;

import static org.shandrikov.util.StringPool.PAYMENT_NOT_UNIQUE;
import static org.shandrikov.util.StringPool.SMTH_WRONG;


@Service
@Validated
public class AccountServiceImpl implements AccountService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public List<Payment> getAllPayments() {
        return accountRepository.findAll();
    }

    @Override
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

    @Override
    public Map<String, String> updatePayment(@Valid PaymentInDTO paymentDTO) {
        userRepository.findByEmail(paymentDTO.getEmail()).orElseThrow(EmployeeNotFoundException::new);
        Payment payment = PaymentMapper.convertDtoToEntity(paymentDTO);
        Optional<Payment> paymentByPeriod = accountRepository.findByEmailAndPeriod(payment.getEmail(), payment.getPeriod());
        if (paymentByPeriod.isPresent()){
            paymentByPeriod.get().setSalary(paymentDTO.getSalary());
            accountRepository.save(paymentByPeriod.get());
            return Map.of("status", "Updated successfully!");
        }
        return Map.of("status", SMTH_WRONG);
    }

    @Override
    public List<PaymentOutDTO> showPaymentsForEmployee(User user) {
        Optional<User> userById = userRepository.findByEmail(user.getUsername());
        List<Payment> paymentById = accountRepository.findByEmail(user.getUsername());
        List<PaymentOutDTO> resultSet = paymentById.stream()
                .map(el -> PaymentMapper.convertEntityToDTO(userById.get(), el))
                .toList();
        return resultSet.stream()
                .sorted(Comparator.comparing(PaymentOutDTO::getPeriod).reversed())
                .toList();
    }

    @Override
    public PaymentOutDTO showPaymentForPeriod(String period, User user) {
        YearMonth periodYM;
        try {
            periodYM = YearMonth.of(Integer.parseInt(period.substring(3)), Integer.parseInt(period.substring(0, 2)));
        } catch (DateTimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong month in params");
        }
        Optional<User> userById = userRepository.findByEmail(user.getUsername());
        Optional<Payment> paymentByPeriod = accountRepository.findByEmailAndPeriod(user.getUsername(), periodYM);
        if (paymentByPeriod.isPresent() && userById.isPresent()){
            return PaymentMapper.convertEntityToDTO(userById.get(), paymentByPeriod.get());
        }
        return null;
    }
}

package org.shandrikov.repository;

import org.shandrikov.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Payment, Long>{
     List<Payment> findByEmail(String email);

     Optional<Payment> findByEmailAndPeriod(String email, YearMonth period);
}

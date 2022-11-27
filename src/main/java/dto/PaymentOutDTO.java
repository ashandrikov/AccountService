package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOutDTO {
    private String name;
    private String lastname;
    @JsonFormat(pattern="MMMM-y")
    private YearMonth period;
    private String salary;
}

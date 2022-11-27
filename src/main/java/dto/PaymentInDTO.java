package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.time.YearMonth;

@Data
public class PaymentInDTO {
    @JsonProperty("employee")
    private String email;
    @JsonFormat(pattern="MM-y")
    private YearMonth period;
    @Min(value = 0, message = "Salary cannot be less than 0!")
    private Long salary;
}

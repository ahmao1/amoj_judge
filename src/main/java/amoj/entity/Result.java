package amoj.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result implements Serializable {
    private Long resultId;
    private Long submitId;
    private Long usedTime;
    private Long usedMem;
    private String result;
    private String resultMsg;
    private Double acc;
}

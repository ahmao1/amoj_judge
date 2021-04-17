package amoj.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Submit implements Serializable {
    private Long submitId;
    private Long problemId;
    private Integer language;
    private Long userId;
    private String submitTime;
    private String source;//源代码
    private Long time;//时限
    private Long mem;//内存限制
    private Long resultId;
}

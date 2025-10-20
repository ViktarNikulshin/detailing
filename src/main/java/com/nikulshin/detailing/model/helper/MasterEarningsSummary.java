package com.nikulshin.detailing.model.helper;

import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.dto.report.MasterWorkTypeEarningDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class MasterEarningsSummary  extends MasterWorkTypeEarningDto {
    private Map<String, Double> totalEarningsByWorkType = new HashMap<>();
    private User master;

    public void addEarnings(String workType, Double earnings) {
        this.totalEarningsByWorkType.merge(workType, earnings, Double::sum);
    }
    public Double getTotalMasterEarnings() {
        return totalEarningsByWorkType.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

}

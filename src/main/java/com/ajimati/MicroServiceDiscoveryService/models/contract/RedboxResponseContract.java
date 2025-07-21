package com.ajimati.MicroServiceDiscoveryService.models.contract;

import com.ajimati.MicroServiceDiscoveryService.enums.Responses;
import com.ajimati.MicroServiceDiscoveryService.models.QualifiedProjects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedboxResponseContract {
    private String responseCode;
    private String responseMsg;
    private Object responseDetails;

    RedboxResponseContract(Responses responseEnum) {
        responseCode = responseEnum.getResponseCode();
        responseMsg = responseEnum.getResponseMsg();
        responseDetails = new QualifiedProjects();
    }

    public RedboxResponseContract(QualifiedProjects qualifiedProjects) {
        responseCode = "00";
        responseMsg = "Match found";
        responseDetails = qualifiedProjects;
    }
}

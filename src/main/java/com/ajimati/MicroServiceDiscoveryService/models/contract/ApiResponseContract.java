package com.ajimati.MicroServiceDiscoveryService.models.contract;

import com.ajimati.MicroServiceDiscoveryService.enums.Responses;
import com.ajimati.MicroServiceDiscoveryService.models.QualifiedProjects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseContract {
    private String responseCode;
    private String responseMsg;
    private String exceptions;
    private Object responseDetails;

    ApiResponseContract(Responses responseEnum) {
        responseCode = responseEnum.getResponseCode();
        responseMsg = responseEnum.getResponseMsg();
        responseDetails = new QualifiedProjects();
    }

    public ApiResponseContract(QualifiedProjects qualifiedProjects, String warning) {
        responseCode = "00";
        responseMsg = "Match found";
        exceptions = warning;
        responseDetails = qualifiedProjects;
    }
}

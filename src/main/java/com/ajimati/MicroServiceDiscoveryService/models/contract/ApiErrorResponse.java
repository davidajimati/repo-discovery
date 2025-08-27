package com.ajimati.MicroServiceDiscoveryService.models.contract;

import com.ajimati.MicroServiceDiscoveryService.enums.Responses;
import com.ajimati.MicroServiceDiscoveryService.models.QualifiedProjects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String responseCode;
    private String responseMsg;
    private Object responseDetails;


    ApiErrorResponse(Responses responseEnum) {
        responseCode = responseEnum.getResponseCode();
        responseMsg = responseEnum.getResponseMsg();
        responseDetails = new QualifiedProjects();
    }

    public ApiErrorResponse(QualifiedProjects qualifiedProjects) {
        responseCode = "00";
        responseMsg = "Match found";
        responseDetails = qualifiedProjects;
    }
}

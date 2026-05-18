package com.mes.dto.request;

import lombok.Data;

@Data
public class PullStationRequest {

    // Serial cần kéo
    private String serialCode;

    // Công đoạn đích
    private Integer targetStepId;

    // Lý do kéo trạm
    private String reason;

    // Có force hay không
    private Boolean force;
}
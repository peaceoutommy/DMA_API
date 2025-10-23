package dev.tomas.dma.model;

import lombok.Data;

@Data
public class UserCompanyMembershipModel {
    private Integer userId;
    private Integer companyId;
    private String companyRole;
}

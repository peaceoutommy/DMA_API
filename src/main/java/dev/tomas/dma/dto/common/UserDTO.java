package dev.tomas.dma.dto.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDTO {
    private Integer id;
    private String email;
    private String phoneNumber;
    private String address;
    private String firstName;
    private String lastName;
    private String middleNames;
    private String username;
    private Integer companyId;
    private Integer companyRoleId;
    private String companyRoleName;
}

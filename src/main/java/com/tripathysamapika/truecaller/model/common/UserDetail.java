package com.tripathysamapika.truecaller.model.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class UserDetail {

    private String firstName;
    private String lastName;
    private String middleName;
    private Tag tag;
    private BusinessSize businessSize;
    private Map<Days, OperatingHours> openHours;
    private SocialInfo socialInfo;
    private String phoneNumber;
    private String countryCode;
    private String email;

}

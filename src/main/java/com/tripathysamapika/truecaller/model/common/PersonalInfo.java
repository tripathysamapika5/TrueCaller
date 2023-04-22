package com.tripathysamapika.truecaller.model.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersonalInfo {

    private String firstName;
    private String lastName;
    private String middleName;
    private String initials;
    private String dob;
    private Gender gender;
    private Address address;
    private String companyName;
    private String title;

    public PersonalInfo(String firstName){
        this.firstName = firstName;
    }

}

package com.tripathysamapika.truecaller.model.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Business {
    private String businessName;
    private String businessDescription;
    private Tag tag;
    private BusinessSize businessSize;
    private Map<Days, OperatingHours> openHours;
    private Contact contact;
    private PersonalInfo ownerInfo;
    private SocialInfo socialInfo;


    public Business(String businessName, Tag tag){
        this.businessName = businessName;
        this.tag = tag;
    }

}

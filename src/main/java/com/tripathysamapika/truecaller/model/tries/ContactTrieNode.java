package com.tripathysamapika.truecaller.model.tries;

import com.tripathysamapika.truecaller.model.common.UserDetail;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ContactTrieNode{

    private String keyChar;
    private ContactTrieNode[] children;
    private Boolean isLeaf;
    private List<UserDetail> contactDetails;

    public ContactTrieNode(String keyChar){
        this.keyChar = keyChar;
        children = new ContactTrieNode[256];
        contactDetails = new ArrayList<>();
    }

    public void insertChild(int index, String keyChar){
        this.children[index] = new ContactTrieNode(keyChar);
    }

    public void insertChild(int index, String keyChar, UserDetail contactDetail){
        this.children[index] = new ContactTrieNode(keyChar);
        this.children[index].getContactDetails().add(contactDetail);
        this.children[index].setIsLeaf(true);
    }

}

package com.tripathysamapika.truecaller.model.tries;

import com.tripathysamapika.truecaller.model.common.UserDetail;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@ToString
public class ContactTrie {

    private ContactTrieNode rootContactTrie;

    public ContactTrie() {
        rootContactTrie = new ContactTrieNode(null);
    }

    public void insert(String key, UserDetail userDetail) {
        int keyLength = key.length();
        AtomicReference<ContactTrieNode> tempNode = new AtomicReference<>(rootContactTrie);
        IntStream.range(0, keyLength).forEach(index -> {
                    char keyChar = key.charAt(index);
                    int asciiIndex = keyChar;
                    if (tempNode.get().getChildren()[asciiIndex] == null) {
                        tempNode.get().insertChild(asciiIndex, String.valueOf(keyChar));

                    }
            tempNode.set(tempNode.get().getChildren()[asciiIndex]);


                }
        );
        tempNode.get().getContactDetails().add(userDetail);
        tempNode.get().setIsLeaf(true);
    }


    public List<UserDetail> search(String key) {
        int keyLength = key.length();
        AtomicReference<ContactTrieNode> tempNode = new AtomicReference<>(rootContactTrie);

        List<UserDetail> userDetails = new ArrayList<>();
        for(int index = 0 ; index < keyLength ; index ++){
            char keyChar = key.charAt(index);
            int asciiIndex = keyChar;
            if (tempNode.get().getChildren()[asciiIndex] == null) {
                return userDetails;
            }else{
                tempNode.set(tempNode.get().getChildren()[asciiIndex]);
            }
        }

        if(tempNode.get().getIsLeaf()){
            userDetails = tempNode.get().getContactDetails();
        }

        return userDetails;
    }


    public List<UserDetail> allContactsWithPrefix(String prefix){
        int keyLength = prefix.length();
        AtomicReference<ContactTrieNode> tempNode = new AtomicReference<>(rootContactTrie);

        List<UserDetail> userDetails = new ArrayList<>();
        for(int index = 0 ; index < keyLength ; index ++){
            char keyChar = prefix.charAt(index);
            int asciiIndex = keyChar;
            if (tempNode.get().getChildren()[asciiIndex] == null) {
                return userDetails;
            }else{
                tempNode.set(tempNode.get().getChildren()[asciiIndex]);
            }
        }

        getAllContacts(tempNode.get(), userDetails);

        return userDetails;


    }


    private void getAllContacts(ContactTrieNode rootNode, List<UserDetail> userDetails){
        AtomicReference<ContactTrieNode> tempNode = new AtomicReference<>(rootNode);
        userDetails.addAll(tempNode.get().getContactDetails());
        Arrays.stream(tempNode.get().getChildren()).filter(node -> node != null && node.getKeyChar() != null ).forEach(child ->{
            tempNode.set(child);
            getAllContacts(tempNode.get(), userDetails);
        });
    }

    public void remove(String key){
        int keyLength = key.length();
        AtomicReference<ContactTrieNode> tempNode = new AtomicReference<>(rootContactTrie);
        IntStream.range(0, keyLength).forEach(index -> {
                    char keyChar = key.charAt(index);
                    int asciiIndex = keyChar;
                    if (tempNode.get().getChildren()[asciiIndex] == null) {
                        return;
                    }
                    tempNode.set(tempNode.get().getChildren()[asciiIndex]);

                }
        );
        tempNode.get().setContactDetails(new ArrayList<>());
        tempNode.get().setIsLeaf(false);
    }


}

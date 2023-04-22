package com.tripathysamapika.truecaller.model;

import com.tripathysamapika.truecaller.GlobalContacts;
import com.tripathysamapika.truecaller.model.common.Contact;
import com.tripathysamapika.truecaller.GlobalSpam;
import com.tripathysamapika.truecaller.model.common.PersonalInfo;
import com.tripathysamapika.truecaller.model.common.UserDetail;
import com.tripathysamapika.truecaller.model.tries.ContactTrie;
import lombok.Getter;
import lombok.Setter;
import orestes.bloomfilter.FilterBuilder;

import java.util.*;

import static com.tripathysamapika.truecaller.model.common.Constant.*;

@Setter
@Getter
public class User  extends Account  {

    public User(){
        setContactTrie(new ContactTrie());
    }

    public User(String phoneNumber, String firstName) {
        super(phoneNumber, firstName);
        setContactTrie(new ContactTrie());
    }

    public User(String phoneNumber, String firstName, String lastName) {
        super(phoneNumber, firstName, lastName);
        setContactTrie(new ContactTrie());
    }

    @Override
    public void register(UserCategory userCategory, String userName, String password, String email,
                         String phoneNumber, String countryCode, String firstName) {
        setId(UUID.randomUUID().toString());
        setUserCategory(userCategory);
        setUserName(userName);
        setPassword(password);
        setContact(new Contact(phoneNumber, email, countryCode));
        setPersonalInfo(new PersonalInfo(firstName));
        init(userCategory);

        UserDetail userDetail = getUserDetail();

        insertToTries(phoneNumber, firstName, userDetail);
    }

    private UserDetail getUserDetail() {
        UserDetail userDetail = new UserDetail();
        userDetail.setFirstName(this.getPersonalInfo().getFirstName());
        userDetail.setPhoneNumber(this.getPhoneNumber());
        if(this.getContact() != null) {
            userDetail.setEmail(this.getContact().getEmailId());
            userDetail.setCountryCode(this.getContact().getCountryCode());
        }
        return userDetail;
    }


    private void init(UserCategory userCategory) {
        switch (userCategory) {
            case FREE:
                setContacts(new HashMap<>(MAX_FREE_USER_CONTACTS));
                setBlockedContacts(new FilterBuilder(MAX_FREE_USER_BLOCKED_CONTACTS, .01)
                        .buildCountingBloomFilter());
                setBlockedSet(new HashSet<>(MAX_FREE_USER_BLOCKED_CONTACTS));
                break;
            case GOLD:
                setContacts(new HashMap<>(MAX_GOLD_USER_CONTACTS));
                setBlockedContacts(new FilterBuilder(MAX_GOLD_USER_BLOCKED_CONTACTS, .01)
                        .buildCountingBloomFilter());
                setBlockedSet(new HashSet<>(MAX_GOLD_USER_BLOCKED_CONTACTS));
                break;

            case PLATINUM:
                setContacts(new HashMap<>(MAX_PLATINUM_USER_CONTACTS));
                setBlockedContacts(new FilterBuilder(MAX_PLATINUM_USER_BLOCKED_CONTACTS, .01)
                        .buildCountingBloomFilter());
                setBlockedSet(new HashSet<>(MAX_PLATINUM_USER_BLOCKED_CONTACTS));
                break;
        }
    }

    private void insertToTries(String phoneNumber, String firstName, UserDetail userDetail) {
        getContactTrie().insert(phoneNumber, userDetail);
        getContactTrie().insert(firstName, userDetail);
        GlobalContacts.INSTANCE.getContactTrie().insert(phoneNumber, userDetail);
        GlobalContacts.INSTANCE.getContactTrie().insert(firstName, userDetail);
    }

    @Override
    public void addContact(User user) {
        checkAddUser();
        UserDetail userDetail = user.getUserDetail();
        getContacts().putIfAbsent(user.getPhoneNumber(), user);
        insertToTries(user.getPhoneNumber(), user.getPersonalInfo().getFirstName(), userDetail);
    }

    private void checkAddUser(){
        switch (this.getUserCategory()) {
            case FREE:
                if (this.getContacts().size() >= MAX_FREE_USER_CONTACTS)
                    throw new RuntimeException("Default contact size exceeded");
            case GOLD:
                if (this.getContacts().size() >= MAX_GOLD_USER_CONTACTS)
                    throw new RuntimeException("Default contact size exceeded");
            case PLATINUM:
                if (this.getContacts().size() >= MAX_PLATINUM_USER_CONTACTS)
                    throw new RuntimeException("Default contact size exceeded");
        }
    }


    @Override
    public void removeContact(String number){
        User contact = getContacts().get(number);
        if (contact == null)
            throw new RuntimeException("Contact does not exist");
        getContacts().remove(number);
        getContactTrie().remove(number);
        getContactTrie().remove(contact.getPersonalInfo().getFirstName());
    }



    private void checkBlockUser() {
        switch (this.getUserCategory()) {
            case FREE:
                if (this.getContacts().size() >= MAX_FREE_USER_BLOCKED_CONTACTS)
                    throw new RuntimeException("Exceeded max contacts to be blocked");
            case GOLD:
                if (this.getContacts().size() >= MAX_GOLD_USER_BLOCKED_CONTACTS)
                    throw new RuntimeException("Exceeded max contacts to be blocked");
            case PLATINUM:
                if (this.getContacts().size() >= MAX_PLATINUM_USER_BLOCKED_CONTACTS)
                    throw new RuntimeException("Exceeded max contacts to be blocked");
        }
    }

    public void blockNumber(String number){
        checkBlockUser();
        getBlockedContacts().add(number);
    }

    public void unblockNumber(String number) {
        getBlockedContacts().remove(number);
    }

    public void reportSpam(String number, String reason) {
        getBlockedContacts().add(number);
        GlobalSpam.INSTANCE.reportSpam(number, this.getPhoneNumber(), reason);
    }


    public void upgrade(UserCategory userCategory) {
        int count = 0;
        int blockedCount = 0;
        switch (userCategory) {
            case GOLD:
                count = MAX_GOLD_USER_CONTACTS;
                blockedCount = MAX_GOLD_USER_BLOCKED_CONTACTS;
                break;
            case PLATINUM:
                count = MAX_PLATINUM_USER_CONTACTS;
                blockedCount = MAX_PLATINUM_USER_BLOCKED_CONTACTS;
                break;
        }
        upgradeContacts(count);
        upgradeBlockedContact(blockedCount);
    }

    public boolean isBlocked(String number) {
        return getBlockedContacts().contains(number);
    }

    public boolean canReceive(String number) {
        return !isBlocked(number) &&
                !GlobalSpam.INSTANCE.isGlobalSpam(number);
    }


    private void upgradeBlockedContact(int blockedCount) {
        setBlockedContacts(new FilterBuilder(blockedCount, .01)
                .buildCountingBloomFilter());
        Set<String> upgradedSet = new HashSet<>();
        for (String blocked : getBlockedSet()) {
            upgradedSet.add(blocked);
            getBlockedContacts().add(blocked);
        }
    }

    private void upgradeContacts(int count) {
        Map<String, User> upgradedContacts = new HashMap<>(count);
        for (Map.Entry<String, User> entry : getContacts().entrySet()) {
            upgradedContacts.putIfAbsent(entry.getKey(), entry.getValue());
        }
        setContacts(upgradedContacts);
    }

    public boolean importContacts(List<User> users) {
        for (User user : users) {
            try {
                addContact(user);
            } catch (RuntimeException cee) {
                System.out.println("Some of the contact could not be imported as limit exceeded");
                return false;
            }
        }
        return true;
    }

}

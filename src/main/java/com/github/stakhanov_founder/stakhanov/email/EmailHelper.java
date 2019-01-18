package com.github.stakhanov_founder.stakhanov.email;

import org.apache.commons.math3.util.Pair;

import com.github.stakhanov_founder.stakhanov.email.model.EmailAddressComponents;

class EmailHelper {

    boolean isSameEmailAccount(String emailAddress1, String emailAddress2) {
        if (emailAddress1 == null || emailAddress2 == null) {
            return emailAddress1 == emailAddress2;
        }
        return isSameEmailAccount(
                decomposeEmailAddress(emailAddress1), decomposeEmailAddress(emailAddress2));
    }

    boolean isSameEmailAccount(EmailAddressComponents emailAddressComponents1,
            EmailAddressComponents emailAddressComponents2) {
        if (emailAddressComponents1 == null || emailAddressComponents2 == null) {
            return emailAddressComponents1 == emailAddressComponents2;
        }
        return emailAddressComponents1.userName.equals(emailAddressComponents2.userName)
                && emailAddressComponents1.domain.equals(emailAddressComponents2.domain);
    }

    EmailAddressComponents decomposeEmailAddress(String emailAddress) {
        if (emailAddress == null) {
            throw new IllegalArgumentException("Null passed as email address");
        }
        Pair<String, String> addressSplitByArobas
            = splitByFirstOccurrenceOfCharacter(emailAddress, '@');
        Pair<String, String> userNameAndTag = splitByFirstOccurrenceOfCharacter(
                addressSplitByArobas.getFirst(), '+');
        return new EmailAddressComponents(
                userNameAndTag.getFirst(), userNameAndTag.getSecond(),
                addressSplitByArobas.getSecond());
    }

    private Pair<String, String> splitByFirstOccurrenceOfCharacter(String string, char character) {
        if (string  == null) {
            throw new IllegalArgumentException("Null passed as input string to split in two");
        }
        int indexOfCharacter = string.indexOf(character);
        if (indexOfCharacter < 0 || indexOfCharacter == string.length() - 1) {
            return new Pair<>(string, "");
        }
        return new Pair<>(string.substring(0, indexOfCharacter), string.substring(indexOfCharacter + 1));
    }
}

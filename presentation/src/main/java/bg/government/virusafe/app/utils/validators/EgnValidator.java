package bg.government.virusafe.app.utils.validators;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;

import static com.upnetix.applicationservice.registration.model.Gender.VALUE_FEMALE;
import static com.upnetix.applicationservice.registration.model.Gender.VALUE_MALE;

/**
 * Validate EGN.
 */
public class EgnValidator implements PersonalIdValidator {

    private static final List<Integer> EGN_WEIGHTS = Arrays.asList(2, 4, 8, 5, 10, 9, 7, 3, 6);
    private static final Integer EGN_MOD = 11;

    private String personalNumber;
    private boolean isValid = false;
    private Long years = null;
    private String gender;

    public boolean isValidPersonalId() {

        return isValid;
    }

    @Nullable
    public Long getYears() {

        return years;
    }

    @Nullable
    public String getGender() {

        return gender;
    }

    public void initPersonalNumber(@Nullable String personalNumber) {

        this.personalNumber = personalNumber;
        isValid = isValidPersonalNumber();
        if (!isValid) {
            years = null;
            gender = null;
        }
    }

    public void clearAll() {

        this.personalNumber = null;
        isValid = false;
        years = null;
        gender = null;
    }

    private boolean isValidPersonalNumber() {

        if (personalNumber == null || personalNumber.equals("")) {
            return true;
        } else if (personalNumber.length() != 10) {
            return false;
        }

        final char[] charArr = personalNumber.toCharArray();
        final List<Integer> personalNumberDigits = new ArrayList<>(charArr.length);

        for (char c : charArr) {
            personalNumberDigits.add(c - '0');
        }

        updateGender(personalNumberDigits);

        boolean isValidBirthday = validateBirthday(personalNumberDigits);
        boolean isValidEgnChecksum = validateEgnCheckSum(personalNumberDigits);
        return isValidBirthday && isValidEgnChecksum;
    }

    private void updateGender(List<Integer> egnDigits) {

        int c = egnDigits.get(8);
        if (c % 2 == 0) {
            gender = VALUE_MALE;
        } else {
            gender = VALUE_FEMALE;
        }
    }

    private boolean validateBirthday(List<Integer> egnDigits) {

        int year = egnDigits.get(0) * 10 + egnDigits.get(1);
        int month = egnDigits.get(2) * 10 + egnDigits.get(3);
        int day = egnDigits.get(4) * 10 + egnDigits.get(5);

        // Handle month offsets based on birth years before 1900 and after 2000.
        if (month > 40) {
            month -= 40;
            year += 2000;
        } else if (month > 20) {
            month -= 20;
            year += 1800;
        } else {
            year += 1900;
        }
        try {
            LocalDate birthDate = LocalDate.of(year, month, day);
            LocalDate now = LocalDate.now();
            years = ChronoUnit.YEARS.between(birthDate, now);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean validateEgnCheckSum(List<Integer> egnDigits) {

        int checkSum = 0;
        for (int i = 0; i < egnDigits.size() - 1; i++) {
            checkSum += (egnDigits.get(i) * EGN_WEIGHTS.get(i));
        }
        checkSum %= EGN_MOD;
        if (checkSum == 10) {
            checkSum = 0;
        }
        return egnDigits.get(9) == checkSum;
    }
}

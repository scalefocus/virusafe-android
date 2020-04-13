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
 * Validate EMBG.
 */
public class EmbgValidator implements PersonalIdValidator {

    private static final List<Integer> EMBG_WEIGHTS = Arrays.asList(7, 6, 5, 4, 3, 2);
    private static final Integer EMBG_SKIP_FACTOR = 6;
    private static final Integer EMBG_MOD = 11;
    private static final Integer EMBG_BASE = 11;

    private String personalNumber;
    private boolean isValid = false;
    private Long years = null;
    private String gender;

    @Override
    public boolean isValidPersonalId() {

        return isValid;
    }

    @Nullable
    @Override
    public Long getYears() {

        return years;
    }

    @Nullable
    @Override
    public String getGender() {

        return gender;
    }

    @Override
    public void initPersonalNumber(@Nullable String personalNumber) {

        this.personalNumber = personalNumber;
        isValid = isValidPersonalNumber();
        if (!isValid) {
            years = null;
            gender = null;
        }
    }

    @Override
    public void clearAll() {

        this.personalNumber = null;
        isValid = false;
        years = null;
        gender = null;
    }

    private boolean isValidPersonalNumber() {

        if (personalNumber == null || personalNumber.equals("")) {
            return true;
        } else if (personalNumber.length() != 13) {
            return false;
        }

        final char[] charArr = personalNumber.toCharArray();
        final List<Integer> personalNumberDigits = new ArrayList<>(charArr.length);

        for (char c : charArr) {
            personalNumberDigits.add(c - '0');
        }

        updateGender(personalNumberDigits);

        boolean isValidBirthday = validateBirthday(personalNumberDigits);
        boolean isValidEmbgChecksum = validateChecksum(personalNumberDigits);
        return isValidBirthday && isValidEmbgChecksum;
    }

    private boolean validateBirthday(List<Integer> personalNumberDigits) {

        int year = 1000 + personalNumberDigits.get(4) * 100 +
                personalNumberDigits.get(5) * 10 + personalNumberDigits.get(6);
        int month = personalNumberDigits.get(2) * 10 + personalNumberDigits.get(3);
        int day = personalNumberDigits.get(0) * 10 + personalNumberDigits.get(1);

        // Handle years after 2000.
        if (year <= 1800) {
            year += 1000;
        }

        try {
            LocalDate birthday = LocalDate.of(year, month, day);
            LocalDate now = LocalDate.now();
            years = ChronoUnit.YEARS.between(birthday, now);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void updateGender(List<Integer> personalNumberDigits) {

        int genderUniqueNumber = personalNumberDigits.get(9) * 100 +
                personalNumberDigits.get(10) * 10 + personalNumberDigits.get(11);
        if (genderUniqueNumber > 499) {
            gender = VALUE_FEMALE;
        } else {
            gender = VALUE_MALE;
        }
    }

    private boolean validateChecksum(List<Integer> personalNumberDigits) {

        int checkSum = 0;
        for (int i = 0; i < EMBG_WEIGHTS.size(); i++) {
            checkSum += (personalNumberDigits.get(i) + personalNumberDigits.get(i + EMBG_SKIP_FACTOR))
                    * EMBG_WEIGHTS.get(i);
        }
        checkSum %= EMBG_MOD;
        checkSum = EMBG_BASE - checkSum;
        if (checkSum > 9) {
            checkSum = 0;
        }
        return personalNumberDigits.get(12) == checkSum;
    }
}

package com.polyconnect.validation;

import com.polyconnect.exception.InvalidPinException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PinValidator {

    // Matches: 2 digits year + 3 digits college code + '-' + 2-3 letters branch + '-' + 3-4 digits roll
    // Example: "24047-CS-023", "21001-EC-005", "23002-ME-102"
    private static final Pattern PIN_PATTERN = Pattern.compile("^(\\d{2})(\\d{3})-([A-Za-z]{2,4})-(\\d{3,4})$");

    public record ParsedPin(
        String yearPrefix,
        String collegeCode,
        String branchCode,
        String rollNumber
    ) {}

    public ParsedPin parseAndValidate(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            throw new InvalidPinException("Student PIN cannot be empty.");
        }

        String sanitizedPin = pin.trim().toUpperCase();
        Matcher matcher = PIN_PATTERN.matcher(sanitizedPin);

        if (!matcher.matches()) {
            throw new InvalidPinException("Invalid PIN format '" + pin + "'. Required format: YYCCC-BB-NNN (e.g. 24047-CS-023)");
        }

        String yearPrefix = matcher.group(1);
        String collegeCode = matcher.group(2); // 3-digit college code preserving leading zeros e.g. "047", "001"
        String branchCode = matcher.group(3).toUpperCase();
        String rollNumber = matcher.group(4);

        return new ParsedPin(yearPrefix, collegeCode, branchCode, rollNumber);
    }

    public void validateMatchesRegistration(String pin, String expectedCollegeCode, String expectedBranchCode) {
        ParsedPin parsed = parseAndValidate(pin);

        if (expectedCollegeCode != null && !parsed.collegeCode().equals(expectedCollegeCode)) {
            throw new InvalidPinException("PIN college code '" + parsed.collegeCode() + "' does not match selected college code '" + expectedCollegeCode + "'.");
        }

        if (expectedBranchCode != null && !parsed.branchCode().equalsIgnoreCase(expectedBranchCode)) {
            throw new InvalidPinException("PIN branch code '" + parsed.branchCode() + "' does not match selected branch '" + expectedBranchCode + "'.");
        }
    }
}

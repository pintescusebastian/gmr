package com.mycompany.report_generator.services;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class VitalSignsRiskStrategy implements RiskEvaluationStrategy {

    @Override
    public List<String> evaluate(Map<String, String> vitalSigns, String symptomsDescription) {
        if (vitalSigns == null) return Collections.emptyList();

        List<String> risks = new ArrayList<>();

        String bp = vitalSigns.get("blood_pressure");
        if (bp != null && bp.matches("1[4-9]\\d/.*|2\\d{2}/.*")) {
            risks.add("High Blood Pressure");
        }

        String hr = vitalSigns.get("heart_rate");
        if (hr != null) {
            try {
                int heartRate = Integer.parseInt(hr.replaceAll("\\D", ""));
                if (heartRate > 100) {
                    risks.add("High Heart Rate");
                }
            } catch (NumberFormatException e) {
                // invalid heart rate, ignore
            }
        }

        return risks;
    }
}

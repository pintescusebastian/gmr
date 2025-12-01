package com.mycompany.report_generator.services;

import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.Patient;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Component
public class AgeRiskStrategy implements RiskEvaluationStrategy {

    @Override
    public List<String> evaluate(Map<String, String> vitalSigns, String symptomsDescription) {
        List<String> risks = new ArrayList<>();

        if(vitalSigns != null && vitalSigns.containsKey("age")) {
            try {
                int age = Integer.parseInt(vitalSigns.get("age"));
                if(age >= 65) {
                    risks.add("Elderly Patient Risk");
                }
            } catch(NumberFormatException e) {
                // ignore
            }
        }

        return risks;
    }
}

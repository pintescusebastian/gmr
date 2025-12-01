package com.mycompany.report_generator.services;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
public class SymptomsRiskStrategy implements RiskEvaluationStrategy {

    @Override
    public List<String> evaluate(Map<String, String> vitalSigns, String symptomsDescription) {
        List<String> risks = new ArrayList<>();

        if(symptomsDescription == null) return risks;

        String desc = symptomsDescription.toLowerCase();

        if(desc.contains("chest pain") || desc.contains("palpitations")) {
            risks.add("Cardiac Symptoms");
        }
        if(desc.contains("dizziness") || desc.contains("fainting")) {
            risks.add("Neurological Symptoms");
        }
        if(desc.contains("shortness of breath")) {
            risks.add("Respiratory Symptoms");
        }

        return risks;
    }
}

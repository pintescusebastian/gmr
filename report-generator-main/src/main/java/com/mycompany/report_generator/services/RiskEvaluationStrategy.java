package com.mycompany.report_generator.services;

import java.util.List;
import java.util.Map;

public interface RiskEvaluationStrategy {
    List<String> evaluate(Map<String, String> vitalSigns, String symptomsDescription);
}

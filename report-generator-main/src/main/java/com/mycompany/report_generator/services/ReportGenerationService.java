package com.mycompany.report_generator.services;


import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;

public interface ReportGenerationService {

    ObservationReport generateReport(Observation observation);

}

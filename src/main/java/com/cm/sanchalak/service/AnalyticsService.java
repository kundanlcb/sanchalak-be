package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.analytics.CollectionTrendDto;
import com.cm.sanchalak.dto.analytics.FinancialSummaryDto;
import com.cm.sanchalak.dto.analytics.ReportCardDataDto;
import java.util.List;

public interface AnalyticsService {
    ReportCardDataDto getReportCardData(Long studentId, Long termId);
    FinancialSummaryDto getFinancialSummary();
    List<CollectionTrendDto> getCollectionTrend(int days);
}

package com.concur.classes;

public class ReportSummary {
	private String reportName;
	private String reportID;
	private String reportCurrency;
	private String reportDate;
	private String lastComment;
	private String approvalstatus;
	private String reportDetails;
	private String expenseUserLoginID;
	private String approverLoginID;
	private String employeeName;
	private String paymentStatus;
	private int reportTotal;

	public ReportSummary(String rn, String rID, String rc, int rt, String rd,
			String c, String as, String rdet, String eID, String aID,
			String name, String status) {
		reportName = rn;
		reportID = rID;
		reportCurrency = rc;
		reportTotal = rt;
		reportDate = rd;
		lastComment = c;
		approvalstatus = as;
		reportDetails = rdet;
		expenseUserLoginID = eID;
		approverLoginID = aID;
		employeeName = name;
		paymentStatus = status;
	}

}

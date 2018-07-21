package com.kcsl.dynadoc.wrapper;

import java.util.List;

import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Tbody;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Tfoot;
import com.hp.gagawa.java.elements.Th;
import com.hp.gagawa.java.elements.Thead;
import com.hp.gagawa.java.elements.Tr;
import com.kcsl.dynadoc.data.Issue;
import com.kcsl.dynadoc.data.JavaClass;

public class ClassIssuesWrapper {
	
	private static final String ISSUES_TABLE_JAVASCRIPT_FILE_NAME = "jquery-issues-table-script.js";
	
	private static final String ISSUES_SECTION_HEADER = "Issue Summary";
	
	private static final String ISSUES_TABLE_NAME = "issues-table";

	private static final String [] ISSUES_TABLE_HEADERS = { "Issue Id", "Last Changed", "Summary", "Status", "Severity", "Priority", "Related Commits", "View Report" };

	private List<Issue> issues;
	
	public ClassIssuesWrapper(JavaClass javaClass) {
		this.issues = javaClass.getIssues();
	}
	
	private List<Issue> getIssues() {
		return this.issues;
	}
	
	public Div wrap() {
		Div fieldsTableDiv = new Div();
		fieldsTableDiv.setCSSClass("card text-white bg-secondary mb-3");
		fieldsTableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(ISSUES_SECTION_HEADER);
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId(ISSUES_TABLE_NAME);
					table.setCSSClass("display small");
					table.setStyle("width:100%");
						
						Thead tHead = new Thead();
							Tr tr = new Tr();
								Th firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(String headerText: ISSUES_TABLE_HEADERS) {
									Th column = new Th();
									column.appendText(headerText);
									tr.appendChild(column);
								}
							tHead.appendChild(tr);
						table.appendChild(tHead);
						
						Tbody tBody = new Tbody();
						
						List<Issue> issues = this.getIssues();
						for(Issue issue: issues) {
							Tr issueRow = this.wrapIssue(issue);
							tBody.appendChild(issueRow);
						}
						table.appendChild(tBody);
						
						Tfoot tFoot = new Tfoot();
							tr = new Tr();
								firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(int i = 0; i < ISSUES_TABLE_HEADERS.length; i++) {
									Th column = new Th();
									tr.appendChild(column);
								}						
							tFoot.appendChild(tr);
						table.appendChild(tFoot);
						
					cardContent.appendChild(table);
				
				cardHeader.appendChild(cardContent);
			
			fieldsTableDiv.appendChild(cardHeader);
		return fieldsTableDiv;
	}

	private Tr wrapIssue(Issue issue) {
		Tr row = new Tr();
		
		Td showHideColumn = this.wrapShowHideIcon();
		row.appendChild(showHideColumn);
		
		Td idColumn = this.wrapId(issue);
		row.appendChild(idColumn);
		
		Td lastChanged = this.wrapLastChanged(issue);
		row.appendChild(lastChanged);
		
		Td summaryColumn = this.wrapSummary(issue);
		row.appendChild(summaryColumn);
		
		Td statusColumn = this.wrapStatus(issue);
		row.appendChild(statusColumn);
		
		Td severityColumn = this.wrapSeverity(issue);
		row.appendChild(severityColumn);
		
		Td priorityColumn = this.wrapPriority(issue);
		row.appendChild(priorityColumn);

		Td commitsColumn = this.wrapCommits(issue);
		row.appendChild(commitsColumn);		

		Td urlColumn = this.wrapUrl(issue);
		row.appendChild(urlColumn);
		
		return row;
	}
	
	private Td wrapShowHideIcon() {
		Td td = new Td();
		td.setCSSClass("details-control");
		return td;
	}
	
	private Td wrapId(Issue issue) {
		Td td = new Td();
		td.appendText(issue.id());
		return td;
	}
	
	private Td wrapLastChanged(Issue issue) {
		Td td = new Td();
		td.appendText(issue.lastChanged());
		return td;
	}
	
	private Td wrapSummary(Issue issue) {
		Td td = new Td();
		td.appendText(issue.summary());
		return td;
	}
	
	private Td wrapStatus(Issue issue) {
		Td td = new Td();
		td.appendText(issue.status());
		return td;
	}
	
	private Td wrapSeverity(Issue issue) {
		Td td = new Td();
		td.appendText(issue.severity());
		return td;
	}
	
	private Td wrapPriority(Issue issue) {
		Td td = new Td();
		td.appendText(issue.priority());
		return td;
	}
	
	private Td wrapCommits(Issue issue) {
		Td td = new Td();
		td.appendText(issue.associatedCommitsString());
		return td;
	}
	
	private Td wrapUrl(Issue issue) {
		Td td = new Td();
		A link = new A();
		link.setHref(issue.url());
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		td.appendChild(link);
		return td;
	}

}

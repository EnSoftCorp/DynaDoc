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
import com.kcsl.dynadoc.data.Commit;
import com.kcsl.dynadoc.data.JavaClass;

public class ClassCommitsWrapper {
	
	private static final String COMMITS_TABLE_JAVASCRIPT_FILE_NAME = "jquery-commits-table-script.js";
	
	private static final String COMMIT_SECTION_HEADER = "Revision Control Summary";
	
	private static final String COMMITS_TABLE_NAME = "commits-table";
	
	private static final String [] COMMITS_TABLE_HEADERS = { "Commit Id", "Commiter", "Date/Time", "Summary", "Related Issues", "View Commit" };

	private List<Commit> commits;
	
	public ClassCommitsWrapper(JavaClass javaClass) {
		this.commits = javaClass.getCommits();
	}
	
	private List<Commit> getCommits() {
		return this.commits;
	}
	
	public Div wrap() {
		Div tableDiv = new Div();
		tableDiv.setCSSClass("card text-white bg-warning mb-3");
		tableDiv.setStyle("max-width: 98%; margin: 10pt");
		
			Div cardHeader = new Div();
			cardHeader.setCSSClass("card-header");
			cardHeader.appendText(COMMIT_SECTION_HEADER);
			
				Div cardContent = new Div();
				cardContent.setCSSClass("card-body bg-white text-dark");
				
					Table table = new Table();
					table.setId(COMMITS_TABLE_NAME);
					table.setCSSClass("display small");
					table.setStyle("width:100%");
						
						Thead tHead = new Thead();
							Tr tr = new Tr();
								Th firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(String headerText: COMMITS_TABLE_HEADERS) {
									Th column = new Th();
									column.appendText(headerText);
									tr.appendChild(column);
								}

							tHead.appendChild(tr);
						table.appendChild(tHead);
						
						Tbody tBody = new Tbody();
						
						List<Commit> commits = this.getCommits();
						for(Commit commit: commits) {
							Tr commitRow = this.wrapCommit(commit);
							tBody.appendChild(commitRow);
						}
						table.appendChild(tBody);
						
						Tfoot tFoot = new Tfoot();
							tr = new Tr();
								firstColumn = new Th();
								tr.appendChild(firstColumn);
								
								for(int i = 0; i < COMMITS_TABLE_HEADERS.length; i++) {
									Th column = new Th();
									tr.appendChild(column);
								}						
							tFoot.appendChild(tr);
						table.appendChild(tFoot);
						
					cardContent.appendChild(table);
				
				cardHeader.appendChild(cardContent);
			
			tableDiv.appendChild(cardHeader);
		return tableDiv;
	}

	private Tr wrapCommit(Commit commit) {
		Tr row = new Tr();
		
		Td showHideColumn = this.wrapShowHideIcon();
		row.appendChild(showHideColumn);
		
		Td idColumn = this.wrapId(commit);
		row.appendChild(idColumn);
		
		Td commiterColumn = this.wrapCommiter(commit);
		row.appendChild(commiterColumn);
		
		Td dateTimeColumn = this.wrapDateTime(commit);
		row.appendChild(dateTimeColumn);
		
		Td summaryColumn = this.wrapSummary(commit);
		row.appendChild(summaryColumn);
		
		Td issuesColumn = this.wrapIssues(commit);
		row.appendChild(issuesColumn);

		Td urlColumn = this.wrapUrl(commit);
		row.appendChild(urlColumn);
		
		return row;
	}
	
	private Td wrapShowHideIcon() {
		Td td = new Td();
		td.setCSSClass("details-control");
		return td;
	}
	
	private Td wrapId(Commit commit) {
		Td td = new Td();
		td.appendText(commit.id());
		return td;
	}
	
	private Td wrapCommiter(Commit commit) {
		Td td = new Td();
		td.appendText(commit.commiter());
		return td;
	}
	
	private Td wrapDateTime(Commit commit) {
		Td td = new Td();
		td.appendText(commit.dateTime());
		return td;
	}
	
	private Td wrapSummary(Commit commit) {
		Td td = new Td();
		td.appendText(commit.summary());
		return td;
	}
	
	private Td wrapIssues(Commit commit) {
		Td td = new Td();
		td.appendText(commit.associatedIssuesString());
		return td;
	}
	
	private Td wrapUrl(Commit commit) {
		Td td = new Td();
		A link = new A();
		link.setHref(commit.url());
		link.setTarget("_blank");
		link.setAttribute("role", "button");
		link.setAttribute("class", "btn btn-primary");
		link.appendText("Show");
		td.appendChild(link);
		return td;
	}

}

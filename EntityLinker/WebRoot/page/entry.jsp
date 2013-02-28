<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page language="java"
	import="com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO"%>
<%@ page language="java"
	import="com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO"%>



<%
    String path = request.getContextPath();
    String basePath =
        request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";

    List<ResultDAO> resultSub = null;
    List<ResultDAO> resultObj = null;
    List<ResultDAO> resultPredLookup = null;
    List<ResultDAO> resultPredSearch = null;

    List<SuggestedFactDAO> retListSuggstFacts = null;

    
    if (request.getAttribute("matchingListSubj") != null) {
        resultSub = (List<ResultDAO>) request.getAttribute("matchingListSubj");
    }
    if (request.getAttribute("matchingListObj") != null) {
        resultObj = (List<ResultDAO>) request.getAttribute("matchingListObj");
    }
    if (request.getAttribute("matchingListPredLookup") != null) {
        resultPredLookup = (List<ResultDAO>) request.getAttribute("matchingListPredLookup");
    }
    if (request.getAttribute("matchingListPredSearch") != null) {
        resultPredSearch = (List<ResultDAO>) request.getAttribute("matchingListPredSearch");
    }
        
    if (request.getAttribute("suggestedFactList") != null) {
        retListSuggstFacts = (List<SuggestedFactDAO>) request.getAttribute("suggestedFactList");
    }
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<script language="javascript">
	var flag = false;

	function toggle(source) {
		for ( var i = 0; i < document.forms[0].checkbox.length; i++) {
			document.forms[0].checkbox[i].checked = source.checked;
		}
		saveSuggestions();
	}

	function ifChecked() {
		var ctr = 0;
		for ( var i = 0; i < document.forms[0].checkbox.length; i++) {
			if (document.forms[0].checkbox[i].checked) {
				ctr++;
			}
		}

		if (ctr == 0) {
			flag = false;
		} else {
			flag = true;
		}

		return flag;
	}

	function saveSuggestions() {

		var ele = document.getElementById('saveButtn');
		if (ele.style.display == "block" && !ifChecked()) {
			ele.style.display = "none";
		} else {
			ele.style.display = "block";
		}
	}

	function toggle4(showHideDiv) {
		var ele = document.getElementById(showHideDiv);
		if (ele.style.display == "block") {
			ele.style.display = "none";
		} else {
			ele.style.display = "block";
		}
	}

	function validateForm() {
		var errorMsg = new Array();

		var sub = document.forms["myForm"]["subject"].value;
		var obj = document.forms["myForm"]["object"].value;

		if (sub == null || sub == "" || sub == "Subject") {
			errorMsg[0] = "Please Enter Subject\n";
		}

		if (obj == null || obj == "" || obj == "Object") {
			errorMsg[1] = "Please Enter Object\n";
		}

		if (errorMsg != " ") {
			for ( var i = 0; i < errorMsg.length; i++) {
				document.getElementById("error").innerHTML += "<li >"
						+ errorMsg[i] + "</li><br>";
			}
			errorMsg = null;
			return false;

		}

	}

	function doPageSubmit() {
		document.getElementById('mode').value = 'suggest';
		document.forms["myForm"].submit();
	}

	function writeToDB() {
		document.getElementById('mode').value = '';
		document.forms["myForm"].submit();
	}
</script>

<meta name="keywords" content="" />
<meta name="description" content="" />
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title></title>
<link
	href="http://fonts.googleapis.com/css?family=Open+Sans+Condensed:300"
	rel="stylesheet" type="text/css" />
<link href="style/style.css" rel="stylesheet" type="text/css"
	media="screen" />
</head>


<body>
	<div id="header-wrapper">
		<div id="header" class="container">
			<div id="logo">
				<h1 class=HEADLINE>smartMATCH</h1>
				<h2 class=SUBHEADLINE>DBPedia knows it !</h2>

			</div>
		</div>

	</div>
	<form action="EntryServlet" method="post" name="myForm">

		<div id="error" style="color: #ff0000"></div>

		<table align="center">
			<!-- Search Fields -->
			<tr>
				<td><input class="style6" title="Enter your search subject"
					type="text"
					value="<%=(request.getAttribute("subject") != null) ? request.getAttribute("subject") : "Subject"%>"
					name="subject" />
				</td>
				<td><input class="style6" title="Enter your search predicate"
					type="text"
					value="<%=(request.getAttribute("predicate") != null) ? request.getAttribute("predicate") : "Predicate"%>"
					name="predicate" />
				</td>
				<td><input class="style6" type="text"
					title="Enter your search object"
					value="<%=(request.getAttribute("object") != null) ? request.getAttribute("object") : "Object"%>"
					name="object" />
				</td>


				<td><input type="submit" class="submit" title="Search" value="">
					<input type="button" class="button" title="Tweak search parameters"
					onclick="toggle4('box');"> <input type="submit"
					class="suggest" title="Suggest" value="" name="action"
					onclick="doPageSubmit();"> <input type="hidden" id="mode"
					name="action" value="smthng">
				</td>

			</tr>

			<!-- Parameter Extra Fields -->


			<tr>
				<td>
					<div id="box" style="display: none;padding: 5px;">
						<table>
							<tr>
								<td><h4 class=SUBHEADLINE2>Top K Results</h4></td>
								<td><input class="style5" title="Top K results" type="text"
									value="<%=(request.getAttribute("topk") != null) ? request.getAttribute("topk") : "3"%>"
									name="topk" size="40" /></td>
							</tr>
							<tr>
								<td><h4 class=SUBHEADLINE2>Similarity (%)</h4></td>
								<td><input class="style5" title="Similarity of atleast"
									type="text"
									value="<%=(request.getAttribute("sim") != null) ? request.getAttribute("sim") : "100"%>"
									name="sim" size="40" /></td>
							</tr>
						</table>
					</div></td>
			</tr>
			<!-- Results Feilds -->
			<tr>
				<!-- Subject -->
				<td>
					<%
					    if (resultSub != null) {
					%>
					<div style="height:400px; overflow-y:auto; overflow-x:hidden;">
						<table>

							<c:forEach items="<%= resultSub%>" var="matchingEntries">
								<tr>
									<td width="78%" style="word-wrap: break-word"><a
										style="color: #00a000"
										href=${matchingEntries.fieldURI
									} target="_blank">${matchingEntries.fieldURI}</a>
									</td>
									<td width="22%" align="center" style="color: #ffffff"><input
										type="checkbox" name="checkboxSubjs" id="checkbox_id"
										value='${matchingEntries.fieldURI}' onclick="" /></td>
								</tr>
							</c:forEach>
						</table>
					</div> <%
     }
 %>
				</td>

				<!-- Predicate -->

				<td>
					<%
					    if (resultPredLookup != null) {
					%>
					<div style="height:400px; overflow-y:auto; overflow-x:hidden;">
						<table>
							<c:forEach items="<%= resultPredLookup%>" var="matchingEntries">
								<tr>
									<td width="78%" style="word-wrap: break-word"><a
										style="color: #FFFFFF" target="_blank"
										href=${matchingEntries.fieldURI}>${matchingEntries.fieldURI}</a>
									</td>
									<td width="22%" align="center" style="color: #ffffff;"><input
										type="checkbox" name="checkboxPredLookup" id="checkbox_id"
										value='${matchingEntries.fieldURI}' onclick="" />
									</td>
								</tr>
							</c:forEach>
							<%
							    }
							    if (resultPredSearch != null) {
							%>
							<c:forEach items="<%= resultPredSearch%>" var="matchingEntries">
								<tr>
									<td width="78%" style="word-wrap: break-word"><a
										style="color: #00A000" target="_blank"
										href=${matchingEntries.fieldURI}>${matchingEntries.fieldURI}</a>
									</td>
									<td width="22%" align="center" style="color: #ffffff;"><input
										type="checkbox" name="checkboxPredSearch" id="checkbox_id"
										value='${matchingEntries.fieldURI}' onclick="" />
									</td>
								</tr>
							</c:forEach>
							<%
							    } else {
							%>
							<tr>
								<td width="78%" style="word-wrap: break-word">&nbsp;</td>
								<td width="22%" align="center" style="color: #ffffff;">&nbsp;</td>
							</tr>
							<%
							    }
							%>
						</table>
					</div>
				</td>

				<!-- object -->
				<td>
					<%
					    if (resultObj != null) {
					%>
					<div style="height:400px; overflow-y:auto; overflow-x:hidden;">

						<table>
							<c:forEach items="<%= resultObj%>" var="matchingEntries">
								<tr>
									<td width="78%" style="word-wrap: break-word"><a
										style="color: #00a000" target="_blank"
										href=${matchingEntries.fieldURI}>${matchingEntries.fieldURI}</a>
									</td>
									<td width="22%" align="center" style="color: #ffffff"><input
										type="checkbox" name="checkboxObjs" id="checkbox_id"
										value='${matchingEntries.fieldURI}' onclick="" /></td>
								</tr>
							</c:forEach>
						</table>
					</div> <%
     }
 %>
				</td>
			</tr>
		</table>




		<!-- FACT SUGGESTION -->
		<%
		    if (retListSuggstFacts != null) {
		%>

		<Table>
			<Tr>
				<td width=25%>
					<Table width=20%>
						<Tr>
							<td><h2 class=SUBHEADLINE3>Suggestions</h2>
							</td>
							<td align=right>
								<div id="saveButtn" style="display: none;padding: 5px;">
									<input type="submit" name="action" value="" class="save"
										title="save to database" onclick="writeToDB()">
								</div>
							</td>
						</Tr>
					</Table>
				</Td>
				<td></td>
			</Tr>
		</Table>

		<input type="checkbox" onClick="toggle(this)" /> <FONT
			COLOR="#66a266"> Check All</FONT> <br />
		<div
			style="width:80%; height:300px; overflow-y:auto; overflow-x:hidden;">
			<table>
				<c:forEach items="<%= retListSuggstFacts%>" var="matchingEntries">
					<tr>
						<td width="5%" align="center" style="color: #ffffff"><input
							type="checkbox" name="checkbox" id="checkbox_id"
							value='${matchingEntries.subject}~${matchingEntries.predicate}~${matchingEntries.object}'
							onclick="saveSuggestions()" /></td>
						<td width="23%" align="right"
							style="word-wrap: break-word; color:#00a000; font-size: 12pt;">${matchingEntries.subject}</td>
						<td width="23%" align="center"
							style="word-wrap: break-word; color:#00a000; font-size: 12pt;">${matchingEntries.predicate}</td>
						<td width="23%" align="left"
							style="word-wrap: break-word; color:#00a000; font-size: 12pt;">${matchingEntries.object}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<%
		    }
		%>



	</form>


</body>

<%--<div class="footer" style="position: static;">
	<a rel="license"
		href="http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_US"><img
		alt="Creative Commons License" style="border-width:0"
		src="http://i.creativecommons.org/l/by-nc-sa/3.0/88x31.png" /> </a> This
	work is licensed under a <a style="color: green;" rel="license"
		href="http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_US">Creative
		Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License</a>.
</div>

--%>
</html>


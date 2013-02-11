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

	function ifChecked(){
	
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
	
	function toggl(showHideDiv) {
		
		var ele = document.getElementById(showHideDiv);
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

		<table>
			<!-- Search Fields -->
			<tr>
				<td><input class="style6" title="Enter your search subject"
					type="text"
					value="<%=(request.getAttribute("subject") != null) ? request.getAttribute("subject") : "Subject"%>"
					onClick="(this.value='')" name="subject" />
				</td>
				<td><input class="style6" title="Enter your search predicate"
					type="text"
					value="<%=(request.getAttribute("predicate") != null) ? request.getAttribute("predicate") : "Predicate"%>"
					onClick="(this.value='')" name="predicate" />
				</td>
				<td><input class="style6" type="text"
					title="Enter your search object"
					value="<%=(request.getAttribute("object") != null) ? request.getAttribute("object") : "Object"%>"
					onClick="(this.value='')" name="object" />
				</td>


				<td><input type="submit" class="submit" title="Search" value="">
					<input type="button" class="button" title="Tweak search parameters"
					onclick="toggle4('box');"></td>
			</tr>

			<!-- Parameter Extra Fields -->


			<tr>
				<td>
					<div id="box" style="display: none;padding: 5px;">
						<table>
							<!-- 
							<tr>
								<td><h4 class=SUBHEADLINE2>Similarity</h4></td>
								<td><input class="style5" title="Set threshold similarity"
									type="text" value="0.5" name="sim" size="40" /></td>
							</tr>
							 -->
							<tr>

								<td><h4 class=SUBHEADLINE2>Top K Results</h4></td>
								<td><input class="style5" title="Top K results" type="text"
									value="<%=(request.getAttribute("topk") != null) ? request.getAttribute("topk") : "5"%>"
									name="topk" size="40" /></td>
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
									<td width="22%" align="center" style="color: #ffffff">${matchingEntries.score}</td>
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
									<td width="22%" align="center" style="color: #ffffff;">${matchingEntries.score}</td>
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
									<td width="22%" align="center" style="color: #ffffff;">${matchingEntries.score}</td>
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
									<td width="22%" align="center" style="color: #ffffff">${matchingEntries.score}</td>
								</tr>
							</c:forEach>

						</table>
					</div> <%
     }
 %>
				</td>

			</tr>
		</table>



		<%
		    if (retListSuggstFacts != null) {
		%>

		<h2 align="left" class=SUBHEADLINE3>Suggestions</h2>

		<div style="height:400px; overflow-y:auto; overflow-x:hidden;">
			<table>
				<c:forEach items="<%= retListSuggstFacts%>" var="matchingEntries">
					<tr width="70%">
						<td width="1%" align="right" style="color: #ffffff"><input
							type="checkbox" name="checkbox" id="checkbox_id"
							value='${matchingEntries.subject}~${matchingEntries.predicate}~${matchingEntries.object}'
							onclick="toggl('saveButtn')" /></td>
						<td width="23%" align="right"
							style="color:#00a000; font-size: 15pt;">${matchingEntries.subject}</td>
						<td width="23%" align="center"
							style="color:#00a000; font-size: 15pt;">${matchingEntries.predicate}</td>
						<td width="23%" align="left"
							style="color:#00a000; font-size: 15pt;">${matchingEntries.object}</td>
					</tr>
				</c:forEach>
			</table>
			<div id="saveButtn" style="display: none;padding: 5px;">
				<input type="submit" name="action" value="Save Facts">
			</div>
		</div>
		<%
		    }
		%>
	</form>
</body>


<body>
	<div id="wrapper">
		<div id="wrapper-bgbtm">

			<!-- end #page -->

			<!-- end #footer -->
		</div>
	</div>
</body>


</html>


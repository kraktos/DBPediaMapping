<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page language="java" import="com.mapper.dataObjects.ResultDAO"%>



<%
    String path = request.getContextPath();
    String basePath =
        request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";

    List<ResultDAO> resultSub = null;
    List<ResultDAO> resultObj = null;
    List<ResultDAO> resultPredLookup = null;
    List<ResultDAO> resultPredSearch = null;

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
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<script language="javascript">
	function toggle4(showHideDiv) {
		var ele = document.getElementById(showHideDiv);
		if (ele.style.display == "block") {
			ele.style.display = "none";
		} else {
			ele.style.display = "block";
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
<link href="style.css" rel="stylesheet" type="text/css" media="screen" />
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
	<form action="EntryServlet" method="post">

		<table>
			<!-- Search Fields -->
			<tr>
				<td><input class="style6" title="Enter your search subject"
					type="text"
					value="<%=(request.getAttribute("subject") != null) ? request.getAttribute("subject") : "Subject"%>"
					onClick="(this.value='')" name="subject" /></td>
				<td><input class="style6" title="Enter your search predicate"
					type="text"
					value="<%=(request.getAttribute("predicate") != null) ? request.getAttribute("predicate") : "Predicate"%>"
					onClick="(this.value='')" name="predicate" /></td>
				<td><input class="style6" type="text"
					title="Enter your search object"
					value="<%=(request.getAttribute("object") != null) ? request.getAttribute("object") : "Object"%>"
					onClick="(this.value='')" name="object" /></td>


				<td><input type="submit" class="submit" title="Search" value="">
					<input type="button" class="button" title="Tweak search parameters"
					onclick="toggle4('box');">
				</td>
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

								<td><h4 class=SUBHEADLINE2>Top K Results</h4>
								</td>
								<td><input class="style5" title="Top K results" type="text"
									value="<%=(request.getAttribute("topk") != null) ? request.getAttribute("topk") : "5"%>"
									name="topk" size="40" />
								</td>
							</tr>
						</table>
					</div>
				</td>
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
							    if (resultPredSearch != null){
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
							    }else{
							%>
							<tr>
								<td width="78%" style="word-wrap: break-word">&nbsp;</td>
								<td width="22%" align="center" style="color: #ffffff;">&nbsp;</td>
							</tr>
							<%} %>
						</table>
					</div></td>

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


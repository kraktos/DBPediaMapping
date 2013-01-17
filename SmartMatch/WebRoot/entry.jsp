<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page language="java" import="com.mapper.relationMatcher.ResultDAO"%>



<%
    String path = request.getContextPath();
    String basePath =
        request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";

    List<ResultDAO> resultSub = null;
    List<ResultDAO> resultObj = null;
    List<ResultDAO> resultPred = null;

    if (request.getAttribute("matchingListSubj") != null) {
        resultSub = (List<ResultDAO>) request.getAttribute("matchingListSubj");
    }
    if (request.getAttribute("matchingListObj") != null) {
        resultObj = (List<ResultDAO>) request.getAttribute("matchingListObj");
    }
    if (request.getAttribute("matchingListPred") != null) {
        resultPred = (List<ResultDAO>) request.getAttribute("matchingListPred");
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
				<h1 class=HEADLINE>
					<a
						href="http://lski-001.informatik.uni-mannheim.de:8080/SmartMatch/">smartMATCH
					</a>
				</h1>
				<h2 class=SUBHEADLINE>
					<p>DBPedia knows it !</p>
				</h2>

			</div>
		</div>

	</div>
	<form action="EntryServlet" method="post">

		<table>
			<!-- Search Fields -->
			<tr>
				<td><input class="style6" title="Enter your search subject"
					type="text" value="Subject" onClick="(this.value='')"
					name="subject" />
				</td>
				<td><input class="style6" title="Enter your search predicate"
					type="text" value="Predicate" onClick="(this.value='')"
					name="predicate" />
				</td>
				<td><input class="style6" type="text"
					title="Enter your search object" value="Object"
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
						<input class="style6" title="Set threshold similarity" type="text"
							value="0.5" name="sim" size="40" /> <input class="style6"
							title="Top K results" type="text" value="5" name="topk" size="40" />
					</div></td>


			</tr>
			<!-- Results Feilds -->
			<tr>
				<!-- Subject -->
				<td>
					<%
					    if (resultSub != null) {
					%>
					<table>
						<c:forEach items="<%= resultSub%>" var="matchingEntries">
							<tr>
								<td width="85%" style="word-wrap: break-word"><a
									style="color: #00a000" href=${matchingEntries.fieldURI}>${matchingEntries.fieldURI}</a>
								</td>
								<td width="15%" align="center" style="color: #ffffff">${matchingEntries.score}</td>
							</tr>
						</c:forEach>
					</table> <%
     }
 %>
				</td>

				<!-- Predicate -->

				<td>
					<%
					    if (resultPred != null) {
					%>
					<table>
						<c:forEach items="<%= resultPred%>" var="matchingEntries">
							<tr>
								<td width="85%" style="word-wrap: break-word"><a
									style="color: #00a000" href=${matchingEntries.fieldURI}>${matchingEntries.fieldURI}</a>
								</td>
								<td width="15%" align="center" style="color: #ffffff;">${matchingEntries.score}</td>
							</tr>
						</c:forEach>
					</table> <%
     }
 %>
				</td>

				<!-- object -->
				<td>
					<%
					    if (resultObj != null) {
					%>
					<table>


						<c:forEach items="<%= resultObj%>" var="matchingEntries">
							<tr>
								<td style="word-wrap: break-word" width="85%"><a
									style="color: #00a000" href=${matchingEntries.fieldURI}>${matchingEntries.fieldURI}</a>
								</td>
								<td width="15%" align="center" style="color: #ffffff">${matchingEntries.score}</td>
							</tr>
						</c:forEach>
					</table> <%
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


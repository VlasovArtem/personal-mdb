
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="../include.jsp"%>
<html>
<head>
    <title>Admin Panel</title>
</head>
<body>
<header>
    <%@include file="../navbar.jsp"%>
</header>
<section id="added-series" class="container">
    <h2 class="center">User Series</h2>
    <div class="col-md-12 center admin-add-series">
        <span class="error"></span>
    </div>
    <article class="col-md-offset-2 col-md-8">
        <table class="table table-striped ">
            <tr>
                <th>Series title</th>
                <th></th>
            </tr>
            <c:forEach items="${userSeries}" var="ser">
                <tr>
                    <td width="60%">
                        <input type="text" placeholder="Input title" value="${ser.title}" name="newTitle" id="${ser.id}"
                               class="form-control">
                    </td>
                    <td class="center">
                        <input type="button" class="btn btn-sm btn-success" value="Parse"
                               onclick="parseSeries('${ser.id}')">
                        <input type="button" class="btn btn-sm btn-warning" value="Update Title"
                               onclick="updateTitle('${ser.id}')">
                        <input type="button" class="btn btn-sm btn-danger" value="Delete"
                               onclick="deleteUserService('${ser.id}')">
                    </td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${fn:length(userSeries) > 0}">
            <div class="center">
                <input type="button" class="btn btn-success" value="Parse all series" onclick="parseAllSeries()">
            </div>
        </c:if>
    </article>
</section>
</body>
</html>

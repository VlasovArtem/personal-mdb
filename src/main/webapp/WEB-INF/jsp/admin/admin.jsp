
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
    <article class="col-md-offset-2 col-md-8">
        <table class="table table-striped ">
            <tr>
                <th>Series title</th>
                <th></th>
            </tr>
            <c:forEach items="${userSeries}" var="ser">
                <tr>
                    <td width="70%">${ser.title}</td>
                    <td
                            class="center"><input type="button" class="btn btn-success" value="Parse"
                                                  onclick="parseSeries(${ser.id})"></td>
                </tr>
            </c:forEach>
        </table>
    </article>
</section>
</body>
</html>

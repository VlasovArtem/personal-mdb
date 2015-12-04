<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="include.jsp"%>
<html>
<head>
    <title>Login</title>
</head>
<body>
<header>
    <%@include file="navbar.jsp"%>
</header>
<section id="login">
    <article class="col-md-offset-4 col-md-4">
        <form action="/login" method="post">
            <div class="form-group">
                <label for="loginData">Username or Email</label>
                <input type="text" class="form-control" placeholder="Input username or email..." id="loginData"
                       name="loginData">
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" placeholder="Input password..." class="form-control"
                       name="password">
            </div>
            <div class="form-group center">
                <input type="submit" value="Sign in" class="btn btn-success">
            </div>
        </form>
    </article>
</section>
</body>
</html>

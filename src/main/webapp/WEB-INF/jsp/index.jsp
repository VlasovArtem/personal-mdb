<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Month" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="include.jsp"%>
<!doctype html>
<html lang="en">
<head>
    <title>Personal IMDB API</title>
    <script>
        hljs.initHighlightingOnLoad();
    </script>
</head>
<body>
<header id="header-info">
    <nav class="navbar navbar-default pmdb-navbar">
        <div class="container">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                        data-target="#pmdb-collapse"
                        aria-expanded="false">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#header-info">Personal MDB<span class="line"></span></a>
            </div>
            <div class="collapse navbar-collapse" id="pmdb-collapse">
                <ul class="nav navbar-nav">
                    <li><a href="#api-info">API info<span class="line"></span></a></li>
                    <li><a href="#parsed-series">Parsed series<span class="line"></span></a></li>
                    <li><a href="#change-log">Change Log<span class="line"></span></a></li>
                    <li><a href="#usage-example">Usage Example<span class="line"></span></a></li>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="https://github.com/VlasovArtem/personal-mdb">Github<span class="line"></span></a></li>
                </ul>
            </div>
        </div>
    </nav>
    <section class="container">
        <article>
            <section class="col-md-6">
                <h2>Personal MDB (Movie Database)</h2>
                This project was developed for my personal project that dedicated to Series Tracking. I haven`t found good
                parsers and api that can provide useful and fresh information about favorite Series from website
                www.imdb.com.
                <article class="alert alert-warning">
                    I should warn you, that this database does not contains all series form website IMDB.
                </article>
            </section>
            <section class="json-example col-md-6">
                <h2>Json Example</h2>
                Data which presents in code block will be received after request <i>/series?title=The Expanse</i>.
                This request will return full information about series with title "The Expanse".
                <article class="code">
                    <pre>
                    <code class="lang-json" >
                        <%@ include file="../../resources/example/series-example.json"%>
                    </code>
                        </pre>
                </article>
            </section>
        </article>
    </section>
</header>
<section class="container" id="api-info">
    <article>
        <h2>API Info</h2>
        <table class="table table-striped">
            <tr>
                <th>Parameter</th>
                <th>Base URI</th>
                <th>Required</th>
                <th>Values</th>
                <th>Description</th>
                <th>Example</th>
            </tr>
            <tr>
                <td>title</td>
                <td>/series</td>
                <td><span class="true">Yes</span></td>
                <td>Find by specified title</td>
                <td>Any title</td>
                <td><i>/series?title=Grimm</i> - find Series with title Grimm, json will contains full information</td>
            </tr>
            <tr>
                <td>year</td>
                <td>/series/short</td>
                <td><span class="optional">Optional*</span></td>
                <td>Find series by specified year</td>
                <td>2000 - 2029</td>
                <td><i>/series/short?year=2015</i><br>find all Series that released year is 2015</td>
            </tr>
            <tr>
                <td>genre</td>
                <td>/series/short</td>
                <td><span class="optional">Optional*</span></td>
                <td>Find series that associated with specified genre</td>
                <td>Romance, Horror, Adventure, Music, Sci-Fi, Crime, Mystery, Fantasy, Biography, Action,
                    Thriller, Drama, Comedy</td>
                <td><i>/series/short?genre=action</i><br>find all Series that associated with action genre</td>
            </tr>
            <tr>
                <td>sort</td>
                <td>/series/short</td>
                <td><span class="no">No</span></td>
                <td>Sort series by specified property.</td>
                <td>year, rating, title</td>
                <td><i>/series/short?year=2015&sort=rating</i><br>find all Series that released in 2015 year and sort by
                    imdb rating in descending order</td>
            </tr>
            <tr>
                <td>order</td>
                <td>/series/short</td>
                <td><span class="no">No</span></td>
                <td>sort property in particular order (default value is descending order). This parameter
                    make sense if sort property is specified.</td>
                <td>asc, desc</td>
                <td><i>/series/short?genre=Action&sort=title&order=asc</i><br>find all Series that has Action
                    genre, sort by title in ascending order.</td>
            </tr>
            <tr>
                <td>hideFinished</td>
                <td>/series/short</td>
                <td><span class="no">No</span></td>
                <td>show series that is not finished yet (default value is false)</td>
                <td>true, false</td>
                <td><i>/series/short?year=2015&genre=drama&hideFinished=true</i><br>find all Series that released in 2015 year, has Drama genre and is not finished.</td>
            </tr>
        </table>
        <b>* Optional</b> - one of the parameters is required.<br>
        All base uri that contains <b>short</b> will return short information about series, without information about
        seasons and episodes.
    </article>
</section>
<section id="parsed-series" class="container">
    <article>
        <h2>Parsed Series</h2>
        This list contains all series that successfully parsed into our database. This list will be update every day.
        Today we have ${fn:length(series)} parsed Series.<br>
        <fieldset class="col-md-4">
            <legend>Series search</legend>
            <form class="form-inline" id="searchForm">
                <div class="form-group">
                    <input type="search" placeholder="Input series title" class="form-control">
                    <input type="button" class="btn btn-success" value="Search" onclick="search()">
                    <span class="result"></span>
                </div>
            </form>
        </fieldset>

        <details class="col-md-12">
            <summary>Available series:</summary>
            <table>
                <tr>
                    <td>
                        <c:forEach items="${series}" var="ser" begin="${fnfmt:largest(fn:length(series)/4 * 0)}"
                                   end="${fnfmt:largest(fn:length(series)/4 * 1)}">
                            <span class="col-md-12">${ser}</span>
                        </c:forEach>
                    </td>
                    <td>
                        <c:forEach items="${series}" var="ser" begin="${fnfmt:largest(fn:length(series)/4 * 1 + 1)}"
                                   end="${fnfmt:largest(fn:length(series)/4 * 2)}">
                            <span class="col-md-12">${ser}</span>
                        </c:forEach>
                    </td>
                    <td>
                        <c:forEach items="${series}" var="ser" begin="${fnfmt:largest(fn:length(series)/4 * 2 + 1)}"
                                   end="${fnfmt:largest(fn:length(series)/4 * 3)}">
                            <span class="col-md-12">${ser}</span>
                        </c:forEach>
                    </td>
                    <td>
                        <c:forEach items="${series}" var="ser" begin="${fnfmt:largest(fn:length(series)/4 * 3 + 1)}"
                                   end="${fnfmt:largest(fn:length(series)/4 * 4)}">
                            <span class="col-md-12">${ser}</span>
                        </c:forEach>
                    </td>
                </tr>
            </table>
        </details>
    </article>
</section>
<section id="usage-example" class="container">
    <article>
        <h2>Usage Example</h2>
        <div class="usage-example-block">
            <aside>
                <img src="${exampleSeries.posterUrl}" height="320">
            </aside>
            <article class="info-block">
                <div class="title col-md-12">${exampleSeries.title}</div>
                <div class="year col-md-12">(${exampleSeries.seriesStart.year} -
                    ${exampleSeries.seriesEnd == null ? "" : exampleSeries.seriesEnd.year}${exampleSeries.finished
                            ? "" : "..."})</div>
                <div class="rating"><a href="${exampleSeries.imdbUrl}"><img
                        src="http://www.janinebarris.com/images/imdb_logo.png"
                        height="30"></a><span
                        class="imdb-rating">${exampleSeries.imdbRating}</span></div>
                <c:if test="${exampleSeries.creators != null}">
                    <div class="creators"><b>Creators:</b> ${fnfmt:joining(exampleSeries.creators)}</div>
                </c:if>
                <c:if test="${exampleSeries.actors != null}">
                    <div class="actors"><b>Actors:</b> ${fnfmt:joining(exampleSeries.actors)}</div>
                </c:if>
                <div class="genres"><b>Genres:</b> ${fnfmt:joining(exampleSeries.genres)}</div>
                <div>
                    <div><b>Summary:</b></div>
                    <div>${exampleSeries.plot}</div>
                </div>
                <c:if test="${exampleSeries.nextEpisode != null}">
                    <div class="next-episode">
                        <b>Next episode:</b> ${exampleSeries.nextEpisode}
                    </div>
                </c:if>
            </article>
        </div>
    </article>
</section>
<section id="change-log" class="container">
    <article>
        <h2>Change Log</h2>
        <ul>
            <li><%= LocalDate.of(2015, Month.DECEMBER, 2)%></li>
            <ul>
                <li>Create main html page, that contains information about api</li>
            </ul>
        </ul>
    </article>
</section>
</body>
</html>

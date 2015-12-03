function search () {
    var searchTitle = $('#searchForm input[type=search]')[0].value;
    $.get("/search", "title=" + searchTitle)
        .done(function() {
            var result = $(".result");
            result.removeClass("error");
            result.addClass("success");
            result.html("Found");
        })
        .fail(function() {
            var result = $(".result");
            result.addClass("error");
            result.removeClass("success");
            result.html("Not Found");
        })

}
$(function() {
    $('a[href*=#]:not([href=#])').click(function() {
        if (location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'') && location.hostname == this.hostname) {
            var target = $(this.hash);
            target = target.length ? target : $('[name=' + this.hash.slice(1) +']');
            if (target.length) {
                $('html,body').animate({
                    scrollTop: target.offset().top
                }, 1000);
                return false;
            }
        }
    });
});
// Code to choose optimal parser for given url

(function () {
    "use strict";
    var url = new URL(window.Pareto.getUrl());
    console.log('choose_parser for url=' + url + ' host=' + url.host + ' search=' + url.search + ' pathname=' + url.pathname);

    // HN reply
    if (url.host === 'news.ycombinator.com' && url.pathname === "/reply") {
        console.log('choose html');
        return "html";
    }

    // HN aggregator on main page
    if (url.host === 'news.ycombinator.com' && url.search === "") {
        console.log('choose hn');
        return "hn";
    }

    // HN comments
    if (url.host === 'news.ycombinator.com' && url.pathname === "/item") {
        console.log('choose hn_comments');
        return "hn_comments";
    }

    console.log('choose readability');
    return "readability";
}());

/*
Minimalistic html sanitizer

document.body.innerHTML = Pareto.sanitize('<p onmouseover="alert(1)" onclick="alert(2)">click me</p>');
*/

var Pareto = window.Pareto || {};

Pareto.sanitize = function (html, documentUrl) {
    "use strict";

    function allowAttributes(element, allowedNames) {
        // remove attributes not on list
        var i;
        for (i = element.attributes.length - 1; i >= 0; i--) {
            if (allowedNames.indexOf(element.attributes[i].name) < 0) {
                element.removeAttribute(element.attributes[i].name);
            }
        }
    }

    function urlRelativeToAbsolute(urlString, documentUrl) {
        // convert relative url to absolute (url is not sanitized!)
        var u;
        if (!urlString) {
            return '';
        }
        // if url string starts with / it's relative path from root of document url (/something.html)
        if (urlString.substr(0, 1) === '/') {
            u = new URL(documentUrl);
            // if url starts with // just add protocol
            if (urlString.substr(0, 2) === '//') {
                return u.protocol + urlString;
            }
            u.pathname = urlString;
            u.search = '';
            u.hash = '';
            return u.toString();
        }
        // if url has no protocol it's relative url (something.html)
        if (!urlString.match(/^http[s]{0,1}:/)) {
            u = new URL(documentUrl + urlString);
            return u.toString();
        }
        // absolute url
        return urlString;
    }

    function urlSanitize(urlString, documentUrl) {
        // sanitize url
        var url, a;
        if (!urlString) {
            return '';
        }
        // make absolute url
        a = urlRelativeToAbsolute(urlString, documentUrl);
        try {
            url = (new URL(a.substr(0, 1000)));
        } catch (e) {
            console.warn('ignored invalid url', urlString);
            return '';
        }
        // allowed protocols
        if ((url.protocol === 'http:') || (url.protocol === 'https:')) {
            return url.toString();
        }
        console.warn('ignored protocol', urlString, documentUrl);
        return '';
    }

    // minimalistic html sanitizer based on DOMParser
    var p, d, e, walk, n, i, bad;
    bad = [];
    p = new window.DOMParser();

    // parse
    d = p.parseFromString(html, 'text/html');
    e = d.getElementsByTagName('parsererror');
    if (e.length > 0) {
        console.error(e.innerHTML);
        throw e.innerHTML;
    }

    // all nodes
    walk = d.createTreeWalker(d.body, window.NodeFilter.SHOW_ALL, null, false);
    n = walk.nextNode();
    while (n) {
        switch (n.nodeName) {
        case "#text":
            if (n.attributes) {
                throw "text node with attributes!";
            }
            break;
        case "A":
            n.setAttribute('pareto_url', urlSanitize(n.getAttribute('href'), documentUrl));
            allowAttributes(n, ['pareto_url', 'title', 'alt']); // note that href is banned and pareto_url is used instead
            break;
        case "IMG":
            allowAttributes(n, ['src', 'alt', 'width', 'height', 'title']);
            n.src = urlSanitize(n.getAttribute('src'), documentUrl);
            break;
        case "TIME":
            allowAttributes(n, ['datetime']);
            break;
        case "ABBR":
        case "ACRONYM":
        case "ARTICLE":
        case "B":
        case "BIG":
        case "BLOCKQUOTE":
        case "BR":
        case "CAPTION":
        case "CENTER":
        case "CITE":
        case "CODE":
        case "DD":
        case "DIV":
        case "DL":
        case "DT":
        case "EM":
        case "FIGCAPTION":
        case "FIGURE":
        case "H1":
        case "H2":
        case "H3":
        case "H4":
        case "H5":
        case "H6":
        case "HEADER":
        case "HR":
        case "I":
        case "LABEL":
        case "LEGEND":
        case "LI":
        case "OL":
        case "P":
        case "PRE":
        case "S":
        case "SAMP":
        case "SECTION":
        case "SMALL":
        case "SPAN":
        case "STRIKE":
        case "STRONG":
        case "SUB":
        case "SUP":
        case "TABLE":
        case "TBODY":
        case "TD":
        case "TFOOT":
        case "TH":
        case "THEAD":
        case "TR":
        case "U":
        case "UL":
            allowAttributes(n, []);
            break;
        default:
            // all other are removed
            // console.warn('removed', n.nodeName, n, s.length);
            bad.push(n);
        }
        n = walk.nextNode();
    }

    // remove all bad nodes
    for (i = bad.length - 1; i >= 0; i--) {
        bad[i].parentElement.removeChild(bad[i]);
    }

    return d.body.innerHTML;
};


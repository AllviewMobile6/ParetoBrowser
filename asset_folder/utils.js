// Various functions
"use strict";

var Pareto = window.Pareto || {};

Pareto.ajax = function (url, callback) {
    var r = new XMLHttpRequest();
    r.onreadystatechange = function () {
        if (r.readyState === 4) {
            callback(r.responseText);
        }
    };
    r.open('GET', url, true);
    r.send();
};

Pareto.contextMenu = function (items, callback) {
    var bg = document.createElement('div'),
        ul = document.createElement('ul'),
        li,
        i;
    function onClickLi(event) {
        callback(event.target.textContent);
    }
    for (i = 0; i < items.length; i++) {
        li = document.createElement('li');
        li.textContent = items[i];
        li.onclick = onClickLi;
        ul.appendChild(li);
    }
    bg.appendChild(ul);
    bg.className = 'contextMenu';
    bg.onclick = function () {
        bg.parentElement.removeChild(bg);
    };
    document.body.appendChild(bg);
};

Pareto.fixLinks = function (element) {
    // make links open in hidden browser
    function onClickLink(event) {
        event.preventDefault();
        event.stopPropagation = true;
        var s = event.target.getAttribute('pareto_url');
        Pareto.setUrl(s);
        Pareto.hiddenLoadUrl(s);
    }
    var i, a = element.getElementsByTagName('a');
    for (i = 0; i < a.length; i++) {
        if (a[i].getAttribute('pareto_url')) {
            a[i].onclick = onClickLink;
            a[i].href = a[i].getAttribute('pareto_url');
        }
    }
};


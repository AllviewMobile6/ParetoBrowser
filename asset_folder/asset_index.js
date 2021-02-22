// Main page
"use strict";
var Pareto = window.Pareto || {};

Pareto.bookmarkThis = function () {
    // Bookmark current page
    if (!Pareto.lastData.url) {
        alert('No url');
        return;
    }
    var s = prompt('Bookmark title', Pareto.lastData.title || Pareto.lastData.url),
        bookmarks = JSON.parse(Pareto.storageRead('bookmarks.json') || '{}');
    if (s) {
        bookmarks[Pareto.lastData.url] = s;
        delete bookmarks['undefined'];
        Pareto.storageWrite('bookmarks.json', JSON.stringify(bookmarks));
    }
};

Pareto.pageStarted = function () {
    // Called from java
    if (document.getElementById('render')) {
        document.getElementById('render').textContent = '';
    }
};

Pareto.renderData = function (aData) {
    // Render json data
    var o = JSON.parse(aData),
        div = document.getElementById('render');
    Pareto.lastData = o;
    if (!o) {
        div.textContent = 'Cannot parse page!';
        div.className = 'error';
        return;
    }
    console.log('archetype', o.archetype);
    div.textContent = '';
    div.className = o.archetype;
    Pareto.render[o.archetype](div, o);
    Pareto.fixLinks(div);
    Pareto.hideProgressBar();
};

if (Pareto.getUrl()) {
    Pareto.hiddenLoadUrl(Pareto.getUrl());
}

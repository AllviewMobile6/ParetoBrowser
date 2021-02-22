// Bookmarks
"use strict";

var Pareto = window.Pareto || {};

window.addEventListener('DOMContentLoaded', function () {
    var ul = document.getElementById('bookmarks'), li, a, k, del,
        bookmarks = JSON.parse(Pareto.storageRead('bookmarks.json') || '{}');

    function onClickDel(event) {
        var url = event.target.dataUrl;
        if (confirm('Delete ' + url)) {
            delete bookmarks[url];
            Pareto.storageWrite('bookmarks.json', JSON.stringify(bookmarks));
            event.target.parentElement.style.display = 'none';
        }
    }

    function onClickLink(event) {
        event.preventDefault();
        event.stopPropagation = true;
        var url = event.target.getAttribute('href');
        Pareto.setUrl(url);
        Pareto.visibleLoadUrl('file:///android_asset/asset_index.html');
    }

    if (Object.keys(bookmarks).length <= 0) {
        bookmarks = {
            "https://news.ycombinator.com/": "Hacker News"
        };
        //ul.textContent = 'No bookmarks yet. Use menu item "Bookmark this" to add active page to bookmarks.';
        //return;
    }

    // show bookmarks
    for (k in bookmarks) {
        if (bookmarks.hasOwnProperty(k)) {
            li = document.createElement('li');
            a = document.createElement('a');
            a.textContent = bookmarks[k];
            a.href = k;
            a.onclick = onClickLink;
            li.appendChild(a);

            del = document.createElement('span');
            del.textContent = 'x';
            del.style.color = 'red';
            del.style.float = 'right';
            del.dataUrl = k;
            del.onclick = onClickDel;
            li.appendChild(del);

            ul.appendChild(li);
        }
    }
});

// Aggregator renderer
"use strict";

var Pareto = window.Pareto || {};

Pareto.render = Pareto.render || {};

Pareto.render.aggregator = function (parent, data) {
    Pareto.storageMkDir('aggregator_hidden');
    var h1 = document.createElement('h1'),
        h2 = document.createElement('h2'),
        i,
        item,
        div,
        details,
        a,
        comments,
        submitter,
        menu,
        score,
        age,
        hidden_filename = 'aggregator_hidden/' + Pareto.sha256(data.url) + '.json',
        hidden = JSON.parse(Pareto.storageRead(hidden_filename) || '{}');

    console.log(hidden_filename, Object.keys(hidden).length);

    h1.textContent = data.title;
    parent.appendChild(h1);

    h2.textContent = data.author;
    parent.appendChild(h2);

    function onClickMenu(event) {
        var d = event.target.dataItem,
            e = event.target.dataDiv;
        Pareto.contextMenu(['Read article', 'Read comments', 'Hide'], function (menu_item) {
            if (menu_item === 'Read article') {
                Pareto.setUrl(d.url);
                Pareto.hiddenLoadUrl(d.url);
                return;
            }
            if (menu_item === 'Read comments') {
                Pareto.setUrl(d.comments_url);
                Pareto.hiddenLoadUrl(d.comments_url);
                return;
            }
            if (menu_item === 'Hide') {
                hidden[d.url] = 1;
                Pareto.storageWrite(hidden_filename, JSON.stringify(hidden));
                e.style.display = 'none';
                return;
            }
            console.log(menu_item, i);
        });
    }

    /*
    function onClickHide(event) {
        // hide item
        hidden[event.target.dataUrl] = 1;
        Pareto.storageWrite(hidden_filename, JSON.stringify(hidden));
        event.target.dataDiv.style.display = 'none';
    }
    */

    for (i = 0; i < data.items.length; i++) {
        item = data.items[i];

        if (hidden.hasOwnProperty(item.url)) {
            continue;
        }

        div = document.createElement('div');
        div.className = 'item';
        parent.appendChild(div);

        // menu
        menu = document.createElement('button');
        menu.className = 'menu';
        menu.textContent = '☰';
        menu.dataItem = item;
        menu.dataDiv = div;
        menu.onclick = onClickMenu;
        div.appendChild(menu);

        // summary

        a = document.createElement('a');
        a.textContent = item.summary;
        a.href = item.url;
        a.setAttribute('pareto_url', item.url);
        div.appendChild(a);

        // details

        details = document.createElement('div');
        details.className = 'details';
        div.appendChild(details);

        comments = document.createElement('a');
        comments.className = 'comments_count';
        comments.textContent = (item.comments_count || 0) + ' comments';
        comments.href = item.comments_url;
        comments.setAttribute('pareto_url', item.comments_url);
        details.appendChild(comments);

        age = document.createElement('span');
        age.className = 'age';
        age.textContent = item.age;
        details.appendChild(age);

        details.appendChild(document.createTextNode('by'));

        submitter = document.createElement('span');
        submitter.className = 'submitter';
        submitter.textContent = item.submitter || 'anon';
        details.appendChild(submitter);

        score = document.createElement('span');
        score.className = 'score';
        score.textContent = 'score ' + (item.score || 0);
        details.appendChild(score);

        /*
        hide = document.createElement('span');
        hide.className = 'hide';
        hide.textContent = 'hide';
        hide.onclick = onClickHide;
        hide.dataUrl = item.url;
        hide.dataDiv = div;
        details.appendChild(hide);
        */

    }

    //Pareto.fixLinks(c);
};


// Article parser for news.ycombinator.com with comments
(function () {
    "use strict";
    var i, comment_tree, com, texts, j, flat, max, final,
        togg,
        o = {
            archetype: "article",
            type: "article",
            version: 1,
            title: document.title.replace(' | Hacker News', ''),
            url: document.location.toString(),
            debug: 'hn_comments'
        };
    // article
    o.content = document.querySelector('.storylink').parentElement.innerHTML + '<br>' + document.querySelector('.fatitem').querySelector('TR:nth-child(4)').innerHTML;
    o.author = document.querySelector('.fatitem').querySelector('.hnuser').textContent;
    o.age = document.querySelector('.fatitem').querySelector('.age').textContent;
    // comments
    flat = [];
    comment_tree = document.querySelector('.comment-tree');
    com = comment_tree.querySelectorAll('.comment');
    for (i = 0; i < com.length; i++) {
        //console.log(com[i]);
        texts = [];
        for (j = 0; j < com[i].childNodes.length; j++) {
            //console.log(j, com[i].childNodes[j]);
            if (com[i].childNodes[j].innerHTML) {
                texts.push(com[i].childNodes[j].innerHTML);
            }
        }
        togg = com[i].parentElement.querySelector('.togg');
        //console.log('togg', togg, togg.getAttribute('onclick'));
        flat.push({
            id: togg && parseInt(togg.getAttribute('onclick').toString().match(/[0-9]+/)[0], 10),
            level: com[i].parentElement.parentElement.querySelector('.ind').firstElementChild.width / 12,
            content: texts.join(' '), //.substr(0, 20),
            author: com[i].parentElement.querySelector('.hnuser').textContent,
            age: com[i].parentElement.querySelector('.age').textContent,
            reply_url: com[i].parentElement.querySelector('.reply').querySelector('a').href
        });
    }
    //flat[3];
    // use indent levels to restore actual comment hierarchy
    max = 0;
    flat.map(function (a) { max = Math.max(max, a.level); });
    final = [];
    for (i = 0; i < flat.length; i++) {
        //console.log(i, flat[i].indent, max);
        //flat[i].children = [];
        flat[i].comments = [];
        for (j = i + 1; j < flat.length; j++) {
            if (flat[j].level === flat[i].level + 1) {
                //flat[i].children.push(flat[j].id);
                flat[i].comments.push(flat[j]);
                //flat[j].parentId = flat[i].id;
            }
            if (flat[j].level === flat[i].level) {
                break;
            }
        }
        if (flat[i].level === 0) {
            final.push(flat[i]);
        }
    }
    o.comments = final;
    //JSON.stringify(final, undefined, 4);
    return o;
}());


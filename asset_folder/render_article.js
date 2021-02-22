// Article renderer
"use strict";

var Pareto = window.Pareto || {};

Pareto.render = Pareto.render || {};

Pareto.render['article'] = function (parent, data) {
    var h1 = document.createElement('h1'),
        h2 = document.createElement('h2'),
        c = document.createElement('div'),
        img,
        comments,
        i,
        w,
        h;

    h1.textContent = data.title;
    parent.appendChild(h1);

    h2.textContent = (data.author || '') + ', ' + (data.age || '');
    if (data.author || data.age) {
        parent.appendChild(h2);
    }

    c.className = 'content';
    c.innerHTML = Pareto.sanitize(data.content, data.url);
    parent.appendChild(c);

    // make sure images don't overflow width while preserving aspect ratio
    img = c.getElementsByTagName('img');
    for (i = 0; i < img.length; i++) {
        w = parseFloat(img[i].getAttribute('width'));
        h = parseFloat(img[i].getAttribute('height'));
        if (w > 0 && h > 0 && w > parent.clientWidth) {
            img[i].style.maxWidth = parent.clientWidth + 'px';
            img[i].style.maxHeight = (h / w) * parent.clientWidth + 'px';
            img[i].style.position = 'relative';
            img[i].style.left = -img[i].getBoundingClientRect().left + 'px';
            //console.log(img[i].getBoundingClientRect().left);
        }
    }

    if (data.comments && data.comments.length > 0) {
        // comments
        comments = document.createElement('div');
        comments.className = 'comments';
        parent.appendChild(comments);

        h1 = document.createElement('h1');
        h1.textContent = 'Comments';
        comments.appendChild(h1);

        Pareto.render.comments(comments, data.comments, data.url);
    }
};


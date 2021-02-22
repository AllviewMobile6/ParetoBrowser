// Render comments (part of article or other archetypes)
"use strict";

var Pareto = window.Pareto || {};

Pareto.render = Pareto.render || {};

Pareto.render['comments'] = function (parent, data, url) {
    //console.log('comments', data, url);
    if (!data) {
        return;
    }
    var i, div, message, details, author, age, replies, collapse, reply;

    function onClickCollapse(event) {
        if (event.target.textContent === '⊖') {
            event.target.textContent = '⊕';
            event.target.dataDiv.style.maxHeight = '1.2em';
            event.target.dataDiv.style.overflowY = 'hidden';
        } else {
            event.target.textContent = '⊖';
            event.target.dataDiv.style.maxHeight = '';
            event.target.dataDiv.style.overflowY = '';
        }
    }

    function onClickReply(event) {
        Pareto.setUrl(event.target.dataUrl);
        Pareto.hiddenLoadUrl(event.target.dataUrl);
    }

    for (i = 0; i < data.length; i++) {
        div = document.createElement('div');
        div.className = 'comment level' + parseFloat(data[i].level);

        details = document.createElement('div');
        details.className = 'details';
        div.appendChild(details);

        collapse = document.createElement('span');
        collapse.className = 'collapse';
        collapse.textContent = '⊖'; //'[−] ';
        collapse.onclick = onClickCollapse;
        collapse.dataDiv = div;
        details.appendChild(collapse);

        author = document.createElement('span');
        author.className = 'author';
        author.textContent = data[i].author;
        details.appendChild(author);

        age = document.createElement('span');
        age.className = 'age';
        age.textContent = data[i].age;
        details.appendChild(age);

        message = document.createElement('div');
        message.className = 'message';
        message.innerHTML = Pareto.sanitize(data[i].content, url);
        div.appendChild(message);

        reply = document.createElement('a');
        reply.className = 'reply';
        reply.textContent = 'reply';
        reply.dataUrl = data[i].reply_url;
        reply.onclick = onClickReply;
        details.appendChild(reply);

        //console.log(data[i].comments);
        if (data[i].comments.length > 0) {
            replies = document.createElement('div');
            replies.className = 'replies';
            div.appendChild(replies);
            Pareto.render.comments(replies, data[i].comments, url);
        }

        parent.appendChild(div);
    }
};


(function () {
    "use strict";
    return {
        archetype: "article",
        version: 1,
        url: document.location.toString(),
        title: document.title,
        content: document.body.innerText,
        debug: 'plaintext'
    };
}());

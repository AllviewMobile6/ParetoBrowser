// Used for pages that are not supported
(function () {
    "use strict";
    return {
        archetype: "article",
        version: 1,
        url: document.location.toString(),
        title: document.title,
        content: 'This page can only be viewed using normal browser. In main menu tap on "Hidden/Visible" to switch between Pareto mode and normal web browser mode.',
        debug: 'html'
    };
}());

# What is pareto browser

Most websites can be reduced into few basic archetypes (news site aggregator, news article, discussion, ...). Pareto browser is browser that renders these archetypes. Every website of the same archetype looks and behaves the same, the only difference is content.

# Why we need new web

Modern web is pure garbage. When you visit new page, you have to close 2-3 popups in order to use the website (cookies, subscribe to newsletter, mobile app ad, full screen video ads). Then everything is in small, weirly shaped, ultra thin font in dark gray color on light gray background, barely readable. Blinking elements are everywhere, weird controls on weird places. Website force extra pages to history so when you press back button you have to go through several extra pages. It's a hostile UI mess. Eventually you start to hate it. That's why I created Pareto browser.

Currently Pareto browser is parsing HTML websites and converting them to Pareto websites. If pareto became successfull, it will be possible to bypass old web altogether if people start creating pareto websites directly. Then no conversion will be necessary.

# What types of pages are supported

## Type 1: Article

Typical news article, with pictures. You can read article. There can be comments at the bottom.

    {
    "archetype": "article",
    "version": 1,
    "url": "https://example.com/banana.html",
    "title": "Banana",
    "author": "John Doe",
    "content": "Banana is <b>yellow</b> vegetable.",
    "age": "2 hours ago",
    "comments": [
        {
            "id": 123,
            "level": 0,
            "author": "alice",
            "age": "3 hours ago",
            "content": "What's your favourite <b>color</b>?",
            "reply_url": "https://example.com/banana/reply?id=123",
            "comments": [
                {
                    "id": 125,
                    "level": 1,
                    "author": "bob",
                    "age": "4 hours ago",
                    "content": "Blue",
                    "reply_url": "https://example.com/banana/reply?id=125",
                    "comments": []
                }
            ],
        },
        {
            "id": 124,
            "level": 0,
            "author": "cindy",
            "age": "3 hours ago",
            "content": "Isn't banana a fruit?",
            "reply_url": "https://example.com/banana/reply?id=124",
            "comments": []
        }
    ]
    }

## Type 2: Aggregator

Typical news homepage with list of most recent news articles. You can read article or comments. You can hide articles.


    {
    "archetype": "agregator",
    "version": 1,
    "url": "https://example.com/news.html",
    "title": "Recent News",
    "author": "Jane Doe",
    "items": [
        {
            "url": "https://example.com/news/1234.html",
            "age": "2 hours ago",
            "summmary": "Banana shortage is upon us!",
            "score": 12,
            "submitter": "bananalover69",
            "comments_url": "https://example.com/comments/1234.html",
            "comments_count": 7
        },
        {
            "url": "https://example.com/news/4567.html",
            "age": "3 hours ago",
            "summmary": "New frog species discovered!",
            "score": 12,
            "submitter": "frog777",
            "comments_url": "https://example.com/comments/4567.html",
            "comments_count": 7
        }
    ]
    }


## What didn't make it to this MVP

- more archetypes (paywalled article, scientific paper, comment reply, slideshow, video, xy chart, bar chart)
- customized themes (after install user would choose black on white, white on black, or other color scheme, font family, font size, ...)
- decentralized, per-url commenting system, no login/registration needed, probably some simple proof-of-work antispam/scoring system
- native pareto websites (instead of html pages people would use json with given archetype, e.g. example.com/blog/ would be aggregator, example.com/blog/123.json would be article and Pareto browser would render them directly)
- blind support (text-to-speech whole page, speech-to-text navigation)
- "report bug" menu item that would report issue current website 
- allow adding custom parsers by users from menu
- remember scrolling (when I go back be scrolled down at the same place)
- multiple tabs support
- paging (to get more articles in aggregator)
- most pages could be parsed directly without need to render them in webview first, this could significantly speed up rendering
- more aggressive reformatting, e.g. convert <div><div><div>Paragraph<div><div><div> into <p>paragraph</p>
- most pages could be parsed before they are loaded but I could not know which and when, so I have to wait for page to fully load before it start parsing, this makes it slower to load page
- comment and article score
- anchor links should scroll, not reload page
- I only tested it on Hacker News

# What can you do

- fork it, make parsers for more websites
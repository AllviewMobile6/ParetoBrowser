// Check for updates
"use strict";

var Pareto = window.Pareto || {};

function updateNow(data) {
    // Apply update
    var p, d, domains;
    // save parsers
    Pareto.storageMkDir('parser');
    for (p in data.parser) {
        if (data.parser.hasOwnProperty(p)) {
            if (!Pareto.storageWrite('parser/' + p + '.js', data.parser[p])) {
                alert('Failed to update ' + 'parser/' + p + '.js');
            }
        }
    }
    // save choose_parser.js
    if (!Pareto.storageWrite('choose_parser.js', data.choose_parser)) {
        alert('Failed to update choose_parser.js');
    }

    // save domains (DEPRECATED?)
    domains = JSON.parse(Pareto.storageRead('domains.json') || '{}');
    for (d in data.domains) {
        if (data.domains.hasOwnProperty(d)) {
            domains[d] = data.domains[d];
        }
    }
    Pareto.storageWrite('domains.json', JSON.stringify(domains));

    // save update itself
    if (!Pareto.storageWrite('update/parser.json', JSON.stringify(data))) {
        alert('Failed to save backup update/parser.json');
    }
    //alert('Updated to ' + data.release);
    document.location.reload();
}

window.addEventListener('DOMContentLoaded', function () {
    Pareto.storageMkDir('update');

    var message = document.getElementById('message'),
        update = document.getElementById('update'),
        old = JSON.parse(Pareto.storageRead('update/parser.json') || '{"release": 0}');

    Pareto.ajax('http://192.168.0.1/pareto/update/parser.json', function (aData) {
        var cur = JSON.parse(aData), changes, k, li;
        console.log(cur);

        // updated
        if (old.release === cur.release) {
            message.textContent = 'Pareto browser is now fully updated, current release is ' + old.release + '.';
            return;
        }

        // show changes cur vs. old version
        changes = document.getElementById('changes');
        for (k in cur.changes) {
            if (cur.changes.hasOwnProperty(k)) {
                if (parseFloat(k) > old.release) {
                    li = document.createElement('li');
                    li.textContent = cur.changes[k];
                    changes.appendChild(li);
                }
            }
        }

        message.textContent = 'Do you want to update Pareto browser from version ' + old.release + ' to version ' + cur.release + '? Update size is ' + Math.ceil(aData.length / 1024) + ' kB, changes are:';
        update.style.display = 'block';
        update.onclick = function () {
            updateNow(cur);
        };

    });
});


// Show console data

var Pareto = window.Pareto || {};

window.addEventListener("DOMContentLoaded", function () {
    "use strict";
    var visible_data = Pareto.consoleData();
    var hidden_data = Pareto.consoleDataHidden();
    var output = document.getElementById("output");

    document.getElementById("visible").onclick = function () {
        output.textContent = visible_data;
    };

    document.getElementById("hidden").onclick = function () {
        output.textContent = hidden_data;
    };

    document.getElementById("history").onclick = function () {
        output.textContent = Pareto.debugHistory();
    };

    output.textContent = visible_data;
});


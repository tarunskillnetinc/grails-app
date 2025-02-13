
function addDays(date, days) {
    var result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

function removeAllOptions(select) {
    while (select.options.length > 0) {
        select.remove(0);
    }
    select.disabled = true;
}

function updateSelectionOptions(select, url, data) {
    $.ajax({
        url: url,
        data: data,
        statusCode: {
            401: function () {
                window.location.href = '/';
            },
            500: function () {
                select.appendChild(new Option('ERROR', ''));
            },
            200: function (response) {
                let options = response?.options;
                if (options && options.length > 0) {
                    select.appendChild(new Option('Please Select', ''));
                    options.forEach(function (option) {
                        select.appendChild(new Option(option.text, option.value));
                    });
                    select.removeAttribute("disabled");
                } else {
                    select.appendChild(new Option('No results', ''));
                }
            }
        }
    });
}

function renderCashReportResult(url, data) {
    var reportContainer = $('#report-container');

    $.ajax({
        url: url, data: data,
        statusCode: {
            401: function () {
                window.location.href = '/';
            },
            500: function () {
                $("#report-time").html('');
                reportContainer.html('');
                $("#error-container").html('<div class="alert alert-danger alert-wl mx-0 text-center" role="alert">Unexpected Error Occurred</div>');
            },
            200: function (response) {
                $("#error-container").html('');
                $("#report-time").html('<div class="alert alert-wl mx-0 font-weight-light text-right" role="alert">' +
                    'Report generated at ' + new Date().toLocaleTimeString() + ' on ' + new Date().toLocaleDateString() + '</div>');
                reportContainer.html(response);

                $('html, body').animate({scrollTop: reportContainer.position().top - 65}, 500);
            },
            204: function (response) {
                $("#report-time").html('');
                $("#report-container").html('');
                $("#error-container").html('<div class="alert alert-warning alert-wl mx-0 text-center" role="alert">No results found</div>');
            }
        }
    });
}
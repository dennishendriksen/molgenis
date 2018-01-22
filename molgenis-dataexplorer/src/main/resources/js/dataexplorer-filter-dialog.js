/**
 * Attribute filter modal
 *
 * Dependencies: dataexplorer.js
 *
 * @param $
 * @param molgenis
 */
(function ($, molgenis) {
    "use strict";

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    molgenis.dataexplorer.filter = molgenis.dataexplorer.filter || {};
    var self = molgenis.dataexplorer.filter.dialog = molgenis.dataexplorer.filter.dialog || {};

    self.openFilterModal = function (attribute, attributeFilter) {
        var modal = createFilterModal();
        var title = molgenis.getAttributeLabel(attribute);

        var description = attribute.description ? attribute.description : 'No description available';
        var controls = molgenis.dataexplorer.filter.createFilter(attribute, attributeFilter, false);

        $('.filter-title', modal).html(title);
        $('.filter-description', modal).html(description);
        $('.form-horizontal', modal).html(controls);

        modal.modal('show');
    };

    function createFilterModal() {
        var modal = $('#filter-modal');
        var filterTemplate = Handlebars.compile($("#filter-modal-template").html());
        modal = $(filterTemplate({}));
        createFilterModalControls(modal);
        return modal;
    }

    function createFilterModalControls(modal) {
        $('.filter-apply-btn', modal).unbind('click');
        $('.filter-apply-btn', modal).click(function () {
            var tokens = $('#query-area').val().split('\n');

            var form = $('form', modal);
            var container = $('.complex-filter-container', form);
            var attribute = container.first().data('attribute');
            var q = [];
            for (var i = 0; i < tokens.length; i++) {
                if (i > 0) {
                    q.push({
                        operator: 'OR'
                    });
                }
                q.push({
                    field: attribute.name,
                    operator: 'EQUALS',
                    value: tokens[i]
                })
            }
            $(document).trigger('changeQuery', {'q': q});
        });

        $(modal).unbind('shown.bs.modal');
        modal.on('shown.bs.modal', function () {
            $('form input:visible:first', modal).focus();
        });
    }
}($, window.top.molgenis = window.top.molgenis || {}));
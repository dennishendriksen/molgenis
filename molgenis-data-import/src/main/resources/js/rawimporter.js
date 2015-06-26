(function($, molgenis) {
	"use strict";

	var analyzedHeaders = {};
	
	$(function() {
		React.render(molgenis.ui.Input({
			type : 'file',
			name: 'file',
			required: true
		}), $('#file-input-container')[0]);
		
		$('form[name=raw-importer-analysis-form]').submit(function(e) {
			e.preventDefault();
			e.stopPropagation();
			$.ajax({
				type : $(this).attr('method'),
				url : $(this).attr('action'),
				contentType: false, // force jQuery not to add a Content-Type header
			    processData: false, // prevent jQuery from converting FormData into a string
				data : new FormData($(this)[0]), // not supported in IE9,
				processData: false
			}).done(function(data) {
				var items = [];
				items.push('<table class="table">');
				items.push('<thead>');
				items.push('<th>Header</th><th>Type</th>');
				items.push('</thead>');
				items.push('<tbody>');
				_.each(data.headers, function(header) {
					analyzedHeaders[header] = 'STRING';
					
					items.push('<tr>');
					items.push('<td>' + header + '</td>');
					items.push('<td>STRING</td>');
					items.push('</tr>');
				});
				items.push('</tbody>');
				items.push('</table>');
				$('#file-analysis-results').html(items.join(''));
			});
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
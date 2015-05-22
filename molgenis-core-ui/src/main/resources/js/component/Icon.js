var React = require("react/addons");
var DeepPureRenderMixin = require("./mixin/DeepPureRenderMixin");

"use strict";

var span = React.DOM.span;

/**
 * @memberOf component
 */
var Icon = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'Icon',
	propTypes: {
		name: React.PropTypes.string.isRequired
	},
	render: function() {
		return (
			span(null,
				span({className: 'glyphicon glyphicon-' + this.props.name, 'aria-hidden': true}),
				span({className: 'sr-only'}, this.props.name)
			)
		);
	}
});

module.exports = React.createFactory(Icon);
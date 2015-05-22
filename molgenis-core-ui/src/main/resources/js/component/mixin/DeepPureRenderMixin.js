/* global _: false, molgenis: true */

var _ = require("underscore");

"use strict";

/**
 * Only render components if their state or props changed
 *
 * @memberOf component.mixin
 */
var DeepPureRenderMixin = {
	shouldComponentUpdate: function(nextProps, nextState) {
		return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
	}
};
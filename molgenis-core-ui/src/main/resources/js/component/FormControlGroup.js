"use strict";

var React = require("react/addons");
var DeepPureRenderMixin = require("./mixin/DeepPureRenderMixin");
var AttributeLoaderMixin = require("./mixin/AttributeLoaderMixin");
var FormControl = require("./FormControl");
var FormControlGroup = require("./FormControlGroup");

var div = React.DOM.div, p = React.DOM.p, fieldset = React.DOM.fieldset, legend = React.DOM.legend;
;

/**
 * @memberOf component
 */
var FormControlGroup = React.createClass({
    mixins: [DeepPureRenderMixin, AttributeLoaderMixin],
    displayName: 'FormControlGroup',
    propTypes: {
        entity: React.PropTypes.object,
        entityInstance: React.PropTypes.object,
        attr: React.PropTypes.object.isRequired,
        value: React.PropTypes.object,
        mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
        formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
        colOffset: React.PropTypes.number,
        errorMessages: React.PropTypes.object.isRequired,
        focus: React.PropTypes.bool,
        onValueChange: React.PropTypes.func.isRequired,
        onBlur: React.PropTypes.func.isRequired
    },
    getInitialState: function () {
        return {
            attr: null
        };
    },
    render: function () {
        if (this.state.attr === null) {
            // attribute not available yet
            return molgenis.ui.Spinner();
        }
        var attributes = this.state.attr.attributes;

        // add control for each attribute
        var foundFocusControl = false;
        var controls = [];
        var hasVisible = false;
        for (var i = 0; i < attributes.length; ++i) {
            var attr = attributes[i];
            if ((attr.visibleExpression === undefined) || (this.props.entity.allAttributes[attr.name].visible === true)) {
                var ControlFactory = attr.fieldType === 'COMPOUND' ? FormControlGroup : FormControl;
                var controlProps = {
                    entity: this.props.entity,
                    attr: attr,
                    value: this.props.entityInstance ? this.props.entityInstance[attr.name] : undefined,
                    entityInstance: this.props.entityInstance,
                    mode: this.props.mode,
                    formLayout: this.props.formLayout,
                    colOffset: this.props.colOffset,
                    saveOnBlur: this.props.saveOnBlur,
                    validate: this.props.validate,
                    onValueChange: this.props.onValueChange,
                    onBlur: this.props.onBlur,
                    key: '' + i
                };

                if (attr.fieldType === 'COMPOUND') {
                    _.extend(controlProps, {
                        errorMessages: this.props.errorMessages,
                        hideOptional: this.props.hideOptional
                    });
                } else {
                    controlProps['errorMessage'] = this.props.errorMessages[attr.name];
                }
                // IE9 does not support the autofocus attribute, focus the first visible input manually
                if (!foundFocusControl && attr.visible === true) {
                    _.extend(controlProps, {focus: true});
                    foundFocusControl = true;
                }

                var Control = ControlFactory(controlProps);
                if (attr.nillable === true && this.props.hideOptional === true) {
                    Control = div({className: 'hide'}, Control);
                } else {
                    hasVisible = true;
                }
                controls.push(Control);
            }
        }

        var Fieldset = fieldset({},
            legend({}, this.props.attr.label),
            p({}, this.props.attr.description),
            div({className: 'row'},
                div({className: 'col-md-offset-1 col-md-11'},
                    controls
                )
            )
        );

        if (!hasVisible) {
            Fieldset = div({className: 'hide'}, Fieldset);
        }
        return Fieldset;
    }
});
module.exports = React.createFactory(FormControlGroup);
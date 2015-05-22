var React = require("react/addons");
var DeepPureRenderMixin = require("./mixin/DeepPureRenderMixin");
var FormControlGroup = require("./FormControlGroup");
var FormControl = require("./FormControl");

/**
 * @memberOf component
 */
var FormControls = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'FormControls',
    propTypes: {
        entity: React.PropTypes.object.isRequired,
        value: React.PropTypes.object,
        mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
        formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
        colOffset: React.PropTypes.number,
        hideOptional: React.PropTypes.bool,
        enableFormIndex: React.PropTypes.bool,
        errorMessages: React.PropTypes.object.isRequired,
        onValueChange: React.PropTypes.func.isRequired,
        onBlur: React.PropTypes.func.isRequired
    },
    render: function () {
        // add control for each attribute
        var foundFocusControl = false;
        var attributes = this.props.entity.attributes;
        var controls = [];
        for (var key in attributes) {
            if (attributes.hasOwnProperty(key)) {
                var attr = attributes[key];
                if ((this.props.mode !== 'create' || (this.props.mode === 'create' && attr.auto !== true)) &&
                    ((attr.visibleExpression === undefined) || (this.props.entity.allAttributes[attr.name].visible === true))) {
                    var ControlFactory = attr.fieldType === 'COMPOUND' ? FormControlGroup : FormControl;
                    var controlProps = {
                        entity: this.props.entity,
                        entityInstance: this.props.value,
                        attr: attr,
                        value: attr.fieldType === 'COMPOUND' ? this.props.value : (this.props.value ? this.props.value[key] : undefined),
                        mode: this.props.mode,
                        formLayout: this.props.formLayout,
                        colOffset: this.props.colOffset,
                        onBlur: this.props.onBlur,
                        onValueChange: this.props.onValueChange,
                        key: key
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
                    if (this.props.mode !== 'view' && !foundFocusControl && attr.visible === true && (this.props.mode === 'create' || attr.readOnly !== true)) {
                        _.extend(controlProps, {focus: true});
                        foundFocusControl = true;
                    }

                    var Control = ControlFactory(controlProps);
                    if (attr.nillable === true && this.props.hideOptional === true) {
                        Control = div({className: 'hide'}, Control);
                    } else if (this.props.enableFormIndex === true && attr.fieldType === 'COMPOUND') {
                        Control = div({id: this._getLinkId(attr)}, Control);
                    }
                    controls.push(Control);
                }
            }
        }
        return div({}, controls);
    },
    _getLinkId: function (attr) {
        return attr.name + '-link';
    }
});

module.exports = React.createFactory(FormControls);
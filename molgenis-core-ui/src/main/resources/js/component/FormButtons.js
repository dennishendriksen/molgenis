var React = require("react/addons");
var DeepPureRenderMixin = require("./mixin/DeepPureRenderMixin");
var Button = require("./Button");

/**
 * @memberOf component
 */
var FormButtons = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'FormButtons',
    propTypes: {
        mode: React.PropTypes.oneOf(['create', 'edit']).isRequired,
        formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']).isRequired,
        colOffset: React.PropTypes.number,
        cancelBtn: React.PropTypes.bool,
        onCancelClick: React.PropTypes.func,
        onSubmitClick: React.PropTypes.func.isRequired
    },
    getDefaultProps: function () {
        return {
            onCancelClick: function () {
            }
        };
    },
    render: function () {
        var divClasses;
        if (this.props.formLayout === 'horizontal') {
            divClasses = 'col-md-offset-' + this.props.colOffset + ' col-md-' + (12 - this.props.colOffset);
        } else {
            divClasses = 'col-md-12';
        }

        var submitBtnText = this.props.mode === 'create' ? 'Create' : 'Save changes';
        return (
            div({className: 'row', style: {textAlign: 'right'}},
                div({className: divClasses},
                    this.props.cancelBtn ? Button({
                        text: 'Cancel',
                        onClick: this.props.onCancelClick
                    }, 'Cancel') : null,
                    Button({
                        type: 'button',
                        style: 'primary',
                        css: {marginLeft: 5},
                        text: submitBtnText,
                        onClick: this.props.onSubmitClick
                    })
                )
            )
        );
    }
});

module.exports = React.createFactory(FormButtons);
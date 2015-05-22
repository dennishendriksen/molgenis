var React = require("react/addons");
var DeepPureRenderMixin = require("./mixin/DeepPureRenderMixin");

/**
 * @memberOf component
 */
var FormIndex = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'FormIndex',
    propTypes: {
        entity: React.PropTypes.object.isRequired
    },
    render: function () {
        var IndexItems = [];
        var attrs = this.props.entity.attributes;
        for (var key in attrs) {
            if (attrs.hasOwnProperty(key)) {
                var attr = attrs[key];
                if (attr.fieldType === 'COMPOUND') {
                    var IndexItem = (
                        li({key: attr.name},
                            a({href: this._getLinkName(attr)}, attr.label)
                        )
                    );
                    IndexItems.push(IndexItem);
                }
            }
        }

        return (
            ol({style: {'list-style-type': 'none'}},
                IndexItems
            )
        );
    },
    _getLinkName: function (attr) {
        return '#' + attr.name + '-link';
    }
});

module.exports = React.createFactory(FormIndex);

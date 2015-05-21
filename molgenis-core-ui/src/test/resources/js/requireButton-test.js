jest.dontMock('../../../main/resources/js/requireButton.js');
describe('Button', function() {
  it('fires event after click', function() {
    var React = require('react/addons');
    var ButtonComponent = require('../../../main/resources/js/requireButton.js');
    var TestUtils = React.addons.TestUtils;

    var callback = jest.genMockFunction();

    var Button = TestUtils.renderIntoDocument(
		ButtonComponent({onClick: callback})
    );

    var button = TestUtils.findRenderedDOMComponentWithTag(Button, 'button');
    TestUtils.Simulate.click(button);
    expect(callback).toBeCalled();
  });
});
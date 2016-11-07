import React, { Component } from 'react';

import DestinationList from './DestinationList';

class DestinationsBox extends Component {
  constructor(props) {
    super(props);
    this.state = {
      "responseType": "LIVE_FEED",
      "responseData":[
        {
          "country":"Norway",
          "city":"Oslo",
          "latitude":"59.91273",
          "longitude":"10.74609",
          "count":88
        },
        {
          "country":"Ukraine",
          "city":"Kiev",
          "latitude":"50.45466",
          "longitude":"30.5238",
          "count":59
        }
      ]
    };
  }

  subscribeToData() {
    console.log("Should subscribe to data here");

    // TODO: this url should be passed as a props
    let exampleSocket = new WebSocket("ws://localhost:9000/livestate");

    exampleSocket.onopen = function (event) {
      console.log("WebSocket Connection open.");
      let obj = {"requestType": "SUBSCRIBE"};
      exampleSocket.send(JSON.stringify(obj));
    };

    exampleSocket.onmessage = function (event) {
      let data = JSON.parse(event.data);
      if (data.responseType && data.responseData) {
        this.setState({
          responseData: data.responseData,
          responseType: data.responseType
        });
      }
    }.bind(this);

    exampleSocket.onclose = function (event) {
      console.log("WebSocket Connection closed.")
      console.log(event);
    };
  }

  componentDidMount() {
    this.subscribeToData();
  }

  render() {
    return (
      <div className="destinations">
        <h1>Destinations List</h1>
        <DestinationList data={ this.state.responseData } />
      </div>
    );
  }
}

export default DestinationsBox;
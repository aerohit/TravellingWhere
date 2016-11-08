import React, { Component } from 'react';

import Destination from './Destination';

class DestinationList extends Component {
  render() {
    let listData = this.props.data;
    listData.sort(function(d1, d2) {
      return d2.count - d1.count;
    });

    let totalResults = listData.map(function(d) {
      return d.count;
    }).reduce(function(d1, d2) {
      return d1 + d2;
    }, 0);

    let destinationNodes = listData.map((d) => {
      return (
        <Destination key={d.city} country={d.country} city={d.city} count= {d.count} />
      );
    });

    return (
      <div className="DestinationList">
        <h3>Total searches = {totalResults}</h3>
        <table className="u-full-width">
          <thead>
            <tr>
              <th>Country</th>
              <th>City</th>
              <th>Visitors</th>
            </tr>
          </thead>
          <tbody>
            { destinationNodes }
          </tbody>
        </table>
      </div>
    );
  }
}

export default DestinationList;

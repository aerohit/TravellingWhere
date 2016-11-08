import React, { Component } from 'react';

class Destination extends Component {
  render() {
    return (
      <tr>
        <td>{this.props.country}</td>
        <td>{this.props.city}</td>
        <td>{this.props.count}</td>
      </tr>
    );
  }
}

export default Destination;
